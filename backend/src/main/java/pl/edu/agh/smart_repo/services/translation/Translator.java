package pl.edu.agh.smart_repo.services.translation;


public interface Translator {
    String translate(String text, Language sourceLanguage, Language targetLanguage);
}
