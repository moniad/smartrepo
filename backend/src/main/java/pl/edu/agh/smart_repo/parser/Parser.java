package pl.edu.agh.smart_repo.parser;

import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;

public interface Parser {
    DocumentStructure parse(String path);
}
