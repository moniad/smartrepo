package pl.edu.agh.smart_repo.services.translation;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

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

    @JsonCreator
    public static Language fromValue(String value) {
        return Arrays.stream(Language.values())
                .filter(l -> l.languageShortForm.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown language: " + value + ", Allowed values are " + Arrays.toString(values())));
    }

    @Override
    public String toString() {
        return languageShortForm;
    }
}
