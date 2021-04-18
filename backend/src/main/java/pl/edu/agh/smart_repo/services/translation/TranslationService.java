package pl.edu.agh.smart_repo.services.translation;

import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;

@Service
public class TranslationService {

    private Translator translator;

    public TranslationService(ConfigurationFactory configurationFactory) {
        this.translator = configurationFactory.getTranslator();
    }

    public String translate(String text, Language sourceLanguage, Language targetLanguage) {
        return translator.translate(text, sourceLanguage, targetLanguage);
    }
}
