package pl.edu.agh.smart_repo.service;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.List;

@Service
public class FileTreeFetcherService {

    public List<File> fetchFileTree(String directoryPath, boolean recursive, String[] extensions){

        List<File> files = (List<File>) FileUtils.listFiles(new File(directoryPath), extensions, recursive);
        return files;
    }
}
