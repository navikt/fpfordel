package no.nav.foreldrepenger.mottak.domene.v3;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import jakarta.xml.bind.JAXBElement;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v3.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.AnnenForelderMedNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Brukerroller;
import no.nav.vedtak.felles.xml.soeknad.svangerskapspenger.v1.Svangerskapspenger;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.LukketPeriodeMedVedlegg;
import no.nav.vedtak.felles.xml.soeknad.v3.Soeknad;

public class Søknad extends MottattStrukturertDokument<Soeknad> {

    public Søknad(Soeknad skjema) {
        super(skjema);
    }

    private static BehandlingTema utledBehandlingTemaES(SoekersRelasjonTilBarnet relasjonTilBarnet) {
        if ((relasjonTilBarnet instanceof Foedsel) || (relasjonTilBarnet instanceof Termin)) {
            return BehandlingTema.ENGANGSSTØNAD_FØDSEL;
        }
        if ((relasjonTilBarnet instanceof Adopsjon) || (relasjonTilBarnet instanceof Omsorgsovertakelse)) {
            return BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
        }
        return BehandlingTema.ENGANGSSTØNAD;
    }

    private static BehandlingTema utledBehandlingTemaFP(SoekersRelasjonTilBarnet relasjonTilBarnet) {
        if ((relasjonTilBarnet instanceof Foedsel) || (relasjonTilBarnet instanceof Termin)) {
            return BehandlingTema.FORELDREPENGER_FØDSEL;
        }
        if ((relasjonTilBarnet instanceof Adopsjon) || (relasjonTilBarnet instanceof Omsorgsovertakelse)) {
            return BehandlingTema.FORELDREPENGER_ADOPSJON;
        }
        return BehandlingTema.FORELDREPENGER;
    }

    @Override
    protected void kopierVerdier(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        dataWrapper.setStrukturertDokument(true);
        dataWrapper.setAktørId(getSkjema().getSoeker().getAktoerId());
        hentBrukerroller().ifPresent(dataWrapper::setBrukerRolle);
        hentMottattDato(dataWrapper);

        hentFødselsdato().ifPresent(dataWrapper::setBarnFodselsdato);
        hentTermindato().ifPresent(dataWrapper::setBarnTermindato);
        hentOmsorgsovertakelsesdato().ifPresent(dataWrapper::setOmsorgsovertakelsedato);
        hentFørsteUttaksdag().ifPresent(dataWrapper::setFørsteUttakssdag);
        dataWrapper.setAdopsjonsbarnFodselsdatoer(hentFødselsdatoForAdopsjonsbarn());

        if (getYtelse() instanceof Foreldrepenger fp) {
            if (fp.getAnnenForelder() instanceof AnnenForelderMedNorskIdent a) {
                dataWrapper.setAnnenPartId(a.getAktoerId());
            }
            Optional.ofNullable(fp.getRettigheter()).filter(r -> r.isHarAnnenForelderRett()).ifPresent(r -> dataWrapper.setAnnenPartHarRett(true));

        }
    }

    @Override
    protected void validerSkjemaSemantisk(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        sjekkNødvendigeFeltEksisterer(dataWrapper.getForsendelseId().orElse(null));
        final BehandlingTema behandlingTema = hentBehandlingTema();
        final String aktørId = getSkjema().getSoeker().getAktoerId();
        if (!Objects.equals(dataWrapper.getBehandlingTema().getKode(), behandlingTema.getKode())) {
            throw new TekniskException("FP-404782",
                String.format("Ulik behandlingstemakode i tynnmelding (%s) og søknadsdokument (%s)", dataWrapper.getBehandlingTema().getKode(),
                    behandlingTema.getKode()));
        }
        if (!Objects.equals(dataWrapper.getAktørId().orElse(null), aktørId)) {
            throw new TekniskException("FP-502574",
                String.format("Ulik aktørId i tynnmelding (%s) og søknadsdokument (%s)", dataWrapper.getArkivId(), aktørId));
        }
        if (getYtelse() instanceof Endringssoeknad endringssoeknad) {
            final var saksnummer = endringssoeknad.getSaksnummer();
            if (!Objects.equals(dataWrapper.getSaksnummer().orElse(null), saksnummer)) {
                throw new FunksjonellException("FP-401245",
                    String.format("Ulike saksnummer i melding/VL (%s) og endringssøknad (%s).", dataWrapper.getSaksnummer().orElse(null), saksnummer),
                    null);
            }
        }
    }

