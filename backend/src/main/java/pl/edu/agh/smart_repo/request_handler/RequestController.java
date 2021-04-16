package pl.edu.agh.smart_repo.request_handler;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;

@RestController
public class RequestController {

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

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    @ResponseBody
    public ResponseEntity<String> indexFile(@RequestParam("files") MultipartFile file) throws IOException {
        System.out.println("file: " + file.getName());
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        convFile.createNewFile();
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        System.out.println("convFile: " + convFile);
        System.out.println("convFile.getAbsolutePath: " + convFile.getAbsolutePath());
        System.out.println("convFile.getName: " + convFile.getName());
        System.out.println("convFile.getName: " + convFile.toURI());

        return new ResponseEntity<>("added file", HttpStatus.OK);
    }
}
