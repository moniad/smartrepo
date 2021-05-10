package pl.edu.agh.smart_repo.services.translation;

import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;

import java.util.List;

@Service
public class TranslationService {

    private final Translator translator;

    public TranslationService(ConfigurationFactory configurationFactory) {
        this.translator = configurationFactory.getTranslator();
    }

    public List<String> translate(String text, Language sourceLanguage, List<Language> targetLanguages) {
        return translator.translate(text, sourceLanguage, targetLanguages);
    }
}
