package pl.edu.agh.smart_repo;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
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

    private final static Path filesCatalogPath = Paths.get(System.getProperty("user.dir"), "files");;

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
