package pl.edu.agh.smart_repo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.request_handler.uploader.FileUploadHandler;
import pl.edu.agh.smart_repo.request_handler.uploader.file_saver.FileSaver;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.file.FileService;
import pl.edu.agh.smart_repo.indexer.IndexerService;
import pl.edu.agh.smart_repo.parser.ParserService;
import pl.edu.agh.smart_repo.service.SearchService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;


@RestController
public class Controller {

    @Autowired
    SearchService searchService;
    @Autowired
    IndexerService indexerService;
    @Autowired
    ParserService parserService;
    @Autowired
    FileUploadHandler fileUploadHandler;
    @Autowired
    FileService fileService;

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
            if(!fileService.hasAcceptableExtension(file)){
                return new ResponseEntity<>("ERROR while adding. Unacceptable file format.", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            path = file.getAbsolutePath();

            // path should lead to folder on server
            Result result = fileUploadHandler.processFile(file, "pdfs");
            System.out.println("Saving file: " + result.toString());

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