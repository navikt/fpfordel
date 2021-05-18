package no.nav.foreldrepenger.mottak.domene.v1;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

import javax.xml.bind.JAXBElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.exception.TekniskException;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Skjemainnhold;

public class Inntektsmelding extends MottattStrukturertDokument<InntektsmeldingM> {

    private static final Logger log = LoggerFactory.getLogger(Inntektsmelding.class);

    public Inntektsmelding(InntektsmeldingM skjema) {
        super(skjema);
    }

    @Override
    protected void kopierVerdier(MottakMeldingDataWrapper dataWrapper,
            Function<String, Optional<String>> aktørIdFinder) {
        kopierAktørTilMottakWrapper(dataWrapper, aktørIdFinder);
        dataWrapper.setÅrsakTilInnsending(getÅrsakTilInnsending());
        dataWrapper.setVirksomhetsnummer(getVirksomhetsnummer());
        getArbeidsforholdsid().ifPresent(dataWrapper::setArbeidsforholdsid);
        dataWrapper.setFørsteUttakssdag(getStartdatoForeldrepengeperiode());
        dataWrapper.setInntekstmeldingStartdato(getStartdatoForeldrepengeperiode());
        dataWrapper.setInntektsmeldingYtelse(getYtelse());
    }

    public void kopierAktørTilMottakWrapper(MottakMeldingDataWrapper dataWrapper,
            Function<String, Optional<String>> aktørIdFinder) {
        Optional<String> aktørId = aktørIdFinder.apply(getArbeidstakerFnr());
        if (aktørId.isEmpty()) {
            log.warn(new TekniskException("FP-513732", String.format("Finner ikke aktørID for bruker på %s", this.getClass().getSimpleName())).getMessage());
        }
        aktørId.ifPresent(dataWrapper::setAktørId);
    }

    @Override
    protected void validerSkjemaSemantisk(MottakMeldingDataWrapper dataWrapper) {
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

    // FIXME (GS) Disse to verdiene må bli kodeverk her og i fpsak, men hardkodes nå
    // som string for å unngå duplisering av kodeverk og migrering. Det må vel
    // finnes
    // en bedre løsning enn duplisering
    private String getÅrsakTilInnsending() {
        return getSkjema().getSkjemainnhold().getAarsakTilInnsending();
    }

    private String getArbeidstakerFnr() {
        return getSkjema().getSkjemainnhold().getArbeidstakerFnr();
    }

    private String getVirksomhetsnummer() {
        return getSkjema().getSkjemainnhold().getArbeidsgiver().getVirksomhetsnummer();
    }

    private String getYtelse() {
        return getSkjema().getSkjemainnhold().getYtelse();
    }

    private Optional<String> getArbeidsforholdsid() {
        if ((getSkjema().getSkjemainnhold().getArbeidsforhold() != null)
                && (getSkjema().getSkjemainnhold().getArbeidsforhold().getValue().getArbeidsforholdId() != null)) {
            return Optional.ofNullable(
                    getSkjema().getSkjemainnhold().getArbeidsforhold().getValue().getArbeidsforholdId().getValue());
        }
        return Optional.empty();
    }
}
