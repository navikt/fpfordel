package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.Collections;
import java.util.HashSet;
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

    public static final ArkivFilType UDEFINERT = new ArkivFilType("-"); //$NON-NLS-1$

    public static final Set<ArkivFilType> KLARTEKST = Collections.unmodifiableSet(getKlartekstSet());
    public static final Set<ArkivFilType> BINÆR = Collections.unmodifiableSet(getBinærSet());

    public ArkivFilType() {
        // For Hibernate
    }

    public ArkivFilType(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static boolean erKlartekstType(ArkivFilType arkivFilType) {
        return KLARTEKST.contains(arkivFilType);
    }

    private static Set<ArkivFilType> getBinærSet() {
        Set<ArkivFilType> tmp = new HashSet<>();
        tmp.add(PDF);
        tmp.add(PDFA);
        tmp.add(AFP);
        tmp.add(AXML);
        tmp.add(DLF);
        tmp.add(DOC);
        tmp.add(DOCX);
        tmp.add(JPEG);
        tmp.add(RTF);
        tmp.add(TIFF);
        tmp.add(XLS);
        tmp.add(XLSX);
        return tmp;
    }

    private static Set<ArkivFilType> getKlartekstSet() {
        Set<ArkivFilType> tmp = new HashSet<>();
        tmp.add(XML);
        return tmp;
    }
}
