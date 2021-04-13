package pl.edu.agh.smart_repo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.indexer.IndexerService;
import pl.edu.agh.smart_repo.translation.Language;
import pl.edu.agh.smart_repo.translation.TranslationService;

import java.util.List;

@Service
public class SearchService {

    @Autowired
    IndexerService indexerService;

    @Autowired
    TranslationService translationService;

    public List<String> searchDocuments(String phrase) {
        System.out.println("Searching for: " + phrase + " . . .");

        String translatedText = translationService.translate(phrase, Language.POLISH, Language.SPANISH);
        System.out.println("translated text: " + translatedText);

        return indexerService.search(DocumentFields.CONTENTS, phrase);
    }
}
