package pl.edu.agh.smart_repo.common.document_fields;

public enum DocumentField {
    NAME("name"),
    PATH("path"),
    CONTENTS("contents"),
    KEYWORDS("keywords"),
    CREATION_DATE("creation_date"),
    MODIFICATION_DATE("modification_date"),
    LANGUAGE("language"),
    SIZE("size"),
    EXTENSION("extension");

    private final String fieldName;

    DocumentField(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return fieldName;
    }
}