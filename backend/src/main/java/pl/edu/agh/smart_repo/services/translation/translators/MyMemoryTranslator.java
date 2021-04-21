package pl.edu.agh.smart_repo.services.translation.translators;

import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import pl.edu.agh.smart_repo.services.translation.Language;
import pl.edu.agh.smart_repo.services.translation.Translator;
import pl.edu.agh.smart_repo.services.translation.TextCannotBeTranslatedException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class MyMemoryTranslator implements Translator {

    private static final String MY_MEMORY_URL_BASE = "https://api.mymemory.translated.net/get?";
    private static final String TEXT_PARAMETER = "q=";
    private static final String LANGUAGES_PARAMETER = "&langpair=";
    private static final String LANGUAGES_DELIMITER = "|";

    @Override
    public String translate(String text, Language sourceLanguage, Language targetLanguage) throws TextCannotBeTranslatedException {
        InputStreamReader reader;
        String encodedString = buildUrl(text, sourceLanguage, targetLanguage);
        try {
            URL url = new URL(encodedString);
            reader = new InputStreamReader(url.openStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new TextCannotBeTranslatedException();
        }

        MyMemoryDTO dto = new Gson().fromJson(reader, MyMemoryDTO.class);
        return dto.responseData.translatedText;
    }

    @Override
    public List<String> translate(String text, Language sourceLanguage, List<Language> targetLanguages) throws TextCannotBeTranslatedException {
        return targetLanguages.stream()
                .map(targetLanguage -> translate(text, sourceLanguage, targetLanguage))
                .collect(Collectors.toList());
    }

    private String buildUrl(String query, Language sourceLanguage, Language targetLanguage) {
        String builtUrl = MY_MEMORY_URL_BASE +
                TEXT_PARAMETER +
                query +
                LANGUAGES_PARAMETER +
                sourceLanguage +
                LANGUAGES_DELIMITER +
                targetLanguage;
        return UrlEscapers.urlFragmentEscaper().escape(builtUrl);
    }


    private class MyMemoryDTO {
        ResponseData responseData;
    }

    private class ResponseData {
        String translatedText;
        double match;
    }
}
