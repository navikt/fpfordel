package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * DokumentTypeId er et kodeverk som forvaltes av Kodeverkforvaltning. Det er et
 * subsett av kodeverket DokumentType, mer spesifikt alle inngående
 * dokumenttyper.
 */
@Entity(name = "DokumentTypeId")
@DiscriminatorValue(DokumentTypeId.DISCRIMINATOR)
public class DokumentTypeId extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_TYPE_ID";

    // Engangsstønad
    public static final DokumentTypeId SØKNAD_ENGANGSSTØNAD_FØDSEL = new DokumentTypeId("SØKNAD_ENGANGSSTØNAD_FØDSEL");
    public static final DokumentTypeId SØKNAD_ENGANGSSTØNAD_ADOPSJON = new DokumentTypeId(
            "SØKNAD_ENGANGSSTØNAD_ADOPSJON");
    public static final DokumentTypeId ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL = new DokumentTypeId(
            "ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL");
    public static final DokumentTypeId ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON = new DokumentTypeId(
            "ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON");

    // Foreldrepenger
    public static final DokumentTypeId SØKNAD_FORELDREPENGER_FØDSEL = new DokumentTypeId(
            "SØKNAD_FORELDREPENGER_FØDSEL");
    public static final DokumentTypeId SØKNAD_FORELDREPENGER_ADOPSJON = new DokumentTypeId(
            "SØKNAD_FORELDREPENGER_ADOPSJON");
    public static final DokumentTypeId FORELDREPENGER_ENDRING_SØKNAD = new DokumentTypeId(
            "FORELDREPENGER_ENDRING_SØKNAD");
    public static final DokumentTypeId FLEKSIBELT_UTTAK_FORELDREPENGER = new DokumentTypeId(
            "FLEKSIBELT_UTTAK_FORELDREPENGER");
    public static final DokumentTypeId ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON = new DokumentTypeId(
            "ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON");
    public static final DokumentTypeId ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL = new DokumentTypeId(
            "ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL");
    public static final DokumentTypeId ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER = new DokumentTypeId(
            "ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL");
    public static final DokumentTypeId ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD = new DokumentTypeId(
            "ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD");

    // Svangerskapspenger
    public static final DokumentTypeId SØKNAD_SVANGERSKAPSPENGER = new DokumentTypeId("SØKNAD_SVANGERSKAPSPENGER");

    // Støttedokumenter FP
    public static final DokumentTypeId INNTEKTSMELDING = new DokumentTypeId("INNTEKTSMELDING");
    public static final DokumentTypeId INNTEKTSOPPLYSNINGER = new DokumentTypeId("INNTEKTSOPPLYSNINGER");

    // Klage
    public static final DokumentTypeId KLAGE_DOKUMENT = new DokumentTypeId("KLAGE_DOKUMENT");

    // Tilbakekreving
    public static final DokumentTypeId TILBAKEKREV_UTTALELSE = new DokumentTypeId("TILBAKEKREV_UTTALELSE");

    // Uspesifikke dokumenter
    public static final DokumentTypeId DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL = new DokumentTypeId(
            "DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL");
    public static final DokumentTypeId DOKUMENTASJON_AV_OMSORGSOVERTAKELSE = new DokumentTypeId(
            "DOKUMENTASJON_AV_OMSORGSOVERTAKELSE");
    public static final DokumentTypeId ANNET = new DokumentTypeId("ANNET");

    public static final DokumentTypeId UDEFINERT = new DokumentTypeId("-");

    private static final Set<DokumentTypeId> ENGANGSSTØNAD_TYPER = Set.of(
            SØKNAD_ENGANGSSTØNAD_FØDSEL,
            SØKNAD_ENGANGSSTØNAD_ADOPSJON,
            ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL,
            ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON);

    private static final Set<DokumentTypeId> FORELDREPENGER_TYPER = Set.of(
            SØKNAD_FORELDREPENGER_FØDSEL,
            SØKNAD_FORELDREPENGER_ADOPSJON,
            FORELDREPENGER_ENDRING_SØKNAD,
            FLEKSIBELT_UTTAK_FORELDREPENGER,
            ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON,
            ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL,
            ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER,
            ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD);

    private static final Set<DokumentTypeId> SØKNAD_TYPER = Set.of(
            SØKNAD_ENGANGSSTØNAD_FØDSEL,
            SØKNAD_FORELDREPENGER_FØDSEL,
            SØKNAD_ENGANGSSTØNAD_ADOPSJON,
            SØKNAD_FORELDREPENGER_ADOPSJON,
            SØKNAD_SVANGERSKAPSPENGER);

    private static final Set<DokumentTypeId> ENDRING_SØKNAD_TYPER = Set.of(
            FORELDREPENGER_ENDRING_SØKNAD,
            FLEKSIBELT_UTTAK_FORELDREPENGER);

    DokumentTypeId() {
    }

    private DokumentTypeId(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public boolean erForeldrepengerRelatert() {
        return FORELDREPENGER_TYPER.contains(this);
    }

    public boolean erInntektsmelding() {
        return INNTEKTSMELDING.equals(this);
    }

    public boolean erEngangsstønadRelatert() {
        return ENGANGSSTØNAD_TYPER.contains(this);
    }

    public boolean erSvangerskapspengerRelatert() {
        return SØKNAD_SVANGERSKAPSPENGER.equals(this);
    }

    public boolean erSøknadType() {
        return SØKNAD_TYPER.contains(this) || ENDRING_SØKNAD_TYPER.contains(this);
    }
}
