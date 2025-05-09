package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonValue;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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

    // Inntektsmelding
    INNTEKTSMELDING("INNTEKTSMELDING", "I000067", "Inntektsmelding"),

    // Klage + Tilbakekreving
    KLAGE_DOKUMENT("KLAGE_DOKUMENT", "I000027", "Klage/anke"),
    ETTERSENDT_KLAGE("I500027", "I500027", "Ettersendelse til klage/anke"),
    TILBAKEKREV_UTTALELSE("TILBAKEKREV_UTTALELSE", "I000114", "Uttalelse tilbakekreving"),
    TILBAKEBETALING_UTTALSELSE("I000119", "Uttalelse om tilbakebetaling"),

    // Inntekt
    INNTEKTSOPPLYSNING_SELVSTENDIG("INNTEKTSOPPLYSNING_SELVSTENDIG", "I000007",
        "Inntektsopplysninger om selvstendig næringsdrivende og/eller frilansere som skal ha foreldrepenger eller svangerskapspenger"),
    INNTEKTSOPPLYSNINGER("INNTEKTSOPPLYSNINGER", "I000026",
        "Inntektsopplysninger for arbeidstaker som skal ha sykepenger foreldrepenger svangerskapspenger pleie-/opplæringspenger"),
    INNTEKTSOPPLYSNINGERNY("INNTEKTSOPPLYSNINGERNY", "I000226",
        "Inntektsopplysninger for arbeidstaker som skal ha sykepenger foreldrepenger svangerskapspenger pleie-/opplæringspenger og omsorgspenger"),
    INNTEKTSOPPLYSNINGSSKJEMA("I000052", "I000052", "Inntektsopplysningsskjema"),
    DOK_INNTEKT("DOK_INNTEKT", "I000016", "Dokumentasjon av inntekt"),
    RESULTATREGNSKAP("RESULTATREGNSKAP", "I000032", "Resultatregnskap"),

    // Vedlegg fra brukerdialog og fyllut-sendinn
    LEGEERKLÆRING("LEGEERKLÆRING", "I000023", "Legeerklæring"),
    DOK_INNLEGGELSE("DOK_INNLEGGELSE", "I000037", "Dokumentasjon av innleggelse i helseinstitusjon"),
    DOK_MORS_UTDANNING_ARBEID_SYKDOM("DOK_MORS_UTDANNING_ARBEID_SYKDOM", "I000038", "Dokumentasjon av mors utdanning arbeid eller sykdom"),
    DOK_MILITÆR_SIVIL_TJENESTE("DOK_MILITÆR_SIVIL_TJENESTE", "I000039", "Dokumentasjon av militær- eller siviltjeneste"),
    DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL("DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL", "I000041",
        "Dokumentasjon av termindato (lev. kun av mor), fødsel eller dato for omsorgsovertakelse"),
    DOKUMENTASJON_AV_OMSORGSOVERTAKELSE("DOKUMENTASJON_AV_OMSORGSOVERTAKELSE", "I000042", "Dokumentasjon av dato for overtakelse av omsorg"),
    DOK_ETTERLØNN("DOK_ETTERLØNN", "I000044", "Dokumentasjon av etterlønn/sluttvederlag"),
    BESKRIVELSE_FUNKSJONSNEDSETTELSE("BESKRIVELSE_FUNKSJONSNEDSETTELSE", "I000045", "Beskrivelse av funksjonsnedsettelse"),
    BEKREFTELSE_FRA_STUDIESTED("BEKREFTELSE_FRA_STUDIESTED", "I000061", "Bekreftelse fra studiested/skole"),
    BEKREFTELSE_VENTET_FØDSELSDATO("BEKREFTELSE_VENTET_FØDSELSDATO", "I000062", "Bekreftelse på ventet fødselsdato"),
    FØDSELSATTEST("FØDSELSATTEST", "I000063", "Fødselsattest"),
    BEKREFTELSE_FRA_ARBEIDSGIVER("BEKREFTELSE_FRA_ARBEIDSGIVER", "I000065", "Bekreftelse fra arbeidsgiver"),
    DOKUMENTASJON_ALENEOMSORG("DOKUMENTASJON_ALENEOMSORG", "I000110", "Dokumentasjon av aleneomsorg"),
    BEGRUNNELSE_SØKNAD_ETTERSKUDD("BEGRUNNELSE_SØKNAD_ETTERSKUDD", "I000111", "Dokumentasjon av begrunnelse for hvorfor man søker tilbake i tid"),
    DOKUMENTASJON_INTRODUKSJONSPROGRAM("DOKUMENTASJON_INTRODUKSJONSPROGRAM", "I000112", "Dokumentasjon av deltakelse i introduksjonsprogrammet"),
    DOKUMENTASJON_FORSVARSTJENESTE("DOKUMENTASJON_FORSVARSTJENESTE", "I000116",
        "Bekreftelse på øvelse eller tjeneste i Forsvaret eller Sivilforsvaret"),
    DOKUMENTASJON_NAVTILTAK("DOKUMENTASJON_NAVTILTAK", "I000117", "Bekreftelse på tiltak i regi av Arbeids- og velferdsetaten"),
    SEN_SØKNAD("I000118", "Begrunnelse for sen søknad"),
    MOR_INNLAGT("I000120", "Dokumentasjon på at mor er innlagt på sykehus"),
    MOR_SYK("I000121", "Dokumentasjon på at mor er syk"),
    FAR_INNLAGT("I000122", "Dokumentasjon på at far/medmor er innlagt på sykehus"),
    FAR_SYK("I000123", "Dokumentasjon på at far/medmor er syk"),
    BARN_INNLAGT("I000124", "Dokumentasjon på at barnet er innlagt på sykehus"),
    MOR_ARBEID_STUDIE("I000130", "Dokumentasjon på at mor studerer og arbeider til sammen heltid"),
    MOR_STUDIE("I000131", "Dokumentasjon på at mor studerer på heltid"),
    MOR_ARBEID("I000132", "Dokumentasjon på at mor er i arbeid"),
    MOR_KVALPROG("I000133", "Dokumentasjon av mors deltakelse i kvalifiseringsprogrammet"),
    SKATTEMELDING("I000140", "Skattemelding"),
    TERMINBEKREFTELSE("I000141", "Terminbekreftelse"),
    MEDISINSK_DOK("I000142", "Medisinsk dokumentasjon"),
    OPPHOLD("I000143", "Dokumentasjon på oppholdstillatelse"),
    REISE("I000144", "Dokumentasjon på reiser til og fra Norge"),
    OPPFØLGING("I000145", "Dokumentasjon på oppfølging i svangerskapet"),
    DOKUMENTASJON_INNTEKT("I000146", "Dokumentasjon på inntekt"),

    // Tidligere selvbetjening - kan antagelig fjernes snart
    DOK_FERIE("DOK_FERIE", "I000036", "Dokumentasjon av ferie"),
    DOK_ARBEIDSFORHOLD("DOK_ARBEIDSFORHOLD", "I000043", "Dokumentasjon av arbeidsforhold"),
    BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM("BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM", "I000051",
        "Bekreftelse på deltakelse i kvalifiseringsprogrammet"),
    BEKREFTELSE_OPPHOLDSTILLATELSE("BEKREFTELSE_OPPHOLDSTILLATELSE", "I000055", "Bekreftelse på oppholdstillatelse"),
    KOPI_SKATTEMELDING("KOPI_SKATTEMELDING", "I000066", "Kopi av likningsattest eller selvangivelse"),
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

    private static final Map<String, DokumentTypeId> KODER = new LinkedHashMap<>();
    private static final Map<String, DokumentTypeId> OFFISIELLE_KODER = new LinkedHashMap<>();
    private static final Map<String, DokumentTypeId> TERMNAVN_KODER = new LinkedHashMap<>();
    private static final Set<DokumentTypeId> KLAGE_TYPER = Set.of(KLAGE_DOKUMENT, ETTERSENDT_KLAGE);
    private static final Set<DokumentTypeId> SØKNAD_TYPER = Set.of(SØKNAD_ENGANGSSTØNAD_FØDSEL, SØKNAD_FORELDREPENGER_FØDSEL,
        SØKNAD_ENGANGSSTØNAD_ADOPSJON, SØKNAD_FORELDREPENGER_ADOPSJON, SØKNAD_SVANGERSKAPSPENGER);
    private static final Set<DokumentTypeId> ENDRING_SØKNAD_TYPER = Set.of(FORELDREPENGER_ENDRING_SØKNAD, FLEKSIBELT_UTTAK_FORELDREPENGER);

    private static final Set<DokumentTypeId> ANNET_DOK_TYPER = Set.of(ANNET, ANNET_SKJEMA_IKKE_NAV, ANNET_SKJEMA_UTLAND_IKKE_NAV, BREV, BREV_UTLAND);

    private static final Set<DokumentTypeId> ETTERSENDELSE_TYPER = Set.of(ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG,
        ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL, ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON,
        ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL, ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON,
        ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD, ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER);

    // Ulike titler er brukt i selvbetjening, fordel, sak og kodeverk
    private static final Map<String, DokumentTypeId> ALT_TITLER = Map.ofEntries(
        Map.entry("søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser", SØKNAD_SVANGERSKAPSPENGER),
        Map.entry("søknad om svangerskapspenger for selvstendig", SØKNAD_SVANGERSKAPSPENGER),
        Map.entry("bekreftelse på avtalt ferie", DOK_FERIE),
        Map.entry("mor er innlagt i helseinstitusjon", DOK_INNLEGGELSE),
        Map.entry("mor er i arbeid tar utdanning eller er for syk til å ta seg av barnet", DOK_MORS_UTDANNING_ARBEID_SYKDOM),
        Map.entry("dokumentasjon av termindato fødsel eller dato for omsorgsovertakelse", DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL),
        Map.entry("tjenestebevis", DOK_MILITÆR_SIVIL_TJENESTE),
        Map.entry("dokumentasjon av overtakelse av omsorg", DOKUMENTASJON_AV_OMSORGSOVERTAKELSE),
        Map.entry("dokumentasjon av etterlønn eller sluttvederlag", DOK_ETTERLØNN),
        Map.entry("beskrivelse/dokumentasjon funksjonsnedsettelse", BESKRIVELSE_FUNKSJONSNEDSETTELSE),
        Map.entry("mor deltar i kvalifiseringsprogrammet", BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM),
        Map.entry("mor tar utdanning på heltid", BEKREFTELSE_FRA_STUDIESTED),
        Map.entry("kopi av skattemelding", KOPI_SKATTEMELDING),
        Map.entry("svar på varsel om tilbakebetaling", TILBAKEKREV_UTTALELSE),
        Map.entry("klage", DokumentTypeId.KLAGE_DOKUMENT),
        Map.entry("anke", DokumentTypeId.KLAGE_DOKUMENT),
        Map.entry("rettskjennelse fra trygderetten", DokumentTypeId.KLAGE_DOKUMENT),
        Map.entry("ettersending til nav 14-04.10 søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser", ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG),
        Map.entry("ettersending til nav 14-05.07 søknad om engangsstønad ved fødsel", ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL),
        Map.entry("ettersending til nav 14-05.09 søknad om foreldrepenger ved fødsel", ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL));

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
            if (v.offisiellKode != null) {
                OFFISIELLE_KODER.putIfAbsent(v.offisiellKode, v);
            }
            if (v.termnavn != null) {
                TERMNAVN_KODER.putIfAbsent(v.termnavn.toLowerCase(), v);
            }
        }
    }

    @JsonValue
    private String kode;
    private String offisiellKode;
    private String termnavn;

    DokumentTypeId(String offisiellKode, String termnavn) {
        this(offisiellKode, offisiellKode, termnavn);
    }
    DokumentTypeId(String kode, String offisiellKode, String termnavn) {
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
            .map(String::toLowerCase)
            .map(s -> s.replace(",", "").replaceAll(" {2}", " "))
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

    public static boolean erEndringssøknadType(DokumentTypeId kode) {
        return ENDRING_SØKNAD_TYPER.contains(kode);
    }

    public static boolean erKlageType(DokumentTypeId kode) {
        return KLAGE_TYPER.contains(kode);
    }

    public boolean erAnnenDokType() {
        return ANNET_DOK_TYPER.contains(this);
    }

    public boolean erEttersendelseType() {
        return ETTERSENDELSE_TYPER.contains(this);
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
