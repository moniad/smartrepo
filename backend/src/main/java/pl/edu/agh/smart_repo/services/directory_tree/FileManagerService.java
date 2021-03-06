package pl.edu.agh.smart_repo.services.directory_tree;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.response.Result;
import pl.edu.agh.smart_repo.common.response.ResultType;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.directory_tree.util.MagicObjectControllerService;
import pl.edu.agh.smart_repo.services.index.IndexerService;

import java.io.IOException;
import java.nio.file.*;

@Slf4j
@Service
public class FileManagerService {
    private final Path userFilesDirectoryPath;
    private final IndexerService indexerService;
    private final MagicObjectControllerService magicObjectController;

    public FileManagerService(ConfigurationFactory configurationFactory, IndexerService indexerService, MagicObjectControllerService magicObjectController) {
        this.userFilesDirectoryPath = configurationFactory.getStoragePath();
        this.indexerService = indexerService;
        this.magicObjectController = magicObjectController;
    }

    public Result createDirectory(String path) {
        Path resultPath = Paths.get(userFilesDirectoryPath.toString(), path);
        String message = String.format("Directory %s created successfully", path);
        try {
            Files.createDirectory(resultPath);
            magicObjectController.processObjectCreation(path);
        } catch (FileAlreadyExistsException e) {
            message = String.format("Directory %s already exists", path);
            log.error(message, e);
            return new Result(ResultType.FATAL_FAILURE, message);
        } catch (IOException e) {
            message = "Cannot create directory: " + path;
            log.error(message, e);
            return new Result(ResultType.FATAL_FAILURE, message);
        }
        return new Result(ResultType.SUCCESS, message);
    }

    public Result deleteFile(String path) {
        Path resultPath = Paths.get(userFilesDirectoryPath.toString(), path);
        String message;
        Result result;
        try {
            Files.delete(resultPath);
            magicObjectController.processObjectDeletion(path);

            DocumentStructure document = new DocumentStructure();
            document.setPath(resultPath.toString());
            result = indexerService.deleteFileFromIndex(document);

        } catch (NoSuchFileException e) {
            message = String.format("File %s does not exists", path);
            log.error(message, e);
            return new Result(ResultType.FATAL_FAILURE, message);
        } catch (DirectoryNotEmptyException e) {
            message = String.format("Directory %s is not empty", path);
            log.error(message, e);
            return new Result(ResultType.FATAL_FAILURE, message);
        } catch (IOException e) {
            message = "Cannot delete file: " + path;
            log.error(message, e);
            return new Result(ResultType.FATAL_FAILURE, message);
        }
        return result;
    }
}
