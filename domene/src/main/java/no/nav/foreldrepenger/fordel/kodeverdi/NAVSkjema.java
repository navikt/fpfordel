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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
public enum NAVSkjema implements Kodeverdi {

    SKJEMA_SVANGERSKAPSPENGER("SSVPA", "NAV 14-04.10", "Søknad om svangerskapspenger for arbeidstakere"),
    SKJEMA_SVANGERSKAPSPENGER_SN("SSVPS", "NAV 14-04.10", "Søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser"),
    SKJEMA_FORELDREPENGER_ADOPSJON("SFPA", "NAV 14-05.06", "Søknad om foreldrepenger ved adopsjon"),
    SKJEMA_ENGANGSSTØNAD_FØDSEL("SESF", "NAV 14-05.07", "Søknad om engangsstønad ved fødsel"),
    SKJEMA_ENGANGSSTØNAD_ADOPSJON("SESA", "NAV 14-05.08", "Søknad om engangsstønad ved adopsjon"),
    SKJEMA_FORELDREPENGER_FØDSEL("SFPF", "NAV 14-05.09", "Søknad om foreldrepenger ved fødsel"),
    SKJEMA_FLEKSIBELT_UTTAK("SFUT", "NAV 14-16.05", "Søknad om endring eller nytt uttak av foreldrepenger"),
    SKJEMA_INNTEKTSOPPLYSNING_SELVSTENDIG("SIOS", "NAV 14-35.01", "Inntektsopplysninger for selvstendig næringsdrivende og frilansere som skal ha foreldrepenger eller svangerskapspenger"),
    SKJEMA_INNTEKTSOPPLYSNINGER("SIOP", "NAV 08-30.01", "Inntektsopplysninger for arbeidstaker som skal ha sykepenger foreldrepenger svangerskapspenger pleie-/opplæringspenger og omsorgspenger"),
    SKJEMA_KLAGE_DOKUMENT("SKLAGE", "NAV 90-00.08", "Klage/anke"),
    SKJEMA_FORELDREPENGER_ENDRING("SEND", "NAV 14-05.10", "Søknad om endring av uttak av foreldrepenger eller overføring av kvote"),

    SKJEMAE_SVANGERSKAPSPENGER("SESV", "NAVe 14-04.10", "Ettersendelse til søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser"),
    SKJEMAE_FORELDREPENGER_ADOPSJON("SEFA", "NAVe 14-05.06", "Ettersendelse til søknad om foreldrepenger ved adopsjon"),
    SKJEMAE_ENGANGSSTØNAD_FØDSEL("SEEF", "NAVe 14-05.07", "Ettersendelse til søknad om engangsstønad ved fødsel"),
    SKJEMAE_ENGANGSSTØNAD_ADOPSJON("SEEA", "NAVe 14-05.08", "Ettersendelse til søknad om engangsstønad ved adopsjon"),
    SKJEMAE_FORELDREPENGER_FØDSEL("SEFF", "NAVe 14-05.09", "Ettersendelse til søknad om foreldrepenger ved fødsel"),
    SKJEMAE_FLEKSIBELT_UTTAK("SEFU", "NAVe 14-16.05", "Ettersendelse til søknad om endring eller nytt uttak av foreldrepenger"),
    SKJEMAE_INNTEKTSOPPLYSNING_SELVSTENDIG("SEIS", "NAVe 14-35.01", "Ettersendelse til inntektsopplysninger for selvstendig næringsdrivende og frilansere som skal ha foreldrepenger eller svangerskapspenger"),
    SKJEMAE_INNTEKTSOPPLYSNINGER("SEIP", "NAVe 08-30.01", "Ettersendelse til inntektsopplysninger for arbeidstaker som skal ha sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger"),
    SKJEMAE_KLAGE("SEKLAG", "NAVe 90-00.08", "Ettersendelse klage/anke"),
    SKJEMAE_FORELDREPENGER_ENDRING("SEEN", "NAVe 14-05.10", "Ettersendelse til søknad om endring av uttak av foreldrepenger eller overføring av kvote"),

