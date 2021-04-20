package pl.edu.agh.smart_repo.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.services.translation.Translator;
import pl.edu.agh.smart_repo.services.translation.translators.MyMemoryTranslator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ConfigurationFactory {

    @Autowired
    ApplicationArguments appArgs;

    private final Path storagePath = Paths.get("/storage");
    private final String index = "myindex";

    public String getRabbitHost()
    {
        String host;
        List<String> args = appArgs.getNonOptionArgs();

        if (args.size() > 0) {
            host = args.get(0);
        }
        else {
            host = "localhost";
        }

        return host;
    }

    public String getElasticSearchHost()
    {
        String host;
        List<String> args = appArgs.getNonOptionArgs();

        if (args.size() > 1) {
            host = args.get(1);
        }
        else {
            host = "localhost";
        }

        return host;
    }

    public String getIndex()
    {
        return index;
    }

    public Translator getTranslator() { return new MyMemoryTranslator(); }

    public Path getStoragePath() {
        return storagePath;
    }

}
