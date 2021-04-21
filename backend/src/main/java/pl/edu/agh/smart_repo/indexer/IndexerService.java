package pl.edu.agh.smart_repo.indexer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.ConfigurationFactory;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;

import java.util.List;

@Service
public class IndexerService {

    private Indexer indexer;

    @Autowired
    public IndexerService(ConfigurationFactory configurationFactory)
    {
    }

    public void indexDocument(DocumentStructure documentStructure)
    {
        indexer.indexDocument(documentStructure);
    }

    public List<String> search(DocumentFields documentField, String phrase)
    {
        return indexer.search(documentField, phrase);
    }

}
