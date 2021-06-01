package pl.edu.agh.smart_repo.common.file;

public enum AcceptableFileExtension {
    pdf("pdf"),
    xlsx("xlsx"),
    xls("xls"),
    txt("txt"),
    doc("doc"),
    docx("docx"),
    ppt("ppt"),
    pptx("pptx"),
    odt("odt"),
    ods("ods"),
    odg("odg"),
    zip("zip"),
    tar("tar"),
    gz("gz"),
    wav("wav"),
    flac("flac"),
    aac("aac"),
    ogg("ogg"),
    mp3("mp3"),
    jpg("jpg"),
    png("png"),
    mp4("mp4"),
    mov("mov"),
    wmv("wmv"),
    avi("avi"),
    mpeg("mpeg");

    private final String name;

    AcceptableFileExtension(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
