package pl.edu.agh.smart_repo.services.upload;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import https.agh_edu_pl.smart_repo.file_extension_service.Extension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.json.EscapeCharMapper;
import pl.edu.agh.smart_repo.common.response.Result;
import pl.edu.agh.smart_repo.common.response.ResultType;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.file_extension.FileExtensionService;
import pl.edu.agh.smart_repo.services.index.IndexerService;
import pl.edu.agh.smart_repo.services.parse.ParserService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileUploadService {
    private final Path storagePath;
    private final EscapeCharMapper escapeCharMapper;
    private final Gson gson;

    private final ParserService parserService;
    private final IndexerService indexerService;
    private final FileExtensionService fileExtensionService;


    @Autowired
    public FileUploadService(ConfigurationFactory configurationFactory, ParserService parserService, IndexerService indexerService, FileExtensionService fileExtensionService) {
        storagePath = configurationFactory.getStoragePath();
        escapeCharMapper = new EscapeCharMapper();
        this.gson = new Gson();

        this.parserService = parserService;
        this.indexerService = indexerService;
        this.fileExtensionService = fileExtensionService;
    }

    private Result sendDocumentStructureToIndexService(String fileName, String filePath, String parsed)
    {

        fileName = escapeCharMapper.mapAll(fileName).trim();
        filePath = escapeCharMapper.mapAll(filePath).trim();
        parsed = escapeCharMapper.mapAll(parsed).trim();

        DocumentStructure documentStructure = new DocumentStructure();

        //TODO retrieve remaining arguments from frontend`s request
        documentStructure.setName(fileName);
        documentStructure.setPath(filePath);
        documentStructure.setContents(parsed);

        String currentTimestamp = String.valueOf((int) System.currentTimeMillis() / 1000);
        documentStructure.setCreationDate(currentTimestamp); //todo
        documentStructure.setModificationDate(currentTimestamp); //todo

        return indexerService.indexDocument(documentStructure);
    }

    public Result processFile(MultipartFile file, String path) {
        //TODO: this part should be retrieved from frontend
        String fileName = file.getOriginalFilename();
        log.info("Started processing file: " + fileName);

        Path filePath = Paths.get(storagePath.toString(), path, fileName);

        File newFile = new File(filePath.toUri());

        if (newFile.exists() && !newFile.isDirectory()) {
            log.error("Error: file already exists.");
            String errorMsg = "Error: File \"" + fileName + "\" already exists in this directory.";
            return new Result(ResultType.FAILURE, errorMsg, new FileAlreadyExistsException(fileName));
        }

        try (FileOutputStream fos = new FileOutputStream(newFile)) {
            fos.write(file.getBytes());
        } catch (FileNotFoundException e) {
            log.error("Error: file cannot be created.");
            return new Result(ResultType.FATAL_FAILURE, e);
        } catch (IOException e) {
            log.error("Error while saving file.");
            return new Result(ResultType.FATAL_FAILURE, e);
        }

        String parsed = parserService.parse(newFile, Paths.get(path, fileName).toString());

        if (parsed == null) {
            return new Result(ResultType.FATAL_FAILURE, "Failed to parse file.");
        }

        Result result = null;

        Extension extension = fileExtensionService.getStoredFileExtension(filePath);

        if (extension == Extension.TAR || extension == Extension.ZIP || extension == Extension.GZ) {

            JsonArray jsonArray = gson.fromJson(parsed, JsonArray.class);
            System.out.println(jsonArray.toString());

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                String parsed_name = jsonObject.get("name").getAsString();
                String parsed_path = jsonObject.get("path").getAsString();
                String parsed_content = jsonObject.get("content").getAsString();

                result = sendDocumentStructureToIndexService(parsed_name, parsed_path, parsed_content);
            }
        }
        else
        {
            result = sendDocumentStructureToIndexService(fileName, filePath.toString(), parsed);
        }

        return result;
    }
}
