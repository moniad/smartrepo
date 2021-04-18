package pl.edu.agh.smart_repo.services.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import pl.edu.agh.smart_repo.services.parse.ParserService;

@Slf4j
@Service
public class FileUploadService {
    private final Path storagePath;

    @Autowired
    ParserService parserService;

    public FileUploadService(ConfigurationFactory configurationFactory)
    {
        storagePath = configurationFactory.getStoragePath();
    }

    public Result processFile(MultipartFile file) {
        //TODO: this part should be retrieved from frontend
        String path_relative_to_storage = file.getOriginalFilename();
        log.info("Start processing file: " + path_relative_to_storage);

        Path filePath = Paths.get(storagePath.toString(), path_relative_to_storage);

        File new_file = new File(filePath.toUri());

        try (FileOutputStream fos = new FileOutputStream(new_file)){
            fos.write(file.getBytes());
        } catch (FileNotFoundException e)
        {
            log.error("file couldn`t be created");
            return new Result(ResultType.FAILURE, e);
        }
        catch (IOException e) {
            log.error("error while saving file");
            return new Result(ResultType.FAILURE, e);
        }

        String parsed = parserService.parse(new_file, path_relative_to_storage);
        if (parsed == null)
        {
            return new Result(ResultType.FAILURE, "failed to parse file");
        }

        log.info("received parse response: '" + parsed + "'");

        //TODO send to indexing service there...
        return new Result(ResultType.SUCCESS);
    }
}
