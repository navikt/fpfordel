package no.nav.foreldrepenger.mottak.domene;

import java.util.Optional;
import java.util.function.Function;

import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;

public abstract class MottattStrukturertDokument<S> {

    private S skjema;

    protected MottattStrukturertDokument(S skjema) {
        this.skjema = skjema;
    }

    @SuppressWarnings("rawtypes")
    public static MottattStrukturertDokument toXmlWrapper(Object skjema) {
        if (skjema instanceof no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM) { // NOSONAR
            return new no.nav.foreldrepenger.mottak.domene.v1.Inntektsmelding((no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM) skjema);
        }
        if (skjema instanceof no.seres.xsd.nav.inntektsmelding_m._20181211.InntektsmeldingM) { // NOSONAR
            return new no.nav.foreldrepenger.mottak.domene.v2.Inntektsmelding((no.seres.xsd.nav.inntektsmelding_m._20181211.InntektsmeldingM) skjema);
        }
        if (skjema instanceof no.nav.vedtak.felles.xml.soeknad.v3.Soeknad) { // NOSONAR Dto plukker ut info for foreldrepenger, engangsstønad og
                                                                             // endringssøknad
            return new no.nav.foreldrepenger.mottak.domene.v3.Søknad((no.nav.vedtak.felles.xml.soeknad.v3.Soeknad) skjema);
        }

        throw MeldingKonverteringFeil.FACTORY.ukjentSkjemaType(skjema.getClass().getCanonicalName()).toException();
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
