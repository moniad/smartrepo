package pl.edu.agh.smart_repo.services.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;

import java.util.List;

@Service
public class IndexerService {

    private Indexer indexer;

    @Autowired
    public IndexerService(ConfigurationFactory configurationFactory)
    {
    }

    public Result indexDocument(DocumentStructure documentStructure)
    {
        indexer.indexDocument(documentStructure);
        return new Result(ResultType.SUCCESS);
    }

    public List<String> search(DocumentFields documentField, String phrase)
    {
        return indexer.search(documentField, phrase);
    }
}
