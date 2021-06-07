package pl.edu.agh.smart_repo.services.file_extension;

import https.agh_edu_pl.smart_repo.file_extension_service.GetFileExtensionRequest;
import https.agh_edu_pl.smart_repo.file_extension_service.GetFileExtensionResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;

import java.nio.file.Paths;

@Endpoint
public class FileExtensionCheckEndpoint {
    private static final String NAMESPACE_URI = "https://agh.edu.pl/smart-repo/file-extension-service";

    private final FileExtensionService fileExtensionService;
    private final String tempStoragePath;

    public FileExtensionCheckEndpoint(FileExtensionService fileExtensionService, ConfigurationFactory configurationFactory) {
        this.fileExtensionService = fileExtensionService;
        this.tempStoragePath = configurationFactory.getTempStoragePath().toString();
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getFileExtensionRequest")
    @ResponsePayload
    public GetFileExtensionResponse getFileExtension(@RequestPayload GetFileExtensionRequest request) {
        GetFileExtensionResponse response = new GetFileExtensionResponse();
        response.setExtension(fileExtensionService.getStoredFileExtension(Paths.get(tempStoragePath, request.getPath())));

        return response;
    }
}
