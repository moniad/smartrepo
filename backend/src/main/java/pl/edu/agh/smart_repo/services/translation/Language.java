package pl.edu.agh.smart_repo.services.translation;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum Language {
    ENGLISH("en", "English"),
    POLISH("pl", "Polish"),
    GERMAN("de", "German"),
    ITALIAN("it", "Italian"),
    FRENCH("fr", "French"),
    SPANISH("es", "Spanish");

    private final String languageShortForm;
    private final String languageLongForm;

    Language(String shortForm, String longForm) {
        this.languageShortForm = shortForm;
        this.languageLongForm = longForm;
    }

    @JsonCreator
    public static Language fromValue(String value) {
        return Arrays.stream(Language.values())
                .filter(l -> l.languageShortForm.equalsIgnoreCase(value) || l.languageLongForm.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown language: " + value + ", Allowed values are " + Arrays.toString(values())));
    }

    @Override
    public String toString() {
        return languageShortForm;
    }

    public String toLongString() {
        return languageLongForm;
    }
}
