package pl.edu.agh.smart_repo.common.document_fields;

import lombok.Data;

@Data
public class DocumentStructure {

    private String name;
    private String path;
    private String contents;
    private String keywords;
    private String creationDate;
    private String modificationDate;
    private String language;

    public void setByDocumentField(DocumentFields documentField, String val) {
        switch (documentField) {
            case NAME:
                this.name = val;
                break;
            case PATH:
                this.path = val;
                break;
            case CONTENTS:
                this.contents = val;
                break;
            case KEYWORDS:
                this.keywords = val;
                break;
            case CREATION_DATE:
                this.creationDate = val;
                break;
            case MODIFICATION_DATE:
                this.modificationDate = val;
                break;
            case LANGUAGE:
                this.language = val;
                break;
        }
    }

    public String getByDocumentField(DocumentFields documentField) {
        switch (documentField) {
            case NAME:
                return this.name;
            case PATH:
                return this.path;
            case CONTENTS:
                return this.contents;
            case KEYWORDS:
                return this.keywords;
            case CREATION_DATE:
                return this.creationDate;
            case MODIFICATION_DATE:
                return this.modificationDate;
            case LANGUAGE:
                return this.language;
            default:
                return null;
        }
    }
}