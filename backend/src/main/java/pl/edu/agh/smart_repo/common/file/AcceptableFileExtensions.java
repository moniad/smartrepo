package pl.edu.agh.smart_repo.common.file;

public enum AcceptableFileExtensions {
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
    jpg("jpg"),
    png("png"),
    mp4("mp4");

    private final String s;

    AcceptableFileExtensions(String s)
    {
        this.s = s;
    }

    @Override
    public String toString()
    {
        return s;
    }
}
