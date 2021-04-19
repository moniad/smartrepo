package pl.edu.agh.smart_repo.common.document_fields;

public enum DocumentFields {
    NAME("name"),
    PATH("path"),
    CONTENTS("contents"),
    KEYWORDS("keywords");

    private final String s;

    DocumentFields(String s) {
        this.s = s;
    }

    @Override
    public String toString() {
        return s;
    }
}
