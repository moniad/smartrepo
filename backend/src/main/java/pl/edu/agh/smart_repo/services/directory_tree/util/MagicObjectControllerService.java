package pl.edu.agh.smart_repo.services.directory_tree.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.edu.agh.smart_repo.configuration.ConfigurationFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Service
public class MagicObjectControllerService {

    public static final String nonMagicObjectMarker = ".__non_magic__";
    private final String storagePath;
    private final ArrayList<String> magicObjectsMarkers =
            new ArrayList<String>(Arrays.asList(nonMagicObjectMarker, "tmp", "temp"));

    public MagicObjectControllerService(ConfigurationFactory configurationFactory) {
        this.storagePath = configurationFactory.getStoragePath().toString();
    }

    public void processObjectCreation(String objectPath) {
        Path absolutePath = Paths.get(storagePath, objectPath);
        if (!isMagicObjectPath(absolutePath)) {
            return;
        }
        try {
            Files.createFile(getNonMagicObjectMarkerPath(absolutePath));
        } catch (IOException e) {
            log.error("Error: cannot stat non magic object marker. Permission denied.");
        }
    }

    public void processObjectDeletion(String objectPath) {
        Path absolutePath = Paths.get(storagePath, objectPath);
        Path markerPath = getNonMagicObjectMarkerPath(absolutePath);
        if (isMagicObjectPath(absolutePath) && checkIfNonMagicObjectMarkerExists(absolutePath)) {
            File markerFile = new File(markerPath.toUri());
            if (!markerFile.delete()) {
                log.error("Error: cannot delete non magic object marker. Permission denied.");
            }
        }
    }

    public boolean isNonMagicObject(Path objectAbsolutePath) {
        String objectName = objectAbsolutePath.getFileName().toString();
        return !isMagicObjectPath(objectAbsolutePath) || checkIfNonMagicObjectMarkerExists(objectAbsolutePath);
    }

    private boolean checkIfNonMagicObjectMarkerExists(Path objectAbsolutePath) {
        File markerFile = new File(getNonMagicObjectMarkerPath(objectAbsolutePath).toUri());
        return markerFile.exists();
    }

    private boolean isMagicObjectPath(Path objectAbsolutePath) {
        String objectName = objectAbsolutePath.getFileName().toString();
        return magicObjectsMarkers.stream().anyMatch(objectName::contains);
    }

    private Path getNonMagicObjectMarkerPath(Path objectAbsolutePath) {
        return Paths.get(objectAbsolutePath.toString() + nonMagicObjectMarker);
    }
}
