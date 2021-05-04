package pl.edu.agh.smart_repo.services.search;

import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.services.index.IndexerService;
import pl.edu.agh.smart_repo.services.translation.Language;
import pl.edu.agh.smart_repo.services.translation.TranslationService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static pl.edu.agh.smart_repo.services.translation.Language.*;

@Service
@Slf4j
public class SearchService {

    private final IndexerService indexerService;
    private final TranslationService translationService;

    private static final List<Language> defaultLanguagesToSearch
            = new ArrayList<>(Arrays.asList(POLISH, FRENCH, SPANISH, GERMAN, ITALIAN));

    public SearchService(IndexerService indexerService, TranslationService translationService) {
        this.indexerService = indexerService;
        this.translationService = translationService;
    }

    public List<FileInfo> searchDocuments(String phrase, int fromIndex, int resultSize) {
        return searchDocuments(phrase, defaultLanguagesToSearch, fromIndex, resultSize);
    }

    public List<FileInfo> searchDocuments(String phrase, List<Language> languagesToSearch, int fromIndex, int resultSize) {
        log.info("Searching for: " + phrase + " in languages: " + languagesToSearch.toString() + "...");

        List<String> translatedPhrases = translationService.translate(phrase, Language.ENGLISH, languagesToSearch);
        return translatedPhrases.stream().map(phraseInSomeLanguage ->
                indexerService.search(DocumentFields.CONTENTS, phraseInSomeLanguage, fromIndex, resultSize))
                .filter(result -> !result.isEmpty())
                .map(Option::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
