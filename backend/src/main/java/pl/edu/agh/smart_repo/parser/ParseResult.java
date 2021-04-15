package pl.edu.agh.smart_repo.parser;

import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;

public class ParseResult extends Result {

    private DocumentStructure parsedFile = null;

    public ParseResult(ResultType type) {
        super(type);
    }

    public ParseResult(ResultType type, String message) {
        super(type, message);
    }

    public ParseResult(ResultType type, Exception exception) {
        super(type, exception);
    }

    public ParseResult(ResultType type, String message, Exception exception) {
        super(type, message, exception);
    }

    public ParseResult(ResultType type, DocumentStructure parsedFile) {
        super(type);
        this.parsedFile = parsedFile;
    }

    public ParseResult(ResultType type, String message, DocumentStructure parsedFile) {
        super(type, message);
        this.parsedFile = parsedFile;
    }

    public ParseResult(ResultType type, Exception exception, DocumentStructure parsedFile) {
        super(type, exception);
        this.parsedFile = parsedFile;
    }

    public ParseResult(ResultType type, String message, Exception exception, DocumentStructure parsedFile) {
        super(type, message, exception);
        this.parsedFile = parsedFile;
    }

    public DocumentStructure getParsedFile() {
        return parsedFile;
    }
}
