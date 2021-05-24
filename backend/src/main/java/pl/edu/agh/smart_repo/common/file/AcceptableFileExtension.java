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
    wav("wav");

    private final String name;

    AcceptableFileExtension(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
