package pl.edu.agh.smart_repo.services.upload;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import https.agh_edu_pl.smart_repo.file_extension_service.Extension;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;
import pl.edu.agh.smart_repo.common.json.EscapeCharMapper;
import pl.edu.agh.smart_repo.common.response.Result;
import pl.edu.agh.smart_repo.common.response.ResultType;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;
import pl.edu.agh.smart_repo.services.directory_tree.util.MagicObjectControllerService;
import pl.edu.agh.smart_repo.services.file_extension.FileExtensionService;
import pl.edu.agh.smart_repo.services.index.IndexerService;
import pl.edu.agh.smart_repo.services.parse.ParserService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Service
public class FileUploadService {
    private final Path storagePath;
    private final EscapeCharMapper escapeCharMapper;
    private final Gson gson;

    private final ParserService parserService;
    private final IndexerService indexerService;
    private final FileExtensionService fileExtensionService;
    private final MagicObjectControllerService magicObjectController;


    @Autowired
    public FileUploadService(ConfigurationFactory configurationFactory, ParserService parserService, IndexerService indexerService, FileExtensionService fileExtensionService, MagicObjectControllerService magicObjectController) {
        storagePath = configurationFactory.getStoragePath();
        escapeCharMapper = new EscapeCharMapper();
        this.gson = new Gson();

        this.parserService = parserService;
        this.indexerService = indexerService;
        this.fileExtensionService = fileExtensionService;
        this.magicObjectController = magicObjectController;
    }

    private Result sendDocumentStructureToIndexService(String fileName, Extension extension, String filePathIndex,
                                                       String filePathFileSystem, String parsed)
    {
        fileName = escapeCharMapper.mapAll(fileName).trim();
        filePathIndex = escapeCharMapper.mapAll(filePathIndex).trim();
        parsed = escapeCharMapper.mapAll(parsed).trim();

        Path absoluteFilePath = Paths.get(filePathFileSystem);
        
        DocumentStructure documentStructure = new DocumentStructure();

        //TODO retrieve remaining arguments from frontend`s request
        documentStructure.setName(fileName);
        documentStructure.setPath(filePathIndex);
        documentStructure.setExtension(extension.value());
        documentStructure.setContents(parsed);

        fillFileSystemRelatedAttributes(absoluteFilePath, documentStructure);

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
            magicObjectController.processObjectCreation(Paths.get(path, fileName).toString());
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
            log.info(jsonArray.toString());

            boolean first = true;
            String temp_path = null;

            for (JsonElement element : jsonArray) {

                JsonObject jsonObject = element.getAsJsonObject();
                String parsed_name = jsonObject.get("name").getAsString();
                String parsed_extension = jsonObject.get("extension").getAsString();
                String parsed_path = jsonObject.get("path").getAsString();
                String parsed_content = jsonObject.get("content").getAsString();

                //TODO temporary fix for finding file system related atributes
                // for inner files - remove them after indexing on backend

                //first element is always archive itself
                if (first)
                {
                    //TODO storage is added as a prefix of path. This method will not work when it`s changed.
                    ArrayList<String> path_parts = new ArrayList<>(Arrays.asList(parsed_path.split("/")));
                    path_parts.add(2, "__temp");
                    temp_path = String.join("/", path_parts);
                }

                String file_path_file_system = temp_path;
                if (!first)
                {
                    file_path_file_system += "/" + parsed_name;
                }

                first = false;

                result = sendDocumentStructureToIndexService(parsed_name, Extension.fromValue(parsed_extension),
                                                            parsed_path, file_path_file_system, parsed_content);
            }

            try {
                FileUtils.deleteDirectory(Paths.get(temp_path).toFile());
            } catch (IOException e) {
                log.error("Error: failed to delete temporary directory: " + temp_path);
            }
        }
        else
        {
            result = sendDocumentStructureToIndexService(fileName, extension, filePath.toString(),
                                                        filePath.toString(), parsed);
        }

        return result;
    }

    private void fillFileSystemRelatedAttributes(Path absoluteFilePath, DocumentStructure documentStructure) {
        try {
            BasicFileAttributes fileAttributes = Files.readAttributes(absoluteFilePath, BasicFileAttributes.class);
            documentStructure.setSize(Long.toString(fileAttributes.size()));
            documentStructure.setCreationDate(Long.toString(fileAttributes.creationTime().toMillis() / 1000));
            documentStructure.setModificationDate(Long.toString(fileAttributes.lastModifiedTime().toMillis() / 1000));
        } catch (IOException e) {
            log.error("Error: cannot get file system related attributed for file \"" + absoluteFilePath.toString() + "\".");
        }
    }
}
