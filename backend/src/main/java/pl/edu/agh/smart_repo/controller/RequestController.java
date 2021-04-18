package pl.edu.agh.smart_repo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.file_upload.FileUploadService;
import pl.edu.agh.smart_repo.service.SearchService;

import java.io.IOException;

@RestController
public class RequestController {

    @Autowired
    SearchService searchService;
    @Autowired
    FileUploadService fileUploadService;


    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    @ResponseBody
    public ResponseEntity<String> indexFile(@RequestParam("files") MultipartFile file) throws IOException {
        Result result = fileUploadService.processFile(file, "main");
        System.out.println(result);

//        System.out.println("file: " + file.getName());
//        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
//        convFile.createNewFile();
//        FileOutputStream fos = new FileOutputStream(convFile);
//        fos.write(file.getBytes());
//        fos.close();
//        System.out.println("convFile: " + convFile);
//        System.out.println("convFile.getAbsolutePath: " + convFile.getAbsolutePath());
//        System.out.println("convFile.getName: " + convFile.getName());
//        System.out.println("convFile.getName: " + convFile.toURI());

        return new ResponseEntity<>("added file", HttpStatus.OK);
    }
}
