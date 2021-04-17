package pl.edu.agh.smart_repo;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.indexer.Indexer;
import pl.edu.agh.smart_repo.indexer.lucene.LuceneIndexer;
import pl.edu.agh.smart_repo.parser.Parser;
import pl.edu.agh.smart_repo.parser.tika.TikaParser;
import pl.edu.agh.smart_repo.translation.Translator;
import pl.edu.agh.smart_repo.translation.translators.MyMemoryTranslator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ConfigurationFactory {

    //private final static Path filesCatalogPath = Paths.get(System.getProperty("user.dir"), "files");
    private final static Path filesCatalogPath = Paths.get("C:/tmp_files_path");
    private final static String indexDir = "/index";

    public Indexer getIndexer()
    {
        //TODO deletes existing index. Desired for testing, change later
        File dir = new File(indexDir);
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Indexer indexer = null;
        try {
            indexer = new LuceneIndexer(indexDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return indexer;
    }

    public Parser getParser() {
        return new TikaParser();
    }

    public Translator getTranslator() {
        return new MyMemoryTranslator();
    }

    public Path getFileCatalogPath() {
        return filesCatalogPath;
    }

}
