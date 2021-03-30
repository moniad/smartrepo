package pl.edu.agh.smart_repo.indexer;

import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;

import java.util.List;

public interface Indexer {
    void indexDocument(DocumentStructure documentStructure);
    List<String> search(DocumentFields documentField, String phrase);
}
