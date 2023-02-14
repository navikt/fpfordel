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

    private static final Set<ArkivFilType> KLARTEKST = Set.of(XML, JSON);

    ArkivFilType() {
        // Hibernate trenger den
    }

    public static boolean erKlartekstType(ArkivFilType arkivFilType) {
        return KLARTEKST.contains(arkivFilType);
    }

}
