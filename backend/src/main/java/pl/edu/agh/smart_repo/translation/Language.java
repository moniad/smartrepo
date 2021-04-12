package pl.edu.agh.smart_repo.translation;

public enum Language {
    ENGLISH("en"),
    POLISH("pl"),
    GERMAN("de"),
    ITALIAN("it"),
    FRENCH("fr"),
    SPANISH("es");

    private final String languageShortForm;

    Language(String shortForm) {
        this.languageShortForm = shortForm;
    }

    @Override
    public String toString() {
        return languageShortForm;
    }
}
