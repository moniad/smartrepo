package pl.edu.agh.smart_repo.services.directory_tree;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

@Slf4j
@Service
public class FileManagerService {
    private final Path userFilesDirectoryPath;

    public FileManagerService(ConfigurationFactory configurationFactory) {
        this.userFilesDirectoryPath = configurationFactory.getStoragePath();
    }

    public Result createDirectory(String path){
        Path resultPath = Paths.get(userFilesDirectoryPath.toString(), path);
        var message = String.format("Directory %s created successfully", path);
        try {
            Files.createDirectory(resultPath);
        } catch (FileAlreadyExistsException e) {
            message = String.format("Directory %s already exists", path);
            log.error(message, e);
            return new Result(ResultType.FAILURE, message);
        } catch (IOException e) {
            message = "Cannot create directory: " + path;
            log.error(message, e);
            return new Result(ResultType.FAILURE, message);
        }
        return new Result(ResultType.SUCCESS, message);
    }

    public Result deleteFile(String path){
        Path resultPath = Paths.get(userFilesDirectoryPath.toString(), path);
        var message = String.format("File %s deleted successfully", path);
        try {
            Files.delete(resultPath);
        } catch (NoSuchFileException e) {
            message = String.format("File %s does not exists", path);
            log.error(message, e);
            return new Result(ResultType.FAILURE, message);
        } catch (DirectoryNotEmptyException e) {
            message = String.format("Directory %s is not empty", path);
            log.error(message, e);
            return new Result(ResultType.FAILURE, message);
        } catch (IOException e) {
            message = "Cannot delete file: " + path;
            log.error(message, e);
            return new Result(ResultType.FAILURE, message);
        }
        return  new Result(ResultType.SUCCESS, message);
    }

}
