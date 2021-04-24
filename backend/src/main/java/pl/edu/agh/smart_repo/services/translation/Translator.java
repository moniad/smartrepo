package pl.edu.agh.smart_repo.services.translation;


import java.util.List;

public interface Translator {
    String translate(String text, Language sourceLanguage, Language targetLanguage) throws TextCannotBeTranslatedException;
    List<String> translate(String text, Language sourceLanguage, List<Language> targetLanguages) throws TextCannotBeTranslatedException;
}
