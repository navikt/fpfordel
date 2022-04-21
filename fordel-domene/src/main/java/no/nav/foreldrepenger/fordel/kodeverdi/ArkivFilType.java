package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.Set;

public enum ArkivFilType {

    PDF,
    PDFA,
    XML,
    JSON,
    AXML,
    AFP,
    DLF,
    DOC,
    DOCX,
    RTF,
    XLS,
    XLSX,
    JPEG,
    JPG,
    PNG,
    TIF,
    TIFF,
    ;

    ArkivFilType() {
        // Hibernate trenger den
    }

    private static final Set<ArkivFilType> KLARTEKST = Set.of(XML, JSON);

    public static boolean erKlartekstType(ArkivFilType arkivFilType) {
        return KLARTEKST.contains(arkivFilType);
    }

}
