package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
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

    public static final String KODEVERK = "ARKIV_FILTYPE";

    private static final Map<String, ArkivFilType> KODER = new LinkedHashMap<>();

    private String kode;

    ArkivFilType() {
        // Hibernate trenger den
    }

    private ArkivFilType(String kode) {
        this.kode = kode;
    }

    @JsonCreator
    public static ArkivFilType fraKode(@JsonProperty("kode") String kode) {

        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Fagsystem: " + kode);
        }
        return ad;
    }

    public static ArkivFilType fraKodeDefaultUdefinert(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return KODER.getOrDefault(kode, UDEFINERT);
    }

    @JsonProperty
    @Override
    public String getKodeverk() {
        return KODEVERK;
    }

    @JsonProperty
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
    }

}
