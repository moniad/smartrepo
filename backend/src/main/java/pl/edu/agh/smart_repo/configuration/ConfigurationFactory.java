package pl.edu.agh.smart_repo.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.services.translation.Translator;
import pl.edu.agh.smart_repo.services.translation.translators.MyMemoryTranslator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ConfigurationFactory {

    private final Path storagePath;
    private final Path tempStoragePath;
    private final String rabbitHost;
    private final String elasticSearchAddress;
    private final String index;

    @Autowired
    ConfigurationFactory(ApplicationArguments appArgs,
                         @Value("${storage.path}") String storagePath,
                         @Value("${temp.storage.path}") String tempStoragePath,
                         @Value("${elastic.index.index_name}") String index) {
        this.storagePath = Paths.get(storagePath);
        this.tempStoragePath = Paths.get(tempStoragePath);
        this.index = index;
        List<String> args = appArgs.getNonOptionArgs();

        if (args.size() > 0) {
            rabbitHost = args.get(0);
        } else {
            rabbitHost = "localhost";
        }

        if (args.size() > 1) {
            elasticSearchAddress = args.get(1);
        } else {
            elasticSearchAddress = "http://localhost:9200";
        }
    }

    public String getRabbitHost() {
        return rabbitHost;
    }

    public String getElasticSearchAddress() {
        return elasticSearchAddress;
    }

    public String getIndex() {
        return index;
    }

    public Translator getTranslator() {
        return new MyMemoryTranslator();
    }

    public Path getStoragePath() {
        return storagePath;
    }

    public Path getTempStoragePath() {
        return tempStoragePath;
    }
}