    SKJEMA_ANNEN_POST("SANP", "NAV 00-03.00", "Annen post"),

    // Altinn-skjemakode
    SKJEMA_INNTEKTSMELDING("INNTEKTSMELDING", "4936", "Inntektsmelding"),

    // Arbeidstilsynet-skjemakode
    SKJEMA_TILRETTELEGGING_B("SSVPT", "AT-474B", "Tilrettelegging/omplassering ved graviditet"),
    SKJEMA_TILRETTELEGGING_N("SSVPN", "AT-474N", "LIKT SOM SKJEMA_TILRETTELEGGING_B"),

    // ANNET
    FORSIDE_SVP_GAMMEL("SSVPG", "AT-474B","Tilrettelegging/omplassering pga graviditet / Søknad om svangerskapspenger til arbeidstaker"),

    UDEFINERT("-", null, "Ukjent type dokument");

    private static final Map<String, NAVSkjema> KODER = new LinkedHashMap<>();
    private static final Map<String, NAVSkjema> OFFISIELLE_KODER = new LinkedHashMap<>();
    private static final Map<String, NAVSkjema> TERMNAVN_KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "NAV_SKJEMA";

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
            if (v.offisiellKode != null) {
                OFFISIELLE_KODER.putIfAbsent(v.offisiellKode, v);
            }
            if (v.termnavn != null) {
                TERMNAVN_KODER.putIfAbsent(v.termnavn, v);
            }
        }
    }

    private String kode;

    @JsonIgnore
    private String offisiellKode;

    @JsonIgnore
    private String termnavn;

    private NAVSkjema(String kode, String offisiellKode, String termnavn) {
        this.kode = kode;
        this.offisiellKode = offisiellKode;
        this.termnavn = termnavn;
    }

    @JsonCreator
    public static NAVSkjema fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Tema: " + kode);
        }
        return ad;
    }

    public static NAVSkjema fraKodeDefaultUdefinert(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return KODER.getOrDefault(kode, UDEFINERT);
    }

    public static NAVSkjema fraOffisiellKode(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return OFFISIELLE_KODER.getOrDefault(kode, UDEFINERT);
    }

    public static NAVSkjema fraTermNavn(String navn) {
        if (navn == null) {
            return UDEFINERT;
        }
        return TERMNAVN_KODER.getOrDefault(navn, UDEFINERT);
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

    public String getOffisiellKode() {
        return offisiellKode;
    }

    public String getTermNavn() {
        return termnavn;
    }

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<NAVSkjema, String> {
        @Override
        public String convertToDatabaseColumn(NAVSkjema attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public NAVSkjema convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

    private static final Set<NAVSkjema> SØKNAD_TYPER = Set.of(
            SKJEMA_SVANGERSKAPSPENGER,
            SKJEMA_SVANGERSKAPSPENGER_SN,
            SKJEMA_TILRETTELEGGING_B,
            SKJEMA_TILRETTELEGGING_N,
            FORSIDE_SVP_GAMMEL,
            SKJEMA_ENGANGSSTØNAD_ADOPSJON,
            SKJEMA_ENGANGSSTØNAD_FØDSEL,
            SKJEMA_FORELDREPENGER_ADOPSJON,
            SKJEMA_FORELDREPENGER_FØDSEL);

    private static final Set<NAVSkjema> KLAGE_TYPER = Set.of(
            SKJEMAE_KLAGE,
            SKJEMA_KLAGE_DOKUMENT);

    private static final Set<NAVSkjema> ENDRING_SØKNAD_TYPER = Set.of(
            SKJEMA_FLEKSIBELT_UTTAK,
            SKJEMA_FORELDREPENGER_ENDRING);

    public static boolean erInntektsmelding(NAVSkjema kode) {
        return SKJEMA_INNTEKTSMELDING.equals(kode);
    }

    public static boolean erSøknadType(NAVSkjema kode) {
        return SØKNAD_TYPER.contains(kode) || ENDRING_SØKNAD_TYPER.contains(kode);
    }

    public static boolean erKlageType(NAVSkjema kode) {
        return KLAGE_TYPER.contains(kode);
    }

}
