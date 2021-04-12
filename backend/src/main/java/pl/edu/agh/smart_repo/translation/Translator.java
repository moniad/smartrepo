package pl.edu.agh.smart_repo.translation;


public interface Translator {
    String translate(String text, Language sourceLanguage, Language targetLanguage);
}
