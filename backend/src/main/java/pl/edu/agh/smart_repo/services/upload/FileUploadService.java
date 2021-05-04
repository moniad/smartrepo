package pl.edu.agh.smart_repo.services.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.json.EscapeCharMapper;
import pl.edu.agh.smart_repo.common.results.Result;
import pl.edu.agh.smart_repo.common.results.ResultType;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.index.IndexerService;
import pl.edu.agh.smart_repo.services.parse.ParserService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileUploadService {
    private final Path storagePath;
    private final EscapeCharMapper escapeCharMapper;
    private final ParserService parserService;
    private final IndexerService indexerService;

    @Autowired
    public FileUploadService(ConfigurationFactory configurationFactory, ParserService parserService, IndexerService indexerService) {
        storagePath = configurationFactory.getStoragePath();
        escapeCharMapper = new EscapeCharMapper();
        this.parserService = parserService;
        this.indexerService = indexerService;
    }

    public Result processFile(MultipartFile file) {
        //TODO: this part should be retrieved from frontend
        String pathRelativeToStorage = file.getOriginalFilename();
        log.info("Start processing file: " + pathRelativeToStorage);

        Path filePath = Paths.get(storagePath.toString(), pathRelativeToStorage);

        File newFile = new File(filePath.toUri());

        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            fos.write(file.getBytes());
        } catch (FileNotFoundException e) {
            log.error("Error: file cannot be created.");
            return new Result(ResultType.FAILURE, e);
        } catch (IOException e) {
            log.error("Error while saving file.");
            return new Result(ResultType.FAILURE, e);
        }

        String parsed = parserService.parse(newFile, pathRelativeToStorage);

        if (parsed == null) {
            return new Result(ResultType.FAILURE, "Failed to parse file.");
        }

        parsed = escapeCharMapper.mapAll(parsed).trim();

        DocumentStructure documentStructure = new DocumentStructure();

        //TODO retrieve remaining arguments from frontend`s request
        documentStructure.setName(pathRelativeToStorage);
        documentStructure.setContents(parsed);

        return indexerService.indexDocument(documentStructure);
    }
}
