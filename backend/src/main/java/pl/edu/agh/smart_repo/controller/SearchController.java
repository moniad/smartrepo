package pl.edu.agh.smart_repo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.smart_repo.service.SearchService;

import java.util.List;

@RestController
public class SearchController {

    @GetMapping("/search/{phrase}")
    @ResponseBody
    public ResponseEntity<List<String>> get(@PathVariable String phrase) {
        List<String> documentsContainingPhraseNames = SearchService.searchDocuments(phrase);
        return new ResponseEntity<>(documentsContainingPhraseNames, HttpStatus.OK);
    }
}