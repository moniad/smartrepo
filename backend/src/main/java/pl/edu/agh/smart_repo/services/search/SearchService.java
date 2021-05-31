package pl.edu.agh.smart_repo.services.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

    private static final List<Language> defaultLanguagesToSearchIn
            = new ArrayList<>(Arrays.asList(POLISH, FRENCH, SPANISH, GERMAN, ITALIAN)); //todo: use HashSet instead of ArrayList

    public SearchService(IndexerService indexerService, TranslationService translationService) {
        this.indexerService = indexerService;
        this.translationService = translationService;
    }

    public List<FileInfo> searchDocuments(SearchRequest searchRequest) {
        if (searchRequest.getLanguagesToSearchIn() == null) {
            return searchDocuments(searchRequest.getPhrase(), defaultLanguagesToSearchIn);
        }
        return searchDocuments(searchRequest.getPhrase(), searchRequest.getLanguagesToSearchIn());
    }

    public List<FileInfo> searchDocuments(String phrase, List<Language> languagesToSearchIn) {

        Language sourceLanguage = detectInputPhraseLanguage(phrase);

        log.info("Searching for phrase \"" + phrase + "\" (" + sourceLanguage.toLongString() + ") in languages " + languagesToSearchIn.toString() + "...");

        List<String> searchPhrases = getTranslatedPhrasesToSearchFor(sourceLanguage, languagesToSearchIn, phrase);

        return searchPhrases.stream().map(indexerService::search)
                .filter(result -> !result.isEmpty())
                .map(Option::get)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Language detectInputPhraseLanguage(String inputPhrase) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject langDetectRequestBody = new JSONObject();
        langDetectRequestBody.put("phrase", inputPhrase);
        HttpEntity<String> request = new HttpEntity<String>(langDetectRequestBody.toString(), headers);
        String responseStr = restTemplate.postForObject("http://langdetecthost:10000/langdetect", request, String.class);
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode langDetectResponseBody = mapper.readTree(responseStr);
            String detectedLanguageStr = langDetectResponseBody.get("language").asText("English");
            Language detectedLanguage = Language.fromValue(detectedLanguageStr);
            return detectedLanguage;
        } catch (JsonProcessingException | IllegalArgumentException ignored) {
            log.error("Error: cannot detect input phrase language - using English by default.");
            return Language.ENGLISH;
        }
    }

    private List<String> getTranslatedPhrasesToSearchFor(Language sourceLanguage, List<Language> languagesToSearchIn, String phrase) {
        boolean shouldSearchInSourceLanguage = languagesToSearchIn.remove(sourceLanguage);
        List<String> translatedPhrases = translationService.translate(phrase, sourceLanguage, languagesToSearchIn);
        List<String> searchPhrases = new ArrayList<>(translatedPhrases);

        if (shouldSearchInSourceLanguage) {
            searchPhrases.add(phrase);
        }

        return searchPhrases;
    }
}
