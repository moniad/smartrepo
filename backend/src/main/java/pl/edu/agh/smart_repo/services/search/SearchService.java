package pl.edu.agh.smart_repo.services.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.services.translation.Language;
import pl.edu.agh.smart_repo.services.translation.TranslationService;

import java.util.Collections;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    TranslationService translationService;

    public List<String> searchDocuments(String phrase) {
        System.out.println("Searching for: " + phrase + " . . .");

        String translatedText = translationService.translate(phrase, Language.POLISH, Language.SPANISH);
        System.out.println("translated text: " + translatedText);

        System.out.println("(TODO) search not implemented");
        return Collections.singletonList("not implemented");
    }
}
