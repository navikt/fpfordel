package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum DokumentTypeId implements Kodeverdi {

    // Søknader
    SØKNAD_SVANGERSKAPSPENGER("SØKNAD_SVANGERSKAPSPENGER", "I000001", "Søknad om svangerskapspenger"),
    SØKNAD_FORELDREPENGER_ADOPSJON("SØKNAD_FORELDREPENGER_ADOPSJON", "I000002", "Søknad om foreldrepenger ved adopsjon"),
    SØKNAD_ENGANGSSTØNAD_FØDSEL("SØKNAD_ENGANGSSTØNAD_FØDSEL", "I000003", "Søknad om engangsstønad ved fødsel"),
    SØKNAD_ENGANGSSTØNAD_ADOPSJON("SØKNAD_ENGANGSSTØNAD_ADOPSJON", "I000004", "Søknad om engangsstønad ved adopsjon"),
    SØKNAD_FORELDREPENGER_FØDSEL("SØKNAD_FORELDREPENGER_FØDSEL", "I000005", "Søknad om foreldrepenger ved fødsel"),
    FLEKSIBELT_UTTAK_FORELDREPENGER("FLEKSIBELT_UTTAK_FORELDREPENGER", "I000006",
        "Utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)"),
    FORELDREPENGER_ENDRING_SØKNAD("FORELDREPENGER_ENDRING_SØKNAD", "I000050",
        "Søknad om endring av uttak av foreldrepenger eller overføring av kvote"),

    // Klage + Tilbakekreving
    KLAGE_DOKUMENT("KLAGE_DOKUMENT", "I000027", "Klage/anke"),
    ETTERSENDT_KLAGE("I500027", "I500027", "Ettersendelse til klage/anke"),
    TILBAKEKREV_UTTALELSE("TILBAKEKREV_UTTALELSE", "I000114", "Uttalelse tilbakekreving"),

    // Inntekt
    INNTEKTSOPPLYSNING_SELVSTENDIG("INNTEKTSOPPLYSNING_SELVSTENDIG", "I000007",
        "Inntektsopplysninger om selvstendig næringsdrivende og/eller frilansere som skal ha foreldrepenger eller svangerskapspenger"),
    INNTEKTSOPPLYSNINGER("INNTEKTSOPPLYSNINGER", "I000026",
        "Inntektsopplysninger for arbeidstaker som skal ha sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger"),
    RESULTATREGNSKAP("RESULTATREGNSKAP", "I000032", "Resultatregnskap"),
    INNTEKTSMELDING("INNTEKTSMELDING", "I000067", "Inntektsmelding"),

    // Vedlegg fra brukerdialog - brukes i opplysningsplikt (ManglendeVedlegg)
    LEGEERKLÆRING("LEGEERKLÆRING", "I000023", "Legeerklæring"),
    DOK_INNLEGGELSE("DOK_INNLEGGELSE", "I000037", "Dokumentasjon av innleggelse i helseinstitusjon"),
    DOK_MORS_UTDANNING_ARBEID_SYKDOM("DOK_MORS_UTDANNING_ARBEID_SYKDOM", "I000038", "Dokumentasjon av mors utdanning, arbeid eller sykdom"),
    DOK_MILITÆR_SIVIL_TJENESTE("DOK_MILITÆR_SIVIL_TJENESTE", "I000039", "Dokumentasjon av militær- eller siviltjeneste"),
    DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL("DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL", "I000041",
        "Dokumentasjon av termindato (lev. kun av mor), fødsel eller dato for omsorgsovertakelse"),
    DOKUMENTASJON_AV_OMSORGSOVERTAKELSE("DOKUMENTASJON_AV_OMSORGSOVERTAKELSE", "I000042", "Dokumentasjon av dato for overtakelse av omsorg"),
    DOK_ETTERLØNN("DOK_ETTERLØNN", "I000044", "Dokumentasjon av etterlønn/sluttvederlag"),
    BESKRIVELSE_FUNKSJONSNEDSETTELSE("BESKRIVELSE_FUNKSJONSNEDSETTELSE", "I000045", "Beskrivelse av funksjonsnedsettelse"),
    BEKREFTELSE_FRA_STUDIESTED("BEKREFTELSE_FRA_STUDIESTED", "I000061", "Bekreftelse fra studiested/skole"),
    BEKREFTELSE_VENTET_FØDSELSDATO("BEKREFTELSE_VENTET_FØDSELSDATO", "I000062", "Bekreftelse på ventet fødselsdato"),
    FØDSELSATTEST("FØDSELSATTEST", "I000063", "Fødselsattest"),
    ELEVDOKUMENTASJON_LÆRESTED("ELEVDOKUMENTASJON_LÆRESTED", "I000064", "Elevdokumentasjon fra lærested"),
    BEKREFTELSE_FRA_ARBEIDSGIVER("BEKREFTELSE_FRA_ARBEIDSGIVER", "I000065", "Bekreftelse fra arbeidsgiver"),
    DOKUMENTASJON_ALENEOMSORG("DOKUMENTASJON_ALENEOMSORG", "I000110", "Dokumentasjon av aleneomsorg"),
    BEGRUNNELSE_SØKNAD_ETTERSKUDD("BEGRUNNELSE_SØKNAD_ETTERSKUDD", "I000111", "Dokumentasjon av begrunnelse for hvorfor man søker tilbake i tid"),
    DOKUMENTASJON_INTRODUKSJONSPROGRAM("DOKUMENTASJON_INTRODUKSJONSPROGRAM", "I000112", "Dokumentasjon av deltakelse i introduksjonsprogrammet"),
    DOKUMENTASJON_FORSVARSTJENESTE("DOKUMENTASJON_FORSVARSTJENESTE", "I000116",
        "Bekreftelse på øvelse eller tjeneste i Forsvaret eller Sivilforsvaret"),
    DOKUMENTASJON_NAVTILTAK("DOKUMENTASJON_NAVTILTAK", "I000117", "Bekreftelse på tiltak i regi av Arbeids- og velferdsetaten"),

    // Tidligere selvbetjening - kan antagelig fjernes snart
    DOK_FERIE("DOK_FERIE", "I000036", "Dokumentasjon av ferie"),
    DOK_ASYL_DATO("DOK_ASYL_DATO", "I000040", "Dokumentasjon av dato for asyl"),
    DOK_ARBEIDSFORHOLD("DOK_ARBEIDSFORHOLD", "I000043", "Dokumentasjon av arbeidsforhold"),
    KVITTERING_DOKUMENTINNSENDING("KVITTERING_DOKUMENTINNSENDING", "I000046", "Kvittering dokumentinnsending"),
    BRUKEROPPLASTET_DOKUMENTASJON("BRUKEROPPLASTET_DOKUMENTASJON", "I000047", "Brukeropplastet dokumentasjon"),
    BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM("BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM", "I000051",
        "Bekreftelse på deltakelse i kvalifiseringsprogrammet"),
    BEKREFTELSE_OPPHOLDSTILLATELSE("BEKREFTELSE_OPPHOLDSTILLATELSE", "I000055", "Bekreftelse på oppholdstillatelse"),
    KOPI_SKATTEMELDING("KOPI_SKATTEMELDING", "I000066", "Kopi av likningsattest eller selvangivelse"),
    VURDERING_ARBEID_SYKEMELDING("VURDERING_ARBEID_SYKEMELDING", "I000107", "Vurdering av arbeidsmulighet/sykmelding"),
    OPPLYSNING_TILRETTELEGGING_SVANGER("OPPLYSNING_TILRETTELEGGING_SVANGER", "I000108",
        "Opplysninger om muligheter og behov for tilrettelegging ved svangerskap"),
    SKJEMA_TILRETTELEGGING_OMPLASSERING("SKJEMA_TILRETTELEGGING_OMPLASSERING", "I000109",
        "Skjema for tilrettelegging og omplassering ved graviditet"),
    OPPHOLDSOPPLYSNINGER("OPPHOLDSOPPLYSNINGER", "I001000", "Oppholdsopplysninger"),

    // Identifisering av behandlingstema
    ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG("ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG", "I500001",
        "Ettersendelse til søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser"),
    ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON("ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON", "I500002",
        "Ettersendelse til søknad om foreldrepenger ved adopsjon"),
    ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL("ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL", "I500003",
        "Ettersendelse til søknad om engangsstønad ved fødsel"),
    ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON("ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON", "I500004",
        "Ettersendelse til søknad om engangsstønad ved adopsjon"),
    ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL("ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL", "I500005",
        "Ettersendelse til søknad om foreldrepenger ved fødsel"),
    ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER("ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER", "I500006",
        "Ettersendelse til utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)"),
    ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD("ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD", "I500050",
        "Ettersendelse til søknad om endring av uttak av foreldrepenger eller overføring av kvote"),

    // Diverse vanlig forekommende
    BREV_UTLAND("BREV_UTLAND", "I000028", "Brev - utland"),
    ANNET_SKJEMA_UTLAND_IKKE_NAV("ANNET_SKJEMA_UTLAND_IKKE_NAV", "I000029", "Annet skjema (ikke NAV-skjema) - utland"),
    BREV("BREV", "I000048", "Brev"),
    ANNET_SKJEMA_IKKE_NAV("ANNET_SKJEMA_IKKE_NAV", "I000049", "Annet skjema (ikke NAV-skjema)"),
    ANNET("ANNET", "I000060", "Annet"),

    UDEFINERT("-", null, "Ukjent type dokument");

    public static final String KODEVERK = "DOKUMENT_TYPE_ID";
    private static final Map<String, DokumentTypeId> KODER = new LinkedHashMap<>();
    private static final Map<String, DokumentTypeId> OFFISIELLE_KODER = new LinkedHashMap<>();
    private static final Map<String, DokumentTypeId> TERMNAVN_KODER = new LinkedHashMap<>();
    private static final Set<DokumentTypeId> KLAGE_TYPER = Set.of(KLAGE_DOKUMENT, ETTERSENDT_KLAGE);
    private static final Set<DokumentTypeId> SØKNAD_TYPER = Set.of(SØKNAD_ENGANGSSTØNAD_FØDSEL, SØKNAD_FORELDREPENGER_FØDSEL,
        SØKNAD_ENGANGSSTØNAD_ADOPSJON, SØKNAD_FORELDREPENGER_ADOPSJON, SØKNAD_SVANGERSKAPSPENGER);
    private static final Set<DokumentTypeId> ENDRING_SØKNAD_TYPER = Set.of(FORELDREPENGER_ENDRING_SØKNAD, FLEKSIBELT_UTTAK_FORELDREPENGER);
    // Ulike titler er brukt i selvbetjening, fordel, sak og kodeverk
    private static final Map<String, DokumentTypeId> ALT_TITLER = Map.ofEntries(
        Map.entry("Søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser", SØKNAD_SVANGERSKAPSPENGER),
        Map.entry("Søknad om svangerskapspenger for selvstendig", SØKNAD_SVANGERSKAPSPENGER),
        Map.entry("Inntektsopplysningsskjema", INNTEKTSOPPLYSNINGER), Map.entry("Bekreftelse på avtalt ferie", DOK_FERIE),
        Map.entry("Mor er innlagt i helseinstitusjon", DOK_INNLEGGELSE),
        Map.entry("Mor er i arbeid, tar utdanning eller er for syk til å ta seg av barnet", DOK_MORS_UTDANNING_ARBEID_SYKDOM),
        Map.entry("Dokumentasjon av termindato, fødsel eller dato for omsorgsovertakelse", DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL),
        Map.entry("Tjenestebevis", DOK_MILITÆR_SIVIL_TJENESTE),
        Map.entry("Dokumentasjon av overtakelse av omsorg", DOKUMENTASJON_AV_OMSORGSOVERTAKELSE),
        Map.entry("Dokumentasjon av etterlønn eller sluttvederlag", DOK_ETTERLØNN),
        Map.entry("Beskrivelse/Dokumentasjon funksjonsnedsettelse", BESKRIVELSE_FUNKSJONSNEDSETTELSE),
        Map.entry("Mor deltar i kvalifiseringsprogrammet", BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM),
        Map.entry("Mor tar utdanning på heltid", BEKREFTELSE_FRA_STUDIESTED), Map.entry("Terminbekreftelse", BEKREFTELSE_VENTET_FØDSELSDATO),
        Map.entry("Kopi av skattemelding", KOPI_SKATTEMELDING), Map.entry("Svar på varsel om tilbakebetaling", TILBAKEKREV_UTTALELSE),
        Map.entry("Klage", DokumentTypeId.KLAGE_DOKUMENT), Map.entry("Anke", DokumentTypeId.KLAGE_DOKUMENT));

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

    @JsonValue
    private String kode;
    private String offisiellKode;
    private String termnavn;

    private DokumentTypeId(String kode, String offisiellKode, String termnavn) {
        this.kode = kode;
        this.offisiellKode = offisiellKode;
        this.termnavn = termnavn;
    }

    public static DokumentTypeId fraKode(String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Tema: " + kode);
        }
        return ad;
    }

    public static DokumentTypeId fraKodeDefaultUdefinert(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return KODER.getOrDefault(kode, UDEFINERT);
    }

    public static DokumentTypeId fraOffisiellKode(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return OFFISIELLE_KODER.getOrDefault(kode, UDEFINERT);
    }

    public static DokumentTypeId fraTermNavn(String navn) {
        return Optional.ofNullable(navn)
            .map(n -> Optional.ofNullable(TERMNAVN_KODER.get(n)).orElseGet(() -> ALT_TITLER.getOrDefault(n, UDEFINERT)))
            .orElse(UDEFINERT);
    }

    public static boolean erInntektsmelding(DokumentTypeId kode) {
        return INNTEKTSMELDING.equals(kode);
    }

    public static boolean erSøknadType(DokumentTypeId kode) {
        return SØKNAD_TYPER.contains(kode) || ENDRING_SØKNAD_TYPER.contains(kode);
    }

    public static boolean erFørsteSøknadType(DokumentTypeId kode) {
        return SØKNAD_TYPER.contains(kode);
    }

    public static boolean erKlageType(DokumentTypeId kode) {
        return KLAGE_TYPER.contains(kode);
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

    @Converter(autoApply = true)
    public static class KodeverdiConverter implements AttributeConverter<DokumentTypeId, String> {
        @Override
        public String convertToDatabaseColumn(DokumentTypeId attribute) {
            return attribute == null ? null : attribute.getKode();
        }

        @Override
        public DokumentTypeId convertToEntityAttribute(String dbData) {
            return dbData == null ? null : fraKode(dbData);
        }
    }

}
