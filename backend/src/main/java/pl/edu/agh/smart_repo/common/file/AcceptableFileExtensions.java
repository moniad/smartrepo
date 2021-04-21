package pl.edu.agh.smart_repo.common.file;

public enum AcceptableFileExtensions {
    pdf("pdf"),
    xlsx("xlsx"),
    xls("xls"),
    txt("txt"),
    doc("doc"),
    docx("docx"),
    pptx("pptx"),
    odt("odt"),
    ods("ods"),
    odg("odg");

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
