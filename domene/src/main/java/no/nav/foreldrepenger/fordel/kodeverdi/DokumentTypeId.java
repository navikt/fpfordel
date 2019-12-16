package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.LinkedHashMap;
import java.util.List;
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
public enum DokumentTypeId implements Kodeverdi {

    SØKNAD_SVANGERSKAPSPENGER("SØKNAD_SVANGERSKAPSPENGER","I000001", "Søknad om svangerskapspenger"),
    SØKNAD_FORELDREPENGER_ADOPSJON("SØKNAD_FORELDREPENGER_ADOPSJON","I000002", "Søknad om foreldrepenger ved adopsjon"),
    SØKNAD_ENGANGSSTØNAD_FØDSEL("SØKNAD_ENGANGSSTØNAD_FØDSEL","I000003", "Søknad om engangsstønad ved fødsel"),
    SØKNAD_ENGANGSSTØNAD_ADOPSJON("SØKNAD_ENGANGSSTØNAD_ADOPSJON","I000004", "Søknad om engangsstønad ved adopsjon"),
    SØKNAD_FORELDREPENGER_FØDSEL("SØKNAD_FORELDREPENGER_FØDSEL","I000005", "Søknad om foreldrepenger ved fødsel"),
    FLEKSIBELT_UTTAK_FORELDREPENGER("FLEKSIBELT_UTTAK_FORELDREPENGER","I000006", "Utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)"),
    INNTEKTSOPPLYSNING_SELVSTENDIG("INNTEKTSOPPLYSNING_SELVSTENDIG","I000007", "Inntektsopplysninger om selvstendig næringsdrivende og/eller frilansere som skal ha foreldrepenger eller svangerskapspenger"),
    SØKNAD_SKAFFE_BIL("SØKNAD_SKAFFE_BIL","I000008", "Søknad om stønad til anskaffelse av motorkjøretøy"),
    SØKNAD_REISEUTGIFT_BIL("SØKNAD_REISEUTGIFT_BIL","I000009", "Søknad om refusjon av reiseutgifter til bil"),
    SØKNAD_TILPASSNING_BIL("SØKNAD_TILPASSNING_BIL","I000010", "Søknad om spesialutstyr og -tilpassing til bil"),
    LEGEERKLÆRING_EVNE_KJØRE_BIL("LEGEERKLÆRING_EVNE_KJØRE_BIL","I000011", "Legeerklæring om søkerens evne til å føre motorkjøretøy og om behovet for ekstra transport på grunn av funksjonshemmingen"),
    TILLEGGSJKJEMA_BIL("TILLEGGSJKJEMA_BIL","I000012", "Tilleggskjema for bil"),
    BEKREFTELSE_OPPMØTE("BEKREFTELSE_OPPMØTE","I000013", "Bekreftelse på oppmøte"),
    DOK_BEHOV_LEDSAGER("DOK_BEHOV_LEDSAGER","I000014", "Dokumentasjon av behov for ledsager"),
    DOK_BEHOV_TRANSPORTMIDDEL("DOK_BEHOV_TRANSPORTMIDDEL","I000015", "Dokumentasjon av behov for dyrere transportmiddel"),
    DOK_INNTEKT("DOK_INNTEKT","I000016", "Dokumentasjon av inntekt"),
    DOK_UTGIFT_REISE("DOK_UTGIFT_REISE","I000017", "Dokumentasjon av reiseutgifter"),
    SPESIALISTERKLÆRING("SPESIALISTERKLÆRING","I000018", "Spesialisterklæring"),
    DOK_VEIFORHOLD("DOK_VEIFORHOLD","I000019", "Dokumentasjon av veiforhold"),
    KOPI_VERGEATTEST("KOPI_VERGEATTEST","I000020", "Kopi av verge- eller hjelpeverge attest"),
    KOPI_VOGNKORT("KOPI_VOGNKORT","I000021", "Kopi av vognkort"),
    KOPI_FØRERKORT("KOPI_FØRERKORT","I000022", "Kopi av førerkort"),
    LEGEERKLÆRING("LEGEERKLÆRING","I000023", "Legeerklæring"),
    GJELDSBREV_GRUPPE_1("GJELDSBREV_GRUPPE_1","I000024", "Gjeldsbrev gruppe 1"),
    GJELDSBREV_GRUPPE_2("GJELDSBREV_GRUPPE_2","I000025", "Gjeldsbrev gruppe 2"),
    INNTEKTSOPPLYSNINGER("INNTEKTSOPPLYSNINGER","I000026", "Inntektsopplysninger for arbeidstaker som skal ha sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger"),
    KLAGE_DOKUMENT("KLAGE_DOKUMENT","I000027", "Klage/anke"),
    BREV_UTLAND("BREV_UTLAND","I000028", "Brev - utland"),
    ANNET_SKJEMA_UTLAND_IKKE_NAV("ANNET_SKJEMA_UTLAND_IKKE_NAV","I000029", "Annet skjema (ikke NAV-skjema) - utland"),
    NÆRINGSOPPGAVE("NÆRINGSOPPGAVE","I000030", "Næringsoppgave"),
    PERSONINNTEKTSKJEMA("PERSONINNTEKTSKJEMA","I000031", "Personinntektsskjema"),
    RESULTATREGNSKAP("RESULTATREGNSKAP","I000032", "Resultatregnskap"),
    LØNNSLIPP("LØNNSLIPP","I000033", "Lønnsslipp"),
    OPPPDRAGSKONTRAKT("OPPPDRAGSKONTRAKT","I000034", "Oppdragskontrakt"),
    LØNNS_OG_TREKKOPPGAVE("LØNNS_OG_TREKKOPPGAVE","I000035", "Lønns- og trekkoppgave"),
    DOK_FERIE("DOK_FERIE","I000036", "Dokumentasjon av ferie"),
    DOK_INNLEGGELSE("DOK_INNLEGGELSE","I000037", "Dokumentasjon av innleggelse i helseinstitusjon"),
    DOK_MORS_UTDANNING_ARBEID_SYKDOM("DOK_MORS_UTDANNING_ARBEID_SYKDOM","I000038", "Dokumentasjon av mors utdanning, arbeid eller sykdom"),
    DOK_MILITÆR_SIVIL_TJENESTE("DOK_MILITÆR_SIVIL_TJENESTE","I000039", "Dokumentasjon av militær- eller siviltjeneste"),
    DOK_ASYL_DATO("DOK_ASYL_DATO","I000040", "Dokumentasjon av dato for asyl"),
    DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL("DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL","I000041", "Dokumentasjon av termindato, fødsel eller dato for omsorgsovertakelse"),
    DOKUMENTASJON_AV_OMSORGSOVERTAKELSE("DOKUMENTASJON_AV_OMSORGSOVERTAKELSE","I000042", "Dokumentasjon av dato for overtakelse av omsorg"),
    DOK_ARBEIDSFORHOLD("DOK_ARBEIDSFORHOLD","I000043", "Dokumentasjon av arbeidsforhold"),
    DOK_ETTERLØNN("DOK_ETTERLØNN","I000044", "Dokumentasjon av etterlønn/sluttvederlag"),
    BESKRIVELSE_FUNKSJONSNEDSETTELSE("BESKRIVELSE_FUNKSJONSNEDSETTELSE","I000045", "Beskrivelse av funksjonsnedsettelse"),
    KVITTERING_DOKUMENTINNSENDING("KVITTERING_DOKUMENTINNSENDING","I000046", "Kvittering dokumentinnsending"),
    BRUKEROPPLASTET_DOKUMENTASJON("BRUKEROPPLASTET_DOKUMENTASJON","I000047", "Brukeropplastet dokumentasjon"),
    BREV("BREV","I000048", "Brev"),
    ANNET_SKJEMA_IKKE_NAV("ANNET_SKJEMA_IKKE_NAV","I000049", "Annet skjema (ikke NAV-skjema)"),
    FORELDREPENGER_ENDRING_SØKNAD("FORELDREPENGER_ENDRING_SØKNAD","I000050", "Søknad om endring av uttak av foreldrepenger eller overføring av kvote"),
    BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM("BEKREFTELSE_DELTAR_KVALIFISERINGSPROGRAM","I000051", "Bekreftelse på deltakelse i kvalifiseringsprogrammet"),
    SKJEMA_OPPLYSNING_INNTEKT("SKJEMA_OPPLYSNING_INNTEKT","I000052", "Inntektsopplysningsskjema"),
    DOK_ANDRE_UTBETALINGER("DOK_ANDRE_UTBETALINGER","I000053", "Dokumentasjon av andre utbetalinger"),
    DOK_UTBETALING_FRA_ARBEIDSGIVER("DOK_UTBETALING_FRA_ARBEIDSGIVER","I000054", "Dokumentasjon av utbetalinger eller goder fra arbeidsgiver"),
    BEKREFTELSE_OPPHOLDSTILLATELSE("BEKREFTELSE_OPPHOLDSTILLATELSE","I000055", "Bekreftelse på oppholdstillatelse"),
    DOK_UTGIFT_BARNEPASS("DOK_UTGIFT_BARNEPASS","I000056", "Dokumentasjon av utgifter til stell og pass av barn"),
    TREKKOPPLYSNING_ARBEIDSTAKER("TREKKOPPLYSNING_ARBEIDSTAKER","I000057", "Trekkopplysninger for arbeidstaker som skal ha: sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger"),
    DOK_ANDRE_YTELSE("DOK_ANDRE_YTELSE","I000058", "Dokumentasjon av andre ytelser"),
    TIMELISTER("TIMELISTER","I000059", "Timelister"),
    ANNET("ANNET","I000060", "Annet"),
    BEKREFTELSE_FRA_STUDIESTED("BEKREFTELSE_FRA_STUDIESTED","I000061", "Bekreftelse fra studiested/skole"),
    BEKREFTELSE_VENTET_FØDSELSDATO("BEKREFTELSE_VENTET_FØDSELSDATO","I000062", "Bekreftelse på ventet fødselsdato"),
    FØDSELSATTEST("FØDSELSATTEST","I000063", "Fødselsattest"),
    ELEVDOKUMENTASJON_LÆRESTED("ELEVDOKUMENTASJON_LÆRESTED","I000064", "Elevdokumentasjon fra lærested"),
    BEKREFTELSE_FRA_ARBEIDSGIVER("BEKREFTELSE_FRA_ARBEIDSGIVER","I000065", "Bekreftelse fra arbeidsgiver"),
    KOPI_SKATTEMELDING("KOPI_SKATTEMELDING","I000066", "Kopi av likningsattest eller selvangivelse"),
    INNTEKTSMELDING("INNTEKTSMELDING","I000067", "Inntektsmelding"),
    MELDINGSONING("MELDINGSONING","I000068", "Melding til NAV om soning"),
    MELDINGSTRAFFEUNNDRAGELSE("MELDINGSTRAFFEUNNDRAGELSE","I000069", "Melding til NAV om unndragelse av straff"),
    MELDINGENDRINGINSTITUSJON("MELDINGENDRINGINSTITUSJON","I000070", "Melding om endring i institusjonsopphold"),
    MELDINGUTEBLITTINSTITUSJON("MELDINGUTEBLITTINSTITUSJON","I000071", "Melding om uteblivelse fra institusjon"),
    SØKNAD_KONTANTSTØTTE("SØKNAD_KONTANTSTØTTE","I000072", "Søknad om kontantstøtte til småbarnsforeldre"),
    VURDERING_ARBEID_SYKEMELDING("VURDERING_ARBEID_SYKEMELDING","I000107", "Vurdering av arbeidsmulighet/sykmelding"),
    OPPLYSNING_TILRETTELEGGING_SVANGER("OPPLYSNING_TILRETTELEGGING_SVANGER","I000108", "Opplysninger om muligheter og behov for tilrettelegging ved svangerskap"),
    SKJEMA_TILRETTELEGGING_OMPLASSERING("SKJEMA_TILRETTELEGGING_OMPLASSERING","I000109", "Skjema for tilrettelegging og omplassering ved graviditet"),
    DOKUMENTASJON_ALENEOMSORG("DOKUMENTASJON_ALENEOMSORG","I000110", "Dokumentasjon av aleneomsorg"),
    BEGRUNNELSE_SØKNAD_ETTERSKUDD("BEGRUNNELSE_SØKNAD_ETTERSKUDD","I000111", "Dokumentasjon av begrunnelse for hvorfor man søker tilbake i tid"),
    DOKUMENTASJON_INTRODUKSJONSPROGRAM("DOKUMENTASJON_INTRODUKSJONSPROGRAM","I000112", "Dokumentasjon av deltakelse i introduksjonsprogrammet"),
    TILBAKEKREV_UTTALELSE("TILBAKEKREV_UTTALELSE","I000114", "Uttalelse tilbakekreving"),
    OPPHOLDSOPPLYSNINGER("OPPHOLDSOPPLYSNINGER","I001000", "Oppholdsopplysninger"),
    ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG("ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG","I500001", "Ettersendelse til søknad om svangerskapspenger til selvstendig næringsdrivende og frilanser"),
    ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON("ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON","I500002", "Ettersendelse til søknad om foreldrepenger ved adopsjon"),
    ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL("ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL","I500003", "Ettersendelse til søknad om engangsstønad ved fødsel"),
    ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON("ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON","I500004", "Ettersendelse til søknad om engangsstønad ved adopsjon"),
    ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL("ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL","I500005", "Ettersendelse til søknad om foreldrepenger ved fødsel"),
    ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER("ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER","I500006", "Ettersendelse til utsettelse eller gradert uttak av foreldrepenger (fleksibelt uttak)"),
    ETTERSENDT_SØKNAD_SKAFFE_BIL("ETTERSENDT_SØKNAD_SKAFFE_BIL","I500008", "Ettersendelse til søknad om stønad til anskaffelse av motorkjøretøy"),
    ETTERSENDT_SØKNAD_REISEUTGIFT_BIL("ETTERSENDT_SØKNAD_REISEUTGIFT_BIL","I500009", "Ettersendelse til søknad om refusjon av reiseutgifter til bil"),
    ETTERSENDT_SØKNAD_TILPASSNING_BIL("ETTERSENDT_SØKNAD_TILPASSNING_BIL","I500010", "Ettersendelse til søknad om spesialutstyr og- tilpassing til bil"),
    ETTERSENDT_KLAGE("I500027","I500027", "Ettersendelse til klage/anke"),
    ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD("ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD","I500050", "Ettersendelse til søknad om endring av uttak av foreldrepenger eller overføring av kvote"),
    TREKKOPPLYSNINGER_ETTERSENDT("TREKKOPPLYSNINGER_ETTERSENDT","I500057", "Ettersendelse til trekkopplysninger for arbeidstaker som skal ha: sykepenger, foreldrepenger, svangerskapspenger, pleie-/opplæringspenger og omsorgspenger"),

