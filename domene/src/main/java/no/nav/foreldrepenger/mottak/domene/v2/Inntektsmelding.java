package no.nav.foreldrepenger.mottak.domene.v2;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.seres.xsd.nav.inntektsmelding_m._20181211.Arbeidsforhold;
import no.seres.xsd.nav.inntektsmelding_m._20181211.Arbeidsgiver;
import no.seres.xsd.nav.inntektsmelding_m._20181211.ArbeidsgiverPrivat;
import no.seres.xsd.nav.inntektsmelding_m._20181211.Avsendersystem;
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
        getInnsendingstidspunkt().ifPresent(dataWrapper::setForsendelseMottattTidspunkt);
        dataWrapper.setFørsteUttakssdag(getStartdatoForeldrepengeperiode());
        dataWrapper.setInntekstmeldingStartdato(getStartdatoForeldrepengeperiode());
        dataWrapper.setInntektsmeldingYtelse(getYtelse());
    }

    private Optional<String> getArbeidsgiverAktørId(Function<String, Optional<String>> aktørIdFinder) {
        JAXBElement<ArbeidsgiverPrivat> arbeidsgiver = getSkjema().getSkjemainnhold().getArbeidsgiverPrivat();
        if ((arbeidsgiver != null) && (arbeidsgiver.getValue() != null)) {
            return aktørIdFinder.apply(arbeidsgiver.getValue().getArbeidsgiverFnr());
        }
        return Optional.empty();
    }

    public void kopierAktørTilMottakWrapper(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        Optional<String> aktørId = aktørIdFinder.apply(getArbeidstakerFnr());
        if (aktørId.isEmpty()) {
            LOG.warn(new TekniskException("FP-513732",
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
                throw new FunksjonellException("FP-401246", "Ulike personer i journalpost og inntektsmelding.", null);
            }
        }
    }

    private LocalDate getStartdatoForeldrepengeperiode() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold())
            .map(Skjemainnhold::getStartdatoForeldrepengeperiode)
            .map(JAXBElement::getValue)
            .orElse(null);
    }

    private String getÅrsakTilInnsending() {
        return getSkjema().getSkjemainnhold().getAarsakTilInnsending();
    }

    private String getArbeidstakerFnr() {
        return getSkjema().getSkjemainnhold().getArbeidstakerFnr();
    }

    private Optional<String> getVirksomhetsnummer() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold())
            .map(Skjemainnhold::getArbeidsgiver)
            .map(JAXBElement::getValue)
            .map(Arbeidsgiver::getVirksomhetsnummer);
    }

    private String getYtelse() {
        return getSkjema().getSkjemainnhold().getYtelse();
    }

    private Optional<String> getArbeidsforholdsid() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold())
            .map(Skjemainnhold::getArbeidsforhold)
            .map(JAXBElement::getValue)
            .map(Arbeidsforhold::getArbeidsforholdId)
            .map(JAXBElement::getValue);
    }

    private Optional<LocalDateTime> getInnsendingstidspunkt() {
        return Optional.ofNullable(getSkjema().getSkjemainnhold())
            .map(Skjemainnhold::getAvsendersystem)
            .map(Avsendersystem::getInnsendingstidspunkt)
            .map(JAXBElement::getValue);
    }

}
