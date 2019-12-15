package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum RelatertYtelseBehandlingstema implements Kodeverdi {

    FORELDREPENGER_FODSEL_BEHANDLINGSTEMA("FØ"),
    FORELDREPENGER_BEHANDLINGSTEMA("FP"),
    FORELDREPENGER_ADOPSJON_BEHANDLINGSTEMA("AP"),
    FORELDREPENGER_FODSEL_UTLAND_BEHANDLINGSTEMA("FU"),
    SVANGERSKAPSPENGER_BEHANDLINGSTEMA("SV"),
    ENGANGSSTONAD_ADOPSJON_BEHANDLINGSTEMA("AE"),
    ENGANGSSTONAD_FODSEL_BEHANDLINGSTEMA("FE"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    public static final String KODEVERK = "RELATERT_YTELSE_BEH_TEMA";

    private static final Map<String, RelatertYtelseBehandlingstema> KODER = new LinkedHashMap<>();

    private String kode;

    RelatertYtelseBehandlingstema() {
        // Hibernate trenger den
    }

    private RelatertYtelseBehandlingstema(String kode) {
        this.kode = kode;
    }

    @JsonCreator
    public static RelatertYtelseBehandlingstema fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Fagsystem: " + kode);
        }
        return ad;
    }

    public static RelatertYtelseBehandlingstema fraKodeDefaultUdefinert(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return  KODER.getOrDefault(kode, UDEFINERT);
    }

    public static Map<String, RelatertYtelseBehandlingstema> kodeMap() {
        return Collections.unmodifiableMap(KODER);
    }

    public static void main(String[] args) {
        System.out.println(KODER.keySet());
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

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    private static final List<RelatertYtelseBehandlingstema> FORELDREPENGER_BEHANDLINGSTEMAER = List.of(
            FORELDREPENGER_BEHANDLINGSTEMA,
            FORELDREPENGER_FODSEL_BEHANDLINGSTEMA,
            FORELDREPENGER_ADOPSJON_BEHANDLINGSTEMA,
            FORELDREPENGER_FODSEL_UTLAND_BEHANDLINGSTEMA);

    private static final List<RelatertYtelseBehandlingstema> ENGANGSSTONAD_BEHANDLINGSTEMAER = List.of(
            ENGANGSSTONAD_ADOPSJON_BEHANDLINGSTEMA,
            ENGANGSSTONAD_FODSEL_BEHANDLINGSTEMA);

    public static boolean erGjelderEngangsstonad(String behandlingsTema) {
        return ENGANGSSTONAD_BEHANDLINGSTEMAER.contains(fraKodeDefaultUdefinert(behandlingsTema));
    }

    public static boolean erGjelderSvangerskapspenger(String behandlingsTema) {
        return SVANGERSKAPSPENGER_BEHANDLINGSTEMA.equals(fraKodeDefaultUdefinert(behandlingsTema));
    }

    public static boolean erGjelderForeldrepenger(String behandlingsTema) {
        return FORELDREPENGER_BEHANDLINGSTEMAER.contains(fraKodeDefaultUdefinert(behandlingsTema));
    }

}
