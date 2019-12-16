package no.nav.foreldrepenger.mottak.domene.v2;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.domene.MeldingKonverteringFeil;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.seres.xsd.nav.inntektsmelding_m._20181211.Arbeidsgiver;
import no.seres.xsd.nav.inntektsmelding_m._20181211.ArbeidsgiverPrivat;
import no.seres.xsd.nav.inntektsmelding_m._20181211.InntektsmeldingM;
import no.seres.xsd.nav.inntektsmelding_m._20181211.Skjemainnhold;

public class Inntektsmelding extends MottattStrukturertDokument<InntektsmeldingM> {

    private static final Logger LOG = LoggerFactory.getLogger(Inntektsmelding.class);

    public Inntektsmelding(InntektsmeldingM skjema) {
        super(skjema);
    }

    @Override
    protected void kopierVerdier(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        kopierAktørTilMottakWrapper(dataWrapper, aktørIdFinder);
        dataWrapper.setÅrsakTilInnsending(getÅrsakTilInnsending());
        getVirksomhetsnummer().ifPresent(dataWrapper::setVirksomhetsnummer);
        getArbeidsgiverAktørId(aktørIdFinder).ifPresent(dataWrapper::setArbeidsgiverAktørId);
        getArbeidsforholdsid().ifPresent(dataWrapper::setArbeidsforholdsid);
        dataWrapper.setFørsteUttakssdag(getStartdatoForeldrepengeperiode());
        dataWrapper.setInntekstmeldingStartdato(getStartdatoForeldrepengeperiode());
        dataWrapper.setInntektsmeldingYtelse(getYtelse());
    }

    private Optional<String> getArbeidsgiverAktørId(Function<String, Optional<String>> aktørIdFinder) {
        JAXBElement<ArbeidsgiverPrivat> arbeidsgiver = getSkjema().getSkjemainnhold().getArbeidsgiverPrivat();
        if (arbeidsgiver != null && arbeidsgiver.getValue() != null) {
            return aktørIdFinder.apply(arbeidsgiver.getValue().getArbeidsgiverFnr());
        }
        return Optional.empty();
    }

    public void kopierAktørTilMottakWrapper(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        Optional<String> aktørId = aktørIdFinder.apply(getArbeidstakerFnr());
        if (aktørId.isEmpty()) {
            MeldingKonverteringFeil.FACTORY.finnerIkkeAktørId(this.getClass().getSimpleName()).log(LOG);
        }
        dataWrapper.setAktørId(aktørId.get());
    }

    @Override
    protected void validerSkjemaSemantisk(MottakMeldingDataWrapper dataWrapper) {
        return;
    }

    private LocalDate getStartdatoForeldrepengeperiode() {
        final Skjemainnhold skjemainnhold = getSkjema().getSkjemainnhold();
        if (skjemainnhold == null) {
            return null;
        }
        final JAXBElement<LocalDate> startdatoForeldrepengeperiode = skjemainnhold.getStartdatoForeldrepengeperiode();
        if (startdatoForeldrepengeperiode == null) {
            return null;
        }
        return startdatoForeldrepengeperiode.getValue();
    }

    // FIXME (GS) Disse to verdiene må bli kodeverk her og i fpsak, men hardkodes nå som string for å unngå duplisering av kodeverk og migrering. Det må vel finnes
    // en bedre løsning enn duplisering
    private String getÅrsakTilInnsending() {
        return getSkjema().getSkjemainnhold().getAarsakTilInnsending();
    }

    private String getArbeidstakerFnr() {
        return getSkjema().getSkjemainnhold().getArbeidstakerFnr();
    }

    private Optional<String> getVirksomhetsnummer() {
        Skjemainnhold skjemainnhold = getSkjema().getSkjemainnhold();
        if (null == skjemainnhold) {
            return Optional.empty();
        }
        JAXBElement<Arbeidsgiver> arbeidsgiver = skjemainnhold.getArbeidsgiver();
        if (null == arbeidsgiver) {
            return Optional.empty();
        }
        return Optional.ofNullable(arbeidsgiver.getValue().getVirksomhetsnummer());
    }

    private String getYtelse() {
        return getSkjema().getSkjemainnhold().getYtelse();
    }

    private Optional<String> getArbeidsforholdsid() {
        if (getSkjema().getSkjemainnhold().getArbeidsforhold() != null
                && getSkjema().getSkjemainnhold().getArbeidsforhold().getValue().getArbeidsforholdId() != null) {
            return Optional.ofNullable(getSkjema().getSkjemainnhold().getArbeidsforhold().getValue().getArbeidsforholdId().getValue());
        }
        return Optional.empty();
    }

}
