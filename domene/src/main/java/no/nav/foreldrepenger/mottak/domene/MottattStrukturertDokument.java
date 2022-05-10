package no.nav.foreldrepenger.mottak.domene;

import java.util.Optional;
import java.util.function.Function;

import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.exception.TekniskException;

public abstract class MottattStrukturertDokument<S> {

    private S skjema;

    protected MottattStrukturertDokument(S skjema) {
        this.skjema = skjema;
    }

    @SuppressWarnings("rawtypes")
    public static MottattStrukturertDokument toXmlWrapper(Object skjema) {
        if (skjema instanceof no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM s) {
            return new no.nav.foreldrepenger.mottak.domene.v1.Inntektsmelding(s);
        }
        if (skjema instanceof no.seres.xsd.nav.inntektsmelding_m._20181211.InntektsmeldingM s) { // NOSONAR
            return new no.nav.foreldrepenger.mottak.domene.v2.Inntektsmelding(s);
        }
        if (skjema instanceof no.nav.vedtak.felles.xml.soeknad.v3.Soeknad s) { // NOSONAR Dto plukker ut info for foreldrepenger, engangsstønad og
                                                                               // endringssøknad
            return new no.nav.foreldrepenger.mottak.domene.v3.Søknad(s);
        }

        throw new TekniskException("FP-947143", String.format("Ukjent meldingstype %s", skjema.getClass().getCanonicalName()));
    }

    public final void kopierTilMottakWrapper(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        validerSkjemaSemantisk(dataWrapper);
        kopierVerdier(dataWrapper, aktørIdFinder);
    }

    /**
     * Les nødvendige felter fra meldingen og kopier til angitt wrapper. Denne
     * kalles etter semantisk validering av skjemaet gjennom
     * <code>validerSkjemaSemantisk()</code>.
     *
     * @param dataWrapper data holder som skal populeres med verdier fra skjema
     */
    protected abstract void kopierVerdier(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder);

    /**
     * Syntaktisk validering: validering av skjema mot XSD skal allerede være gjort
     * ved lesing av xml.
     * <p>
     * Semantisk validering: hvis det er ting som må/bør valideres/sjekkes før data
     * sendes videre, gjøres det her. Dette betyr blant annent konsistentsjekk av
     * data mellom angitt {@link MottakMeldingDataWrapper} og skjema
     * <p>
     * Hvis ingen slik validering er nødvendig, kan du bare returne.
     *
     * @param dataWrapper data holder som skal populeres med verdier fra skjema
     */
    protected abstract void validerSkjemaSemantisk(MottakMeldingDataWrapper dataWrapper);

    public S getSkjema() {
        return skjema;
    }

}
