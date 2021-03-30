package pl.edu.agh.smart_repo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.indexer.IndexerService;
import pl.edu.agh.smart_repo.parser.ParserService;
import pl.edu.agh.smart_repo.service.SearchService;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
public class Controller {

    @Autowired
    SearchService searchService;
    @Autowired
    IndexerService indexerService;
    @Autowired
    ParserService parserService;

    @GetMapping("/search/{phrase}")
    @ResponseBody
    public ResponseEntity<List<String>> searchForPhrase(@PathVariable String phrase) {
        System.out.println("SEARCH for: " + phrase);
        List<String> documentsContainingPhraseNames = searchService.searchDocuments(phrase);
        return new ResponseEntity<>(documentsContainingPhraseNames, HttpStatus.OK);
    }

    //TODO process uploaded files
    @PostMapping(value = "/add")
    @ResponseBody
    public ResponseEntity<String> indexFile() {

        String path = null;
        try {
            Resource resource = new ClassPathResource("parsable-documents/pdf/Easy-to-parse-document.pdf");
            File file = resource.getFile();
            path = file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (path != null) {
            DocumentStructure documentStructure = parserService.parse(path);

            indexerService.indexDocument(documentStructure);

            return new ResponseEntity<>("added file", HttpStatus.OK);
        }

        return new ResponseEntity<>("ERROR while adding", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}