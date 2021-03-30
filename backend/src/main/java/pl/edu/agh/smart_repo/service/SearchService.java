package pl.edu.agh.smart_repo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.document_fields.DocumentFields;
import pl.edu.agh.smart_repo.indexer.IndexerService;

import java.util.Collections;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    IndexerService indexerService;

    public List<String> searchDocuments(String phrase) {
        System.out.println("Searching for: " + phrase + " . . .");

        return indexerService.search(DocumentFields.CONTENTS, phrase);
    }
}
