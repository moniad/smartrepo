package pl.edu.agh.smart_repo.request_handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.common.file.FileInfo;
import pl.edu.agh.smart_repo.common.request.CreateDirectoryRequest;
import pl.edu.agh.smart_repo.common.request.SearchRequest;
import pl.edu.agh.smart_repo.common.response.Result;
import pl.edu.agh.smart_repo.services.directory_tree.FileManagerService;
import pl.edu.agh.smart_repo.services.directory_tree.FileTreeFetcherService;
import pl.edu.agh.smart_repo.services.search.SearchService;
import pl.edu.agh.smart_repo.services.upload.FileUploadService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
public class RequestController {

    private final SearchService searchService;
    private final FileUploadService fileUploadService;
    private final FileTreeFetcherService fileTreeFetcherService;
    private final FileManagerService fileManagerService;

    public RequestController(SearchService searchService, FileUploadService fileUploadService,
                             FileTreeFetcherService fileTreeFetcherService, FileManagerService fileManagerService) {
        this.searchService = searchService;
        this.fileUploadService = fileUploadService;
        this.fileTreeFetcherService = fileTreeFetcherService;
        this.fileManagerService = fileManagerService;
    }

    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    @ResponseBody
    public ResponseEntity<String> uploadFile(@RequestParam("files") MultipartFile file, @RequestParam("path") String path) {

        //TODO file extension could be checked here, change fileService to accept MultipartFile

        Result result = fileUploadService.processFile(file, path);

        if (result.isSuccess())
            return new ResponseEntity<>("added file: " + file.getOriginalFilename(), HttpStatus.OK);
        else
            return new ResponseEntity<>("error while adding file: " + file.getOriginalFilename() +
                    " error: '" + result.getMessage() + "'",
                    HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping("/search")
    @ResponseBody
    public ResponseEntity<List<FileInfo>> searchForPhrase(@RequestBody SearchRequest searchRequest) {
        log.info("SEARCHING for: " + searchRequest.getPhrase());
        List<FileInfo> documentsContainingPhraseNames = searchService.searchDocuments(searchRequest);
        return new ResponseEntity<>(documentsContainingPhraseNames, HttpStatus.OK);
    }

    @GetMapping(value = "/search")
    @ResponseBody
    public ResponseEntity<List<FileInfo>> searchForPhrase(@RequestParam("phrase") String phrase,
                                                          @RequestParam("languages") String[] languages) {
        //TODO: search files with params
//        List<FileInfo> documentsContainingPhraseNames = searchService.searchDocuments(phrase, fromIndex, resultSize);
        FileInfo fileInfo = new FileInfo("Test file", System.currentTimeMillis(), false, 100000);
        ArrayList<FileInfo> f = new ArrayList<>();
        f.add(fileInfo);
        return new ResponseEntity<>(f, HttpStatus.OK);
    }

    @GetMapping(value = "/files")
    @ResponseBody
    public ResponseEntity<List<FileInfo>> getFiles(@RequestParam("path") String path) {
        var files = fileTreeFetcherService.fetchFileTree(path, false, null);
        return new ResponseEntity<>(files, HttpStatus.OK);
    }

    @CrossOrigin
    @DeleteMapping(value = "/files")
    @ResponseBody
    public ResponseEntity<String> deleteFile(@RequestParam("path") String path) {
        var result = fileManagerService.deleteFile(path);

        if (result.isSuccess())
            return new ResponseEntity<>("Deleted file: " + path, HttpStatus.OK);
        else
            return new ResponseEntity<>(result.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PostMapping(value = "/files")
    @ResponseBody
    public ResponseEntity<String> createDirectory(@RequestBody CreateDirectoryRequest createDirectoryRequest) {
        var result = fileManagerService.createDirectory(createDirectoryRequest.getPath());

        if (result.isSuccess())
            return new ResponseEntity<>("Created directory: " + createDirectoryRequest.getPath(), HttpStatus.OK);
        else
            return new ResponseEntity<>(result.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
