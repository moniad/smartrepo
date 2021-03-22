package Indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import main.java.util.SynchList;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class IndexerTask extends Thread {
    SynchList<File> synchList;
    IndexWriter indexWriter;

    private int my_id;
    private static int id = 0;

    public IndexerTask(SynchList<File> synchList, IndexWriter indexWriter)
    {
        this.synchList = synchList;
        this.indexWriter = indexWriter;
        this.my_id = IndexerTask.id++;
    }


    @Override
    public void run() {
        System.out.println(my_id + " Starting indexer task");
        FileReader fr = null;
        while (true)
        {
            System.out.println(my_id + " waiting for file");
            File f = synchList.getNext();
            if (f == null) {
                System.out.println(my_id + " received null");
                if (fr != null)
                {
                    try {
                        fr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }

            System.out.println(my_id + " received: " + f.getName());

            Document doc = new Document();

            //===================================================
            // add contents of file
            //===================================================
            try {
                fr = new FileReader(f);
                doc.add(new TextField("contents", fr));
                doc.add(new StringField("path", f.getPath(), Field.Store.YES));
                doc.add(new StringField("filename", f.getName(), Field.Store.YES));

                System.out.println(my_id + " writing");
                indexWriter.addDocument(doc);
                System.out.println(my_id + " writing done");
            } catch (IOException ex){
                System.out.println(my_id + " IOexception while reading file " + f + ", msg: " + ex.getMessage());
                ex.printStackTrace();
                if (fr != null)
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

    }
}
