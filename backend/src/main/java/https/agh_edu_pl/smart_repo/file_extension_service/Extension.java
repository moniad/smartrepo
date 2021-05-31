//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.05.31 at 09:33:34 PM CEST 
//


package https.agh_edu_pl.smart_repo.file_extension_service;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for extension.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="extension">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="pdf"/>
 *     &lt;enumeration value="xlsx"/>
 *     &lt;enumeration value="xls"/>
 *     &lt;enumeration value="txt"/>
 *     &lt;enumeration value="doc"/>
 *     &lt;enumeration value="docx"/>
 *     &lt;enumeration value="pptx"/>
 *     &lt;enumeration value="ppt"/>
 *     &lt;enumeration value="odt"/>
 *     &lt;enumeration value="ods"/>
 *     &lt;enumeration value="odg"/>
 *     &lt;enumeration value="zip"/>
 *     &lt;enumeration value="tar"/>
 *     &lt;enumeration value="gz"/>
 *     &lt;enumeration value="wav"/>
 *     &lt;enumeration value="mp3"/>
 *     &lt;enumeration value="ogg"/>
 *     &lt;enumeration value="flac"/>
 *     &lt;enumeration value="aac"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "extension")
@XmlEnum
public enum Extension {

    @XmlEnumValue("pdf")
    PDF("pdf"),
    @XmlEnumValue("xlsx")
    XLSX("xlsx"),
    @XmlEnumValue("xls")
    XLS("xls"),
    @XmlEnumValue("txt")
    TXT("txt"),
    @XmlEnumValue("doc")
    DOC("doc"),
    @XmlEnumValue("docx")
    DOCX("docx"),
    @XmlEnumValue("pptx")
    PPTX("pptx"),
    @XmlEnumValue("ppt")
    PPT("ppt"),
    @XmlEnumValue("odt")
    ODT("odt"),
    @XmlEnumValue("ods")
    ODS("ods"),
    @XmlEnumValue("odg")
    ODG("odg"),
    @XmlEnumValue("zip")
    ZIP("zip"),
    @XmlEnumValue("tar")
    TAR("tar"),
    @XmlEnumValue("gz")
    GZ("gz"),
    @XmlEnumValue("wav")
    WAV("wav"),
    @XmlEnumValue("mp3")
    MP_3("mp3"),
    @XmlEnumValue("ogg")
    OGG("ogg"),
    @XmlEnumValue("flac")
    FLAC("flac"),
    @XmlEnumValue("aac")
    AAC("aac");
    private final String value;

    Extension(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Extension fromValue(String v) {
        for (Extension c: Extension.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
