package no.nav.foreldrepenger.mottak.domene;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Bruker;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.FoedselEllerAdopsjon;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

public class EngangsstønadSøknad extends MottattStrukturertDokument<SoeknadsskjemaEngangsstoenad> {

    EngangsstønadSøknad(SoeknadsskjemaEngangsstoenad skjema) {
        super(skjema); // NOSONAR
    }

    @Override
    protected void kopierVerdier(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        dataWrapper.setStrukturertDokument(true);
        Optional<String> aktørId = aktørIdFinder.apply(getPersonIdentifikator());
        if (!aktørId.isPresent()) {
            throw MeldingKonverteringFeil.FACTORY.finnerIkkeAktørId(this.getClass().getSimpleName()).toException();
        }
        dataWrapper.setAktørId(aktørId.get());
        getOmsorgsoverdragelseDato().ifPresent(dataWrapper::setOmsorgsovertakelsedato);
        getTerminDato().ifPresent(dataWrapper::setBarnTermindato);
        if (BehandlingTema.ENGANGSSTØNAD_FØDSEL.equals(dataWrapper.getBehandlingTema())) {
            getFødselsdato().ifPresent(dataWrapper::setBarnFodselsdato);
        } else {
            dataWrapper.setAdopsjonsbarnFodselsdatoer(getFodselsdagerForAdopsjonsbarn());
        }
        getAntallBarn().ifPresent(dataWrapper::setAntallBarn);
        getTerminBekreftelseDato().ifPresent(dataWrapper::setBarnTerminbekreftelsedato);
    }

    @Override
    protected void validerSkjemaSemantisk(MottakMeldingDataWrapper dataWrapper) {
        final BehandlingTema behandlingTema = hentBehandlingTema();
        if (!Objects.equals(dataWrapper.getBehandlingTema().getKode(), behandlingTema.getKode())) {
            throw MottakMeldingFeil.FACTORY.ulikBehandlingstemaKodeITynnMeldingOgSøknadsdokument(dataWrapper.getBehandlingTema().getKode(), behandlingTema.getKode()).toException();
        }
    }

    BehandlingTema hentBehandlingTema() {
        FoedselEllerAdopsjon foedselEllerAdopsjon = getSkjema().getSoknadsvalg().getFoedselEllerAdopsjon();
        BehandlingTema res;

        if (FoedselEllerAdopsjon.FOEDSEL.equals(foedselEllerAdopsjon)) {
            res = BehandlingTema.ENGANGSSTØNAD_FØDSEL;
        } else if (FoedselEllerAdopsjon.ADOPSJON.equals(foedselEllerAdopsjon)) {
            res = BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
        } else {
            res = BehandlingTema.UDEFINERT;
        }
        return res;
    }

    private String getPersonIdentifikator() {
        return ((Bruker) getSkjema().getBruker()).getPersonidentifikator();
    }

    private List<LocalDate> getFodselsdagerForAdopsjonsbarn() {
        final List<XMLGregorianCalendar> foedselsdato = getSkjema().getOpplysningerOmBarn().getFoedselsdato();
        if (foedselsdato.isEmpty()) {
            return Collections.emptyList();
        }
        return foedselsdato.stream().map(DateUtil::convertToLocalDate).collect(Collectors.toList());
    }

    private Optional<LocalDate> getFødselsdato() {
        final List<XMLGregorianCalendar> foedselsdato = getSkjema().getOpplysningerOmBarn().getFoedselsdato();
        if (foedselsdato.isEmpty()) {
            return Optional.empty();
        } else if (foedselsdato.stream().distinct().limit(2).count() > 1) {
            throw MottakMeldingFeil.FACTORY.merEnnEnFødselsdatoPåFødselsøknad().toException();
        }
        return Optional.of(DateUtil.convertToLocalDate(foedselsdato.get(0)));
    }

    private Optional<LocalDate> getTerminDato() {
        return Optional.ofNullable(DateUtil.convertToLocalDate(getSkjema().getOpplysningerOmBarn().getTermindato()));
    }

    private Optional<LocalDate> getTerminBekreftelseDato() {
        return Optional.ofNullable(DateUtil.convertToLocalDate(getSkjema().getOpplysningerOmBarn().getTerminbekreftelsedato()));
    }

    private Optional<LocalDate> getOmsorgsoverdragelseDato() {
        return Optional.ofNullable(DateUtil.convertToLocalDate(getSkjema().getOpplysningerOmBarn().getOmsorgsovertakelsedato()));
    }

    private Optional<Integer> getAntallBarn() {
        return Optional.of(getSkjema().getOpplysningerOmBarn().getAntallBarn());
    }
}
