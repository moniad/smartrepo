package pl.edu.agh.smart_repo.services.translation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;

import java.util.List;

@Service
public class TranslationService {

    private final Translator translator;

    @Autowired
    public TranslationService(ConfigurationFactory configurationFactory) {
        this.translator = configurationFactory.getTranslator();
    }

    public String translate(String text, Language sourceLanguage, Language targetLanguage) {
        return translator.translate(text, sourceLanguage, targetLanguage);
    }

    public List<String> translate(String text, Language sourceLanguage, List<Language> targetLanguages) {
        return translator.translate(text, sourceLanguage, targetLanguages);
    }
}
