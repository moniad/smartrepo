package pl.edu.agh.smart_repo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.common.file.FileService;
import pl.edu.agh.smart_repo.indexer.IndexerService;
import pl.edu.agh.smart_repo.parser.ParserService;
import pl.edu.agh.smart_repo.request_handler.uploader.FileUploadHandler;
import pl.edu.agh.smart_repo.service.SearchService;

@RestController
public class FileController {

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

    @PostMapping(value = "/upload")
    @ResponseBody
    public ResponseEntity<String> indexFile(@RequestBody MultipartFile file) {
        System.out.println("getName file: " + file.getName());
        System.out.println("getOriginalFilename file: " + file.getOriginalFilename());
        System.out.println("getContentType file: " + file.getContentType());
//        System.out.println("Saving file: " + file.toString());
//        System.out.println("Saving file: " + file.toString());

        return new ResponseEntity<>("added file", HttpStatus.OK);
    }
}