    private Optional<String> hentBrukerroller() {
        return Optional.ofNullable(getSkjema().getSoeker()).map(Bruker::getSoeknadsrolle).map(Brukerroller::getKode);
    }

    public Ytelse getYtelse() {
        final Ytelse ytelse = getSkjema().getOmYtelse()
            .getAny()
            .stream()
            .filter(JAXBElement.class::isInstance)
            .map(jb -> ((JAXBElement<?>) jb).getValue())
            .map(o -> (Ytelse) o)
            .findFirst()
            .orElse(null);

        if (ytelse != null) {
            return ytelse;
        }
        return getSkjema().getOmYtelse().getAny().stream().filter(Ytelse.class::isInstance).map(o -> (Ytelse) o).findFirst().orElse(null);
    }

    public void sjekkNødvendigeFeltEksisterer(UUID forsendelseId) {
        if ((getSkjema().getMottattDato() == null) || (getSkjema().getOmYtelse() == null) || (getSkjema().getSoeker() == null)) {
            throw new TekniskException("FP-874812",
                String.format("Ukjent format på søknad eller mangler nødvendig element (Forsendelse med ID: %s)", forsendelseId));
        }
    }

    public BehandlingTema hentBehandlingTema() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Engangsstønad e) {
            return utledBehandlingTemaES(e.getSoekersRelasjonTilBarnet());
        }
        if (ytelse instanceof Foreldrepenger f) {
            return utledBehandlingTemaFP(f.getRelasjonTilBarnet());
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
            if (wrapper.getForsendelseMottattTidspunkt().isEmpty() || wrapper.getForsendelseMottatt().isAfter(mdato)) {
                wrapper.setForsendelseMottattTidspunkt(mdato.atStartOfDay());
            }
        });
    }

    public Optional<LocalDate> hentTermindato() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Svangerskapspenger svangerskapspenger) {
            return Optional.ofNullable(svangerskapspenger.getTermindato());
        }
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        if (relasjon instanceof Termin termin) {
            return Optional.ofNullable(termin.getTermindato());
        }
        return Optional.empty();
    }

    public Optional<LocalDate> hentFødselsdato() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Svangerskapspenger svangerskapspenger) {
            return Optional.ofNullable(svangerskapspenger.getFødselsdato());
        }
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        if (relasjon instanceof Foedsel fødsel) {
            return Optional.ofNullable(fødsel.getFoedselsdato());
        }
        return Optional.empty();
    }

    public List<LocalDate> hentFødselsdatoForAdopsjonsbarn() {
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        List<LocalDate> datoList = Collections.emptyList();
        if (relasjon instanceof Adopsjon adopsjon) {
            datoList = new ArrayList<>(adopsjon.getFoedselsdato());
        }
        if (relasjon instanceof Omsorgsovertakelse overtakelse) {
            datoList = new ArrayList<>(overtakelse.getFoedselsdato());
        }

        return datoList;
    }

    public Optional<LocalDate> hentOmsorgsovertakelsesdato() {
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        if (relasjon instanceof Omsorgsovertakelse overtakelse) {
            return Optional.ofNullable(overtakelse.getOmsorgsovertakelsesdato());
        }
        if (relasjon instanceof Adopsjon adopsjon) {
            return Optional.ofNullable(adopsjon.getOmsorgsovertakelsesdato());
        }
        return Optional.empty();
    }

    public Optional<LocalDate> hentFørsteUttaksdag() {
        Fordeling fordeling = getFordeling();
        LocalDate dato = null;
        if (fordeling != null) {
            dato = fordeling.getPerioder()
                .stream()
                .filter(Objects::nonNull)
                .map(LukketPeriodeMedVedlegg::getFom)
                .min(LocalDate::compareTo)
                .orElse(null);
        }
        return Optional.ofNullable(dato);
    }

    private SoekersRelasjonTilBarnet getRelasjonTilBarnet() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Foreldrepenger foreldrepenger) {
            return foreldrepenger.getRelasjonTilBarnet();
        }
        if (ytelse instanceof Engangsstønad engangsstønad) {
            return engangsstønad.getSoekersRelasjonTilBarnet();
        }
        return null;
    }

    private Fordeling getFordeling() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Foreldrepenger foreldrepenger) {
            return foreldrepenger.getFordeling();
        }
        return null;
    }
}
