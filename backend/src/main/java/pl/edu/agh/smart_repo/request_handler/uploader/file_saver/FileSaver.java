package pl.edu.agh.smart_repo.request_handler.uploader.file_saver;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.Callable;


@Slf4j
public class FileSaver implements Callable<Result> {
    private Logger logger = LoggerFactory.getLogger(FileSaver.class);

    //TODO: This needs to be changed to a proper directory on server
    private Path targetFileLocation;
    private File file;

    public FileSaver(File file, Path targetFileLocation) {
        this.file = file;
        this.targetFileLocation = targetFileLocation;
    }

    @Override
    public Result call() {
        logger.info("Saving file {} to {}.", file.getName(), targetFileLocation);

        FileInputStream fis;
        byte[] buf = new byte[1024];

        try (FileOutputStream fos = new FileOutputStream(targetFileLocation.toString())) {
            fis = new FileInputStream(this.file);

            int hasRead = 0;
            while ((hasRead = fis.read(buf)) > 0) {
                fos.write(buf, 0, hasRead);
            }

            fis.close();
        } catch (IOException e) {
            logger.error("Error occured while saving file {} to {} location. Messege: {}",
                    file.getName(), targetFileLocation, e.getMessage());

            return new Result(ResultType.FAILURE, e);
        }

        return new Result(ResultType.SUCCESS);
    }
}