package pl.edu.agh.smart_repo.common.document_fields;

public enum DocumentFields {
    NAME("name"),
    PATH("path"),
    CONTENTS("contents"),
    KEYWORDS("keywords"),
    CREATION_DATE("creation_date"),
    MODIFICATION_DATE("modification_date"),
    LANGUAGE("language");

    private final String s;

    DocumentFields(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return s;
    }
}