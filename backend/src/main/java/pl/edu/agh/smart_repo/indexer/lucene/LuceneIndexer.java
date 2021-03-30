package pl.edu.agh.smart_repo.indexer.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.indexer.Indexer;

import java.io.*;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class LuceneIndexer implements Indexer {

    private static final StandardAnalyzer analyzer = new StandardAnalyzer();

    private final FSDirectory index;
    private final IndexWriter writer;

    public LuceneIndexer(String indexDir) throws IOException {
        index = FSDirectory.open(Paths.get(indexDir));

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        writer = new IndexWriter(index, config);
    }

    @Override
    public void indexDocument(DocumentStructure documentStructure) {
        System.out.println("INDEX: " + documentStructure.getByName(DocumentFields.PATH));
        Document doc = new Document();
        for (DocumentFields documentField : DocumentFields.values()) {
            String value = documentStructure.getByName(documentField);
            if (value != null)
                doc.add(new TextField(documentField.toString(), value, Field.Store.YES));
        }

        try {
            writer.addDocument(doc);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> search(DocumentFields documentField, String phrase) {
        List<String> results = new LinkedList<>();
        try {
            Query q = new QueryParser(documentField.toString(), analyzer).parse(phrase);

            int hitsPerPage = 10;
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;

            for(int i=0;i<hits.length;++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);

                results.add(d.get(DocumentFields.PATH.toString()));
            }

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }

        return results;
    }
}
