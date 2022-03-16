package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ArkivFilType implements Kodeverdi {

    PDF("PDF"),
    PDFA("PDFA"),
    XML("XML"),
    JSON("JSON"),
    AXML("AXML"),
    AFP("AFP"),
    DLF("DLF"),
    DOC("DOC"),
    DOCX("DOCX"),
    RTF("RTF"),
    XLS("XLS"),
    XLSX("XLSX"),
    JPEG("JPEG"),
    JPG("JPG"),
    PNG("PNG"),
    TIF("TIF"),
    TIFF("TIFF"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    private static final Map<String, ArkivFilType> KODER = new LinkedHashMap<>();

    @JsonValue
    private String kode;

    ArkivFilType() {
        // Hibernate trenger den
    }

    private ArkivFilType(String kode) {
        this.kode = kode;
    }


    @Override
    public String getKode() {
        return kode;
    }

    private static final Set<ArkivFilType> KLARTEKST = Set.of(XML, JSON);

    public static boolean erKlartekstType(ArkivFilType arkivFilType) {
        return KLARTEKST.contains(arkivFilType);
    }

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<ArkivFilType, String> {
        @Override
        public String convertToDatabaseColumn(ArkivFilType attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public ArkivFilType convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }

        private static ArkivFilType fraKode(String kode) {
            if (kode == null) {
                return null;
            }
            return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent ArkivFilType: " + kode));
        }

    }

}
