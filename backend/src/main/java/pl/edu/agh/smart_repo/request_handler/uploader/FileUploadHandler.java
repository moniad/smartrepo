package pl.edu.agh.smart_repo.request_handler.uploader;

import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.ConfigurationFactory;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;
import pl.edu.agh.smart_repo.request_handler.uploader.file_saver.FileSaver;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

@Service
public class FileUploadHandler {
    private final Path filesCatalogPath;

    public FileUploadHandler(ConfigurationFactory configurationFactory) {
        this.filesCatalogPath = configurationFactory.getFileCatalogPath();
    }

    public Result processFile(File file, String path) {
        Path filePath = Paths.get(filesCatalogPath.toString(), path, file.getName());
        Result result;

        try {
            // folder should already exists
            FutureTask<Result> fileSaveResultFuture = runFileSaveThread(file, filePath);

            // Do other stuff

            result = fileSaveResultFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();

            return new Result(ResultType.FAILURE, e);
        }

        return result;
    }

    private FutureTask<Result> runFileSaveThread(File file, Path path) {
        FileSaver fileSaver = new FileSaver(file, path);
        FutureTask<Result> futureTask = new FutureTask<>(fileSaver);

        Thread thread = new Thread(futureTask);
        thread.start();

        return futureTask;
    }
}
