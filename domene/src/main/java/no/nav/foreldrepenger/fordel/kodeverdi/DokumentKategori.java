package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DokumentKategori implements Kodeverdi {

    BRV("BRV", "B"),
    EDIALOG("EDIALOG", "ELEKTRONISK_DIALOG"),
    ELEKTRONISK_SKJEMA("ESKJ", "ES"),
    FNOT("FNOT", "FORVALTNINGSNOTAT"),
    IBRV("IBRV", "IB"),
    IKKE_TOLKBART_SKJEMA("ITSKJ", "IS"),
    KLAGE_ELLER_ANKE("KLGA", "KA"),
    KONVEARK("KONVEARK", "KD"),
    KONVSYS("KONVSYS", "KS"),
    PUBEOS("PUBEOS", "PUBL_BLANKETT_EOS"),
    SEDOK("SEDOK", "SED"),
    SØKNAD("SOKN", "SOK"),
    TSKJ("TSKJ", "TS"),
    VBRV("VBRV", "VB"),

    UDEFINERT("-", null),
    ;

    private static final Map<String, DokumentKategori> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private String kode;

    private String offisiellKode;

    private DokumentKategori(String kode, String offisiellKode) {
        this.kode = kode;
        this.offisiellKode = offisiellKode;
    }

    public static DokumentKategori fraKodeDefaultUdefinert(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return KODER.getOrDefault(kode, UDEFINERT);
    }

    @Override
    public String getKode() {
        return kode;
    }

    public String getOffisiellKode() {
        return offisiellKode;
    }
}