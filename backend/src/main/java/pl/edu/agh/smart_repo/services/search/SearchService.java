package pl.edu.agh.smart_repo.services.search;

import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.common.request.SearchRequest;
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

    public List<FileInfo> searchDocuments(SearchRequest searchRequest) {
        if (searchRequest.getLanguagesToSearchIn() == null) {
            return searchDocuments(searchRequest.getPhrase(), defaultLanguagesToSearch);
        }
        return searchDocuments(searchRequest.getPhrase(), searchRequest.getLanguagesToSearchIn());
    }

    public List<FileInfo> searchDocuments(String phrase, List<Language> languagesToSearch) {
        log.info("Searching for: " + phrase + " in languages: " + languagesToSearch.toString() + "...");

        List<String> translatedPhrases = Arrays.asList(phrase, "engine");//translationService.translate(phrase, Language.ENGLISH, languagesToSearch);todo
        return translatedPhrases.stream().map(indexerService::search)
                .filter(result -> !result.isEmpty())
                .map(Option::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