    UDEFINERT("-", null, "Ukjent type dokument")
    ;

    private static final Map<String, DokumentTypeId> KODER = new LinkedHashMap<>();
    private static final Map<String, DokumentTypeId> OFFISIELLE_KODER = new LinkedHashMap<>();

    public static final String KODEVERK = "DOKUMENT_TYPE_ID";

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

    @JsonIgnore
    private String termnavn;

    private DokumentTypeId(String kode, String offisiellKode, String termnavn) {
        this.kode = kode;
        this.offisiellKode = offisiellKode;
        this.termnavn = termnavn;
    }

    @JsonCreator
    public static DokumentTypeId fraKode(@JsonProperty("kode") String kode) {
        if (kode == null) {
            return null;
        }
        var ad = KODER.get(kode);
        if (ad == null) {
            throw new IllegalArgumentException("Ukjent Tema: " + kode);
        }
        return ad;
    }

    public static DokumentTypeId fraKodeDefaultUdefinert(@JsonProperty("kode") String kode) {
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

    @Override
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


    private static final Set<DokumentTypeId> ENGANGSSTØNAD_TYPER = Set.of(
            SØKNAD_ENGANGSSTØNAD_FØDSEL,
            SØKNAD_ENGANGSSTØNAD_ADOPSJON,
            ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL,
            ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON);

    private static final List<DokumentTypeId> FORELDREPENGER_TYPER = List.of(
            SØKNAD_FORELDREPENGER_FØDSEL,
            SØKNAD_FORELDREPENGER_ADOPSJON,
            FORELDREPENGER_ENDRING_SØKNAD,
            FLEKSIBELT_UTTAK_FORELDREPENGER,
            ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON,
            ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL,
            ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER,
            ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD);

    private static final Set<DokumentTypeId> SVANGERSKAPSPENGER_TYPER = Set.of(
            SØKNAD_SVANGERSKAPSPENGER,
            ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG);

    private static final Set<DokumentTypeId> KLAGE_TYPER = Set.of(
            KLAGE_DOKUMENT,
            ETTERSENDT_KLAGE);

    private static final Set<DokumentTypeId> SØKNAD_TYPER = Set.of(
            SØKNAD_ENGANGSSTØNAD_FØDSEL,
            SØKNAD_FORELDREPENGER_FØDSEL,
            SØKNAD_ENGANGSSTØNAD_ADOPSJON,
            SØKNAD_FORELDREPENGER_ADOPSJON,
            SØKNAD_SVANGERSKAPSPENGER);

    private static final Set<DokumentTypeId> ENDRING_SØKNAD_TYPER = Set.of(
            FORELDREPENGER_ENDRING_SØKNAD,
            FLEKSIBELT_UTTAK_FORELDREPENGER);

    public static boolean erForeldrepengerRelatert(DokumentTypeId kode) {
        return FORELDREPENGER_TYPER.contains(kode);
    }

    public static boolean erInntektsmelding(DokumentTypeId kode) {
        return INNTEKTSMELDING.equals(kode);
    }

    public static boolean erEngangsstønadRelatert(DokumentTypeId kode) {
        return ENGANGSSTØNAD_TYPER.contains(kode);
    }

    public static boolean erSvangerskapspengerRelatert(DokumentTypeId kode) {
        return SVANGERSKAPSPENGER_TYPER.contains(kode);
    }

    public static boolean erSøknadType(DokumentTypeId kode) {
        return SØKNAD_TYPER.contains(kode) || ENDRING_SØKNAD_TYPER.contains(kode);
    }

    public static boolean erKlageType(DokumentTypeId kode) {
        return KLAGE_TYPER.contains(kode);
    }

    public static void main(String[] args) {
        System.out.println(KODER.keySet());
    }
}