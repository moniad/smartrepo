package pl.edu.agh.smart_repo.file_upload.file_saver;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.Callable;


@Slf4j
public class FileSaver implements Callable<Result> {
    private Logger log = LoggerFactory.getLogger(FileSaver.class);

    private Path targetFileLocation;
    private MultipartFile file;

    public FileSaver(MultipartFile file, Path targetFileLocation) {
        this.file = file;
        this.targetFileLocation = targetFileLocation;
    }

    @Override
    public Result call() {
        log.info("Saving file {} to {}.", file.getName(), targetFileLocation);

        try (FileOutputStream fos = new FileOutputStream(targetFileLocation.toString())) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            log.error("Error occurred while saving file {} to {} location. Messege: {}",
                    file.getName(), targetFileLocation, e.getMessage());

            return new Result(ResultType.FAILURE, e);
        }

        log.info("Successfully saved file {}.", file.getName());
        return new Result(ResultType.SUCCESS);
    }
}