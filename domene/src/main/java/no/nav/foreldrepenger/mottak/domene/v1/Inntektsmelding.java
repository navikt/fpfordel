package no.nav.foreldrepenger.mottak.domene.v1;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.seres.xsd.nav.inntektsmelding_m._20180924.InntektsmeldingM;
import no.seres.xsd.nav.inntektsmelding_m._20180924.Skjemainnhold;

public class Inntektsmelding extends MottattStrukturertDokument<InntektsmeldingM> {

    private static final Logger log = LoggerFactory.getLogger(Inntektsmelding.class);

    public Inntektsmelding(InntektsmeldingM skjema) {
        super(skjema);
    }

    @Override
    protected void kopierVerdier(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        kopierAktørTilMottakWrapper(dataWrapper, aktørIdFinder);
        dataWrapper.setÅrsakTilInnsending(getÅrsakTilInnsending());
        dataWrapper.setVirksomhetsnummer(getVirksomhetsnummer());
        getArbeidsforholdsid().ifPresent(dataWrapper::setArbeidsforholdsid);
        dataWrapper.setFørsteUttakssdag(getStartdatoForeldrepengeperiode());
        dataWrapper.setInntekstmeldingStartdato(getStartdatoForeldrepengeperiode());
        dataWrapper.setInntektsmeldingYtelse(getYtelse());
    }

    public void kopierAktørTilMottakWrapper(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        Optional<String> aktørId = aktørIdFinder.apply(getArbeidstakerFnr());
        if (aktørId.isEmpty()) {
            log.warn(new TekniskException("FP-513732",
                String.format("Finner ikke aktørID for bruker på %s", this.getClass().getSimpleName())).getMessage());
        }
        aktørId.ifPresent(dataWrapper::setAktørId);
    }

    @Override
    protected void validerSkjemaSemantisk(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        Optional<String> aktørIdFraSkjema = aktørIdFinder.apply(getArbeidstakerFnr());
        Optional<String> aktørIdFraJournalpost = dataWrapper.getAktørId();
        if (aktørIdFraJournalpost.isPresent()) {
            if (aktørIdFraSkjema.filter(aktørIdFraJournalpost.get()::equals).isEmpty()) {
                throw new FunksjonellException("FP-401245", "Ulike personer i journalpost og inntektsmelding.", null);
            }
        }
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
        if ((getSkjema().getSkjemainnhold().getArbeidsforhold() != null) && (
            getSkjema().getSkjemainnhold().getArbeidsforhold().getValue().getArbeidsforholdId() != null)) {
            return Optional.ofNullable(getSkjema().getSkjemainnhold().getArbeidsforhold().getValue().getArbeidsforholdId().getValue());
        }
        return Optional.empty();
    }
}
