package pl.edu.agh.smart_repo.common.file;

import lombok.Data;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;

@Data
public class FileInfo {
    private String name;
    private long creationDate;
    private String extension;
    private boolean isDirectory;
    private long size;

    public FileInfo(String name, long creationDate, boolean isDirectory, long size) {
        this.creationDate = creationDate;
        this.isDirectory = isDirectory;
        this.name = name;
        this.size = size;
    }

    public static FileInfo of(DocumentStructure documentStructure) {
        //todo: get more details: e.g. getFileByPath(documentStructure.getPath()); or add fields to DocumentStructure
        return new FileInfo(documentStructure.getName(), Long.parseLong(documentStructure.getCreationDate()), false, 0);
    }
}
