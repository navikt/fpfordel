package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

import no.nav.foreldrepenger.mottak.klient.FagsakYtelseTypeDto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum BehandlingTema implements Kodeverdi {

    ENGANGSSTØNAD("ENGST", "ab0327", "Engangsstønad"),
    ENGANGSSTØNAD_FØDSEL("ENGST_FODS", "ab0050", "Engangsstønad ved fødsel"),
    ENGANGSSTØNAD_ADOPSJON("ENGST_ADOP", "ab0027", "Engangsstønad ved adopsjon"),

    FORELDREPENGER("FORP", "ab0326", "Foreldrepenger"),
    FORELDREPENGER_FØDSEL("FORP_FODS", "ab0047", "Foreldrepenger ved fødsel"),
    FORELDREPENGER_ADOPSJON("FORP_ADOP", "ab0072", "Foreldrepenger ved adopsjon"),

    SVANGERSKAPSPENGER("SVP", "ab0126", "Svangerskapspenger"),

    OMS("OMS", "ab0271", "Omsorgspenger, Pleiepenger og opplæringspenger"),
    OMS_OMSORG("OMS_OMSORG", "ab0149", "Omsorgspenger"),
    OMS_OPP("OMS_OPP", "ab0141", "Opplæringspenger"),
    OMS_PLEIE_BARN("OMS_PLEIE_BARN", "ab0069", "Pleiepenger sykt barn"),
    OMS_PLEIE_BARN_NY("OMS_PLEIE_BARN_NY", "ab0320", "Pleiepenger sykt barn ny ordning"),
    OMS_PLEIE_INSTU("OMS_PLEIE_INSTU", "ab0153", "Pleiepenger ved institusjonsopphold"),

    UDEFINERT("-", null, "Ikke definert"),
    ;

    private static final Map<String, BehandlingTema> KODER = new LinkedHashMap<>();
    private static final Map<String, BehandlingTema> OFFISIELLE_KODER = new LinkedHashMap<>();
    private static final Map<String, BehandlingTema> ALLE_TERMNAVN = new LinkedHashMap<>();
    private static final Set<BehandlingTema> ES_BT = Set.of(ENGANGSSTØNAD, ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_FØDSEL);
    private static final Set<BehandlingTema> FØDSEL_BT = Set.of(FORELDREPENGER_FØDSEL, ENGANGSSTØNAD_FØDSEL);
    private static final Set<BehandlingTema> ADOPSJON_BT = Set.of(FORELDREPENGER_ADOPSJON, ENGANGSSTØNAD_ADOPSJON);
    private static final Set<BehandlingTema> FP_BT = Set.of(FORELDREPENGER, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FØDSEL);
    private static final Set<BehandlingTema> UDEF_BT = Set.of(ENGANGSSTØNAD, FORELDREPENGER, UDEFINERT);

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
            if (v.offisiellKode != null) {
                OFFISIELLE_KODER.putIfAbsent(v.offisiellKode, v);
            }
            if (v.termnavn != null) {
                ALLE_TERMNAVN.putIfAbsent(v.termnavn, v);
            }
        }
    }

    @JsonValue
    private String kode;
    private String offisiellKode;
    private String termnavn;

    BehandlingTema(String kode, String offisiellKode, String termnavn) {
        this.kode = kode;
        this.offisiellKode = offisiellKode;
        this.termnavn = termnavn;
    }

    public static BehandlingTema fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        return Optional.ofNullable(KODER.get(kode)).orElseThrow(() -> new IllegalArgumentException("Ukjent Tema: " + kode));
    }

    public static BehandlingTema fraKodeDefaultUdefinert(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return KODER.getOrDefault(kode, UDEFINERT);
    }

    public static BehandlingTema fraOffisiellKode(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return OFFISIELLE_KODER.getOrDefault(kode, UDEFINERT);
    }

    public static BehandlingTema fraTermNavn(String termnavn) {
        if (termnavn == null) {
            return UDEFINERT;
        }
        return ALLE_TERMNAVN.getOrDefault(termnavn, UDEFINERT);
    }

    public static boolean gjelderEngangsstønad(BehandlingTema bt) {
        return ES_BT.contains(bt);
    }

    public static boolean gjelderForeldrepenger(BehandlingTema bt) {
        return FP_BT.contains(bt);
    }

    public static boolean gjelderFødsel(BehandlingTema behandlingTema) {
        return FØDSEL_BT.contains(behandlingTema);
    }

    public static boolean gjelderAdopsjon(BehandlingTema behandlingTema) {
        return ADOPSJON_BT.contains(behandlingTema);
    }

    public static boolean ikkeSpesifikkHendelse(BehandlingTema bt) {
        return UDEF_BT.contains(bt);
    }

    public static boolean gjelderSvangerskapspenger(BehandlingTema bt) {
        return SVANGERSKAPSPENGER.equals(bt);
    }

    public static BehandlingTema forYtelseUtenFamilieHendelse(BehandlingTema behandlingTema) {

        if (FP_BT.contains(behandlingTema)) {
            return FORELDREPENGER;
        }
        if (ES_BT.contains(behandlingTema)) {
            return ENGANGSSTØNAD;
        }
        return SVANGERSKAPSPENGER.equals(behandlingTema) ? behandlingTema : UDEFINERT;

    }

    @Override
    public String getKode() {
        return kode;
    }

    public String getOffisiellKode() {
        return offisiellKode;
    }

    public String getTermNavn() {
        return termnavn;
    }

    public FagsakYtelseTypeDto utledYtelseType() {
        if (BehandlingTema.gjelderForeldrepenger(this)) {
            return FagsakYtelseTypeDto.FORELDREPENGER;
        }
        if (BehandlingTema.gjelderEngangsstønad(this)) {
            return FagsakYtelseTypeDto.ENGANGSTØNAD;
        }
        if (BehandlingTema.gjelderSvangerskapspenger(this)) {
            return FagsakYtelseTypeDto.SVANGERSKAPSPENGER;
        }
        return null;
    }
}
