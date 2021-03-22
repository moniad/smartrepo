package Indexer;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import main.java.util.SynchList;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 * based on the input of the user.
 */
public class TextFileIndexer {

    private static final int NUM_OF_INDEX_THREADS = 3;

    private static final StandardAnalyzer analyzer = new StandardAnalyzer();

    private final IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Enter the path where the index will be created: (e.g. /tmp/index or c:\\temp\\index)");

        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));

        String s = br.readLine();

        String indexLocation = s;

        TextFileIndexer indexer = null;
        try {
            indexer = new TextFileIndexer(s);
        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        //===================================================
        //read input from user until he enters q for quit
        //===================================================

        System.out.println("Enter the full path to directory to index: (e.g. /home/ron/mydir or c:\\Users\\ron\\mydir)");
        s = br.readLine();

        System.out.println("indexing");

        long start = System.nanoTime();

        indexer.indexFileOrDirectory(s);

        long duration = System.nanoTime() - start;
        double duration_in_s = (double) duration / 1_000_000_000;

        System.out.println("indexing done, duration: " + duration_in_s + " s");

        //===================================================
        //after adding, we always have to call the
        //closeIndex, otherwise the index is not created
        //===================================================
        indexer.closeIndex();

        //=========================================================
        // Now search
        //=========================================================
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexLocation)));
        IndexSearcher searcher = new IndexSearcher(reader);

        s = "";
        while (!s.equalsIgnoreCase("q")) {
            try {
                TopScoreDocCollector collector = TopScoreDocCollector.create(5,10);
                System.out.println("Enter the search query (q=quit):");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }
                Query q = new QueryParser("contents", analyzer).parse(s);
                searcher.search(q, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;

                // 4. display results
                System.out.println("Found " + hits.length + " hits.");
                for(int i=0;i<hits.length;++i) {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println((i + 1) + ". " + d.get("path") + " score=" + hits[i].score);
                }

            } catch (Exception e) {
                System.out.println("Error searching " + s + " : " + e.getMessage());
            }
        }

    }

    /**
     * Constructor
     * @param indexDir the name of the folder in which the index should be created
     * @throws java.io.IOException when exception creating index.
     */
    TextFileIndexer(String indexDir) throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(indexDir));


        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        writer = new IndexWriter(dir, config);
    }

    /**
     * Indexes a file or directory
     * @param fileName the name of a text file or a folder we wish to add to the index
     * @throws java.io.IOException when exception
     */
    public void indexFileOrDirectory(String fileName) throws Exception {
        //===================================================
        //gets the list of files in a folder (if user has submitted
        //the name of a folder) or gets a single file name (is user
        //has submitted only the file name)
        //===================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.getDocStats().numDocs;

        indexFiles(new SynchList<>(queue), writer, NUM_OF_INDEX_THREADS);

        int newNumDocs = writer.getDocStats().numDocs;
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private void indexFiles(SynchList<File> synchList, IndexWriter indexWriter, int num_of_threads) throws InterruptedException {
        ArrayList<Thread> threads = new ArrayList<>();
        for (int i=0; i< num_of_threads; ++i)
            threads.add(new IndexerTask(synchList, indexWriter));

        for (Thread thread: threads)
            thread.start();

        for (Thread thread: threads)
            thread.join();
    }

    private void addFiles(File file) {

        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        else {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    addFiles(f);
                }
            } else {
                String filename = file.getName().toLowerCase();
                //===================================================
                // Only index text files
                //===================================================
                if (filename.endsWith(".htm") || filename.endsWith(".html") ||
                        filename.endsWith(".xml") || filename.endsWith(".txt")) {
                    queue.add(file);
                } else {
                    System.out.println("Skipped " + filename);
                }
            }
        }
    }

    /**
     * Close the index.
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException {
        writer.close();
    }
}
