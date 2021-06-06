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
    private String size;
    private boolean isDirectory;

    public FileInfo(String name, long creationDate, long modificationDate, boolean isDirectory, long size) {
        this.name = name;
        Date creationDateDate = new Date(creationDate);
        this.creationDate = formatter.format(creationDateDate);
        Date modificationDateDate = new Date(modificationDate);
        this.modificationDate = formatter.format(modificationDateDate);
        this.isDirectory = isDirectory;
        this.size = getSizeStringRepr(size);
    }

    public FileInfo(DocumentStructure documentStructure) {
        this.name = documentStructure.getName();
        this.filePath = documentStructure.getPath() + "/";
        Date creationDateDate = new Date(Long.parseLong(documentStructure.getCreationDate()) * 1000);
        this.creationDate = formatter.format(creationDateDate);
        Date modificationDateDate = new Date(Long.parseLong(documentStructure.getModificationDate()) * 1000);
        this.modificationDate = formatter.format(modificationDateDate);
        this.extension = documentStructure.getExtension();
        this.isDirectory = false;
        this.size = getSizeStringRepr(Long.parseLong(documentStructure.getSize()));
    }

    public static FileInfo of(DocumentStructure documentStructure) {
        return new FileInfo(documentStructure);
    }

    private static String getSizeStringRepr(long size) {
        float sizeFloat = size;
        int factor = 0;
        String resultStr = Long.toString(size) + " [";
        while (sizeFloat > 1024) {
            factor++;
            sizeFloat /= 1024;
        }
        if (factor == 0)
            resultStr += Long.toString(size) + " ";
        else
            resultStr += String.format("%.2f ", sizeFloat);
        switch (factor) {
            case 0:
                resultStr += "B";
                break;
            case 1:
                resultStr += "KB";
                break;
            case 2:
                resultStr += "MB";
                break;
            case 3:
                resultStr += "GB";
                break;
        }
        resultStr += "]";
        return resultStr;
    }
}
