package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum DokumentKategori implements KodeverdiMedIntern {

    BRV("BRV", "B"),
    EDIALOG("EDIALOG", "ELEKTRONISK_DIALOG"),
    ELEKTRONISK_SKJEMA("ESKJ", "ES"),
    FNOT("FNOT", "FORVALTNINGSNOTAT"),
    IBRV("IBRV","IB"),
    IKKE_TOLKBART_SKJEMA("ITSKJ", "IS"),
    KLAGE_ELLER_ANKE("KLGA","KA"),
    KONVEARK("KONVEARK", "KD"),
    KONVSYS("KONVSYS","KS"),
    PUBEOS("PUBEOS","PUBL_BLANKETT_EOS"),
    SEDOK("SEDOK","SED"),
    SØKNAD("SOKN","SOK"),
    TSKJ("TSKJ","TS"),
    VBRV("VBRV","VB"),

    UDEFINERT("-", null),
    ;

    private static final Map<String, DokumentKategori> KODER = new LinkedHashMap<>();
    private static final Map<String, DokumentKategori> OFFISIELLE_KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "DOKUMENT_KATEGORI";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
            if (v.offisiellKode != null) {
                OFFISIELLE_KODER.putIfAbsent(v.offisiellKode, v);
            }
        }
    }

    private String kode;

    @JsonIgnore
    private String offisiellKode;

    private DokumentKategori(String kode, String offisiellKode) {
        this.kode = kode;
        this.offisiellKode = offisiellKode;
    }

    @JsonCreator
    public static DokumentKategori fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Tema: " + kode);
        }
        return ad;
    }

    public static DokumentKategori fraKodeDefaultUdefinert(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return KODER.getOrDefault(kode, UDEFINERT);
    }

    public static Map<String, DokumentKategori> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static DokumentKategori fraOffisiellKode(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return OFFISIELLE_KODER.getOrDefault(kode, UDEFINERT);
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
    
    @Override
    public String getOffisiellKode() {
        return offisiellKode;
    }


    public static void main(String[] args) {
        System.out.println(KODER.keySet());
    }
}