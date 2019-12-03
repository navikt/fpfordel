package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.Collections;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "ArkivFilType")
@DiscriminatorValue(ArkivFilType.DISCRIMINATOR)
public class ArkivFilType extends Kodeliste {

    public static final String DISCRIMINATOR = "ARKIV_FILTYPE";

    public static final ArkivFilType PDF = new ArkivFilType("PDF");
    public static final ArkivFilType PDFA = new ArkivFilType("PDFA");
    public static final ArkivFilType XML = new ArkivFilType("XML");
    public static final ArkivFilType AFP = new ArkivFilType("AFP");
    public static final ArkivFilType AXML = new ArkivFilType("AXML");
    public static final ArkivFilType DLF = new ArkivFilType("DLF");
    public static final ArkivFilType DOC = new ArkivFilType("DOC");
    public static final ArkivFilType DOCX = new ArkivFilType("DOCX");
    public static final ArkivFilType JPEG = new ArkivFilType("JPEG");
    public static final ArkivFilType RTF = new ArkivFilType("RTF");
    public static final ArkivFilType TIFF = new ArkivFilType("TIFF");
    public static final ArkivFilType XLS = new ArkivFilType("XLS");
    public static final ArkivFilType XLSX = new ArkivFilType("XLSX");

    public static final ArkivFilType UDEFINERT = new ArkivFilType("-");

    public static final Set<ArkivFilType> KLARTEKST = Collections.unmodifiableSet(getKlartekstSet());
    public static final Set<ArkivFilType> BINÆR = Collections.unmodifiableSet(getBinærSet());

    public ArkivFilType() {
    }

    public ArkivFilType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static boolean erKlartekstType(ArkivFilType arkivFilType) {
        return KLARTEKST.contains(arkivFilType);
    }

    private static Set<ArkivFilType> getBinærSet() {
        return Set.of(PDF, PDFA, AFP, AXML, DLF, DOC, DOCX, JPEG, RTF, TIFF, XLS, XLSX);
    }

    private static Set<ArkivFilType> getKlartekstSet() {
        return Set.of(XML);
    }
}
