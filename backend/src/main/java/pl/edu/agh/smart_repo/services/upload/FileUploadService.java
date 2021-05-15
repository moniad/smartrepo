package pl.edu.agh.smart_repo.services.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.json.EscapeCharMapper;
import pl.edu.agh.smart_repo.common.response.Result;
import pl.edu.agh.smart_repo.common.response.ResultType;
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

    public Result processFile(MultipartFile file, String path) {
        //TODO: this part should be retrieved from frontend
        String fileName = file.getOriginalFilename();
        log.info("Started processing file: " + fileName);

        Path filePath = Paths.get(storagePath.toString(), path, fileName);

        File new_file = new File(filePath.toUri());

        try (FileOutputStream fos = new FileOutputStream(new_file)) {
            fos.write(file.getBytes());
        } catch (FileNotFoundException e) {
            log.error("Error: file cannot be created.");
            return new Result(ResultType.FAILURE, e);
        } catch (IOException e) {
            log.error("Error while saving file.");
            return new Result(ResultType.FAILURE, e);
        }

        String parsed = parserService.parse(new_file, Paths.get(path, fileName).toString());

        if (parsed == null) {
            return new Result(ResultType.FAILURE, "Failed to parse file.");
        }

        parsed = escapeCharMapper.mapAll(parsed).trim();

        DocumentStructure documentStructure = new DocumentStructure();

        //TODO retrieve remaining arguments from frontend`s request
        documentStructure.setName(fileName);
        documentStructure.setPath(filePath.toString());
        documentStructure.setContents(parsed);

        String currentTimestamp = String.valueOf((int) System.currentTimeMillis() / 1000);
        documentStructure.setCreationDate(currentTimestamp); //todo
        documentStructure.setModificationDate(currentTimestamp); //todo

        return indexerService.indexDocument(documentStructure);
    }
}
