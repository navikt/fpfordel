package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;


/**
 * DokumentTypeId er et kodeverk som forvaltes av Kodeverkforvaltning. Det er et subsett av kodeverket DokumentType,  mer spesifikt alle inngående dokumenttyper.
 */
@Entity(name = "DokumentTypeId")
@DiscriminatorValue(DokumentTypeId.DISCRIMINATOR)
public class DokumentTypeId extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_TYPE_ID";

    // Engangsstønad
    public static final DokumentTypeId SØKNAD_ENGANGSSTØNAD_FØDSEL = new DokumentTypeId("SØKNAD_ENGANGSSTØNAD_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId SØKNAD_ENGANGSSTØNAD_ADOPSJON = new DokumentTypeId("SØKNAD_ENGANGSSTØNAD_ADOPSJON"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL = new DokumentTypeId("ETTERSENDT_SØKNAD_ENGANGSSTØNAD_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON = new DokumentTypeId("ETTERSENDT_SØKNAD_ENGANGSSTØNAD_ADOPSJON"); //$NON-NLS-1$

    // Foreldrepenger
    public static final DokumentTypeId SØKNAD_FORELDREPENGER_FØDSEL = new DokumentTypeId("SØKNAD_FORELDREPENGER_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId SØKNAD_FORELDREPENGER_ADOPSJON = new DokumentTypeId("SØKNAD_FORELDREPENGER_ADOPSJON"); //$NON-NLS-1$
    public static final DokumentTypeId FORELDREPENGER_ENDRING_SØKNAD = new DokumentTypeId("FORELDREPENGER_ENDRING_SØKNAD"); //$NON-NLS-1$
    public static final DokumentTypeId FLEKSIBELT_UTTAK_FORELDREPENGER = new DokumentTypeId("FLEKSIBELT_UTTAK_FORELDREPENGER"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON = new DokumentTypeId("ETTERSENDT_SØKNAD_FORELDREPENGER_ADOPSJON"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL = new DokumentTypeId("ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_FLEKSIBELT_UTTAK_FORELDREPENGER = new DokumentTypeId("ETTERSENDT_SØKNAD_FORELDREPENGER_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD = new DokumentTypeId("ETTERSENDT_FORELDREPENGER_ENDRING_SØKNAD"); //$NON-NLS-1$

    // Svangerskapspenger
    public static final DokumentTypeId SØKNAD_SVANGERSKAPSPENGER = new DokumentTypeId("SØKNAD_SVANGERSKAPSPENGER"); //$NON-NLS-1$

    // Støttedokumenter FP
    public static final DokumentTypeId INNTEKTSMELDING = new DokumentTypeId("INNTEKTSMELDING"); //$NON-NLS-1$
    public static final DokumentTypeId INNTEKTSOPPLYSNINGER = new DokumentTypeId("INNTEKTSOPPLYSNINGER"); //$NON-NLS-1$

    // Klage
    public static final DokumentTypeId KLAGE_DOKUMENT = new DokumentTypeId("KLAGE_DOKUMENT"); //$NON-NLS-1$

    // Tilbakekreving
    public static final DokumentTypeId TILBAKEKREV_UTTALELSE = new DokumentTypeId("TILBAKEKREV_UTTALELSE"); //$NON-NLS-1$

    // Uspesifikke dokumenter
    public static final DokumentTypeId DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL = new DokumentTypeId("DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL"); //$NON-NLS-1$
    public static final DokumentTypeId DOKUMENTASJON_AV_OMSORGSOVERTAKELSE = new DokumentTypeId("DOKUMENTASJON_AV_OMSORGSOVERTAKELSE"); //$NON-NLS-1$
    public static final DokumentTypeId ANNET = new DokumentTypeId("ANNET"); //$NON-NLS-1$

    public static final DokumentTypeId UDEFINERT = new DokumentTypeId("-"); //$NON-NLS-1$


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
        // Hibernate trenger en
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
