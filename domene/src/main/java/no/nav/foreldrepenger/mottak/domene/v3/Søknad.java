package no.nav.foreldrepenger.mottak.domene.v3;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.domene.MeldingKonverteringFeil;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v3.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.AnnenForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.AnnenForelderMedNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Rettigheter;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.Svangerskapspenger;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.LukketPeriodeMedVedlegg;
import no.nav.vedtak.felles.xml.soeknad.v3.Soeknad;

public class Søknad extends MottattStrukturertDokument<Soeknad> {

    public Søknad(Soeknad skjema) {
        super(skjema);
    }

    @Override
    protected void kopierVerdier(MottakMeldingDataWrapper dataWrapper,
            Function<String, Optional<String>> aktørIdFinder) {
        dataWrapper.setStrukturertDokument(true);
        dataWrapper.setAktørId(getSkjema().getSoeker().getAktoerId());
        hentMottattDato(dataWrapper);

        hentFødselsdato().ifPresent(dataWrapper::setBarnFodselsdato);
        hentTermindato().ifPresent(dataWrapper::setBarnTermindato);
        hentOmsorgsovertakelsesdato().ifPresent(dataWrapper::setOmsorgsovertakelsedato);
        hentFørsteUttaksdag().ifPresent(dataWrapper::setFørsteUttakssdag);
        dataWrapper.setAdopsjonsbarnFodselsdatoer(hentFødselsdatoForAdopsjonsbarn());

        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Foreldrepenger) {
            AnnenForelder annenForelder = ((Foreldrepenger) ytelse).getAnnenForelder();
            if (annenForelder instanceof AnnenForelderMedNorskIdent) {
                dataWrapper.setAnnenPartId(((AnnenForelderMedNorskIdent) annenForelder).getAktoerId());
            }
            Rettigheter rettigheter = ((Foreldrepenger) ytelse).getRettigheter();
            if (rettigheter != null && rettigheter.isHarAnnenForelderRett()) {
                dataWrapper.setAnnenPartHarRett(true);
            }
        }
    }

    @Override
    protected void validerSkjemaSemantisk(MottakMeldingDataWrapper dataWrapper) {
        sjekkNødvendigeFeltEksisterer(dataWrapper.getForsendelseId().orElse(null));
        final BehandlingTema behandlingTema = hentBehandlingTema();
        final String aktørId = getSkjema().getSoeker().getAktoerId();
        if (!Objects.equals(dataWrapper.getBehandlingTema().getKode(), behandlingTema.getKode())) {
            throw MottakMeldingFeil.FACTORY.ulikBehandlingstemaKodeITynnMeldingOgSøknadsdokument(
                    dataWrapper.getBehandlingTema().getKode(), behandlingTema.getKode()).toException();
        }
        if (!Objects.equals(dataWrapper.getAktørId().orElse(null), aktørId)) {
            throw MottakMeldingFeil.FACTORY.ulikAktørIdITynnMeldingOgSøknadsdokument().toException();
        }
        if (getYtelse() instanceof Endringssoeknad) {
            final String saksnummer = ((Endringssoeknad) getYtelse()).getSaksnummer();
            if (!Objects.equals(dataWrapper.getSaksnummer().orElse(null), saksnummer)) {
                throw MottakMeldingFeil.FACTORY.ulikSaksnummerITynnmeldingOgSøknadsdokument(
                        dataWrapper.getSaksnummer().orElse(null), saksnummer).toException();
            }
        }
    }

    public Ytelse getYtelse() {
        final Ytelse ytelse = getSkjema().getOmYtelse().getAny().stream().filter(it -> it instanceof JAXBElement)
                .map(jb -> ((JAXBElement<?>) jb).getValue())
                .map(o -> (Ytelse) o).findFirst().orElse(null);

        if (ytelse != null) {
            return ytelse;
        }
        return getSkjema().getOmYtelse().getAny().stream().filter(it -> it instanceof Ytelse).map(o -> (Ytelse) o)
                .findFirst().orElse(null);
    }

    public void sjekkNødvendigeFeltEksisterer(UUID forsendelseId) {
        if (getSkjema().getMottattDato() == null || getSkjema().getOmYtelse() == null
                || getSkjema().getSoeker() == null) {
            throw MeldingKonverteringFeil.FACTORY.ukjentFormatPåSøknad(forsendelseId).toException();
        }
    }

    public BehandlingTema hentBehandlingTema() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Engangsstønad) {
            return utledBehandlingTemaES(((Engangsstønad) ytelse).getSoekersRelasjonTilBarnet());
        }
        if (ytelse instanceof Foreldrepenger) {
            return utledBehandlingTemaFP(((Foreldrepenger) ytelse).getRelasjonTilBarnet());
        }
        if (ytelse instanceof Endringssoeknad) {
            return BehandlingTema.FORELDREPENGER;
        }
        if (ytelse instanceof Svangerskapspenger) {
            return BehandlingTema.SVANGERSKAPSPENGER;
        }
        return BehandlingTema.UDEFINERT;

    }

    public void hentMottattDato(MottakMeldingDataWrapper wrapper) {
        Optional.ofNullable(getSkjema().getMottattDato()).ifPresent(mdato -> {
            if (wrapper.getForsendelseMottattTidspunkt().isEmpty() ||
                    wrapper.getForsendelseMottatt().isAfter(mdato)) {
                wrapper.setForsendelseMottattTidspunkt(mdato.atStartOfDay());
            }
        });
    }

    public Optional<LocalDate> hentTermindato() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Svangerskapspenger) {
            return Optional.ofNullable(((Svangerskapspenger) ytelse).getTermindato());
        }
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        if (relasjon instanceof Termin) {
            return Optional.ofNullable(((Termin) relasjon).getTermindato());
        }
        return Optional.empty();
    }

    public Optional<LocalDate> hentFødselsdato() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Svangerskapspenger) {
            return Optional.ofNullable(((Svangerskapspenger) ytelse).getFødselsdato());
        }
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        if (relasjon instanceof Foedsel) {
            return Optional.ofNullable(((Foedsel) relasjon).getFoedselsdato());
        }
        return Optional.empty();
    }

    public List<LocalDate> hentFødselsdatoForAdopsjonsbarn() {
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        List<LocalDate> datoList = Collections.emptyList();
        if (relasjon instanceof Adopsjon) {
            datoList = ((Adopsjon) relasjon).getFoedselsdato().stream().collect(Collectors.toList());
        }
        if (relasjon instanceof Omsorgsovertakelse) {
            datoList = ((Omsorgsovertakelse) relasjon).getFoedselsdato().stream().collect(Collectors.toList());
        }

        return datoList;
    }

    public Optional<LocalDate> hentOmsorgsovertakelsesdato() {
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        if (relasjon instanceof Omsorgsovertakelse) {
            return Optional.ofNullable(((Omsorgsovertakelse) relasjon).getOmsorgsovertakelsesdato());
        }
        if (relasjon instanceof Adopsjon) {
            return Optional.ofNullable(((Adopsjon) relasjon).getOmsorgsovertakelsesdato());
        }
        return Optional.empty();
    }

    public Optional<LocalDate> hentFørsteUttaksdag() {
        Fordeling fordeling = getFordeling();
        LocalDate dato = null;
        if (fordeling != null) {
            dato = fordeling.getPerioder().stream().filter(Objects::nonNull).map(LukketPeriodeMedVedlegg::getFom)
                    .min(LocalDate::compareTo).orElse(null);
        }
        return Optional.ofNullable(dato);
    }

    private SoekersRelasjonTilBarnet getRelasjonTilBarnet() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Foreldrepenger) {
            return ((Foreldrepenger) ytelse).getRelasjonTilBarnet();
        }
        if (ytelse instanceof Engangsstønad) {
            return ((Engangsstønad) ytelse).getSoekersRelasjonTilBarnet();
        }
        return null;
    }

    private BehandlingTema utledBehandlingTemaES(SoekersRelasjonTilBarnet relasjonTilBarnet) {
        if (relasjonTilBarnet instanceof Foedsel || relasjonTilBarnet instanceof Termin) {
            return BehandlingTema.ENGANGSSTØNAD_FØDSEL;
        }
        if (relasjonTilBarnet instanceof Adopsjon || relasjonTilBarnet instanceof Omsorgsovertakelse) {
            return BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
        }
        return BehandlingTema.ENGANGSSTØNAD;
    }

    private BehandlingTema utledBehandlingTemaFP(SoekersRelasjonTilBarnet relasjonTilBarnet) {
        if (relasjonTilBarnet instanceof Foedsel || relasjonTilBarnet instanceof Termin) {
            return BehandlingTema.FORELDREPENGER_FØDSEL;
        }
        if (relasjonTilBarnet instanceof Adopsjon || relasjonTilBarnet instanceof Omsorgsovertakelse) {
            return BehandlingTema.FORELDREPENGER_ADOPSJON;
        }
        return BehandlingTema.FORELDREPENGER;
    }

    private Fordeling getFordeling() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Foreldrepenger) {
            return ((Foreldrepenger) ytelse).getFordeling();
        }
        return null;
    }
}
