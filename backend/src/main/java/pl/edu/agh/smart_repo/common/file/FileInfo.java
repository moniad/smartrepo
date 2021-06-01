package pl.edu.agh.smart_repo.common.file;

import lombok.Data;
import pl.edu.agh.smart_repo.common.document_fields.DocumentStructure;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class FileInfo {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");

    private String name;
    private String filePath;
    private String creationDate;
    private String modificationDate;
    private String extension;
    private String isDirectory;
    private String size;

    public FileInfo(String name, long creationDate, boolean isDirectory, long size) {
        this.name = name;
        Date creationDateDate = new Date(creationDate * 1000);
        this.creationDate = formatter.format(creationDateDate);
        this.isDirectory = isDirectory ? "DIR" : "FILE";
        this.size = Long.toString(size);
    }

    public FileInfo(DocumentStructure documentStructure) {
        this.name = documentStructure.getName();
        this.filePath = documentStructure.getPath();
        Date creationDateDate = new Date(Long.parseLong(documentStructure.getCreationDate()) * 1000);
        this.creationDate = formatter.format(creationDateDate);
        Date modificationDateDate = new Date(Long.parseLong(documentStructure.getModificationDate()) * 1000);
        this.modificationDate = formatter.format(modificationDateDate);
        this.extension = documentStructure.getExtension();
        this.isDirectory = "FILE";
        this.size = documentStructure.getSize();
    }

    public static FileInfo of(DocumentStructure documentStructure) {
        return new FileInfo(documentStructure);
    }
}
