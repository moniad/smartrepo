package pl.edu.agh.smart_repo.parser;

import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;

public interface Parser {
    ParseResult parse(MultipartFile file);
}
