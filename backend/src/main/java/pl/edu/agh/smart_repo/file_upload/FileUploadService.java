package pl.edu.agh.smart_repo.file_upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.ConfigurationFactory;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;
import pl.edu.agh.smart_repo.indexer.IndexerService;
import pl.edu.agh.smart_repo.parser.ParseResult;
import pl.edu.agh.smart_repo.parser.ParserService;
import pl.edu.agh.smart_repo.file_upload.file_saver.FileSaver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Service
public class FileUploadService {
    private Logger log = LoggerFactory.getLogger(FileUploadService.class);

    @Autowired
    ParserService parserService;
    @Autowired
    IndexerService indexerService;

    private final Path filesCatalogPath;

    public FileUploadService(ConfigurationFactory configurationFactory) {
        this.filesCatalogPath = configurationFactory.getFileCatalogPath();
    }

    public Result processFile(MultipartFile file, String path) {
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(filesCatalogPath.toString(), path, fileName);
        Result saveResult;
        ParseResult parseResult;
        Result indexResult;

        log.info("Uploading file {} to {} location.", fileName, path);

        try {
            // folder should already exists
            FutureTask<Result> fileSaveResultFuture = runFileSaveThread(file, filePath);

            parseResult = parserService.parse(file);

            saveResult = fileSaveResultFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error occurred while saving file {} to {} location. Stack trace: ", fileName, e.getStackTrace());
            return new Result(ResultType.FAILURE, e);
        }

        if (saveResult.isFailure()) {
            log.error("Save for file {} failed.", fileName);
            return saveResult;
        }

        if (parseResult.isFailure()) {
            log.error("Parse for file {} failed.", fileName);
            return parseResult;
        }

        indexResult = indexerService.indexDocument(parseResult.getParsedFile());

        if (indexResult.isFailure()) {
            log.error("Indexing for file {} failed.", fileName);
            return indexResult;
        }

        log.info("Successfully uploaded file {}.", fileName);
        return new Result(ResultType.SUCCESS);
    }

    private FutureTask<Result> runFileSaveThread(MultipartFile file, Path path) {
        FileSaver fileSaver = new FileSaver(file, path);
        FutureTask<Result> futureTask = new FutureTask<>(fileSaver);

        Thread thread = new Thread(futureTask);
        thread.start();

        return futureTask;
    }
}
