package pl.edu.agh.smart_repo.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.ConfigurationFactory;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;

@Service
public class ParserService {

    private Parser parser;

    @Autowired
    public ParserService(ConfigurationFactory configurationFactory)
    {
        this.parser = configurationFactory.getParser();
    }

    public ParseResult parse(MultipartFile file)
    {
        return parser.parse(file);
    }
}
