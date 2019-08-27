package no.nav.foreldrepenger.mottak.domene.v1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.mottak.domene.MeldingKonverteringFeil;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v1.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v1.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelder;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.AnnenForelderMedNorskIdent;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Rettigheter;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v1.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v1.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.uttak.v1.LukketPeriodeMedVedlegg;
import no.nav.vedtak.felles.xml.soeknad.v1.Soeknad;

public class Søknad extends MottattStrukturertDokument<Soeknad> {

    public Søknad(Soeknad skjema) {
        super(skjema);
    }

    @Override
    protected void kopierVerdier(MottakMeldingDataWrapper dataWrapper, Function<String, Optional<String>> aktørIdFinder) {
        dataWrapper.setStrukturertDokument(true);
        dataWrapper.setAktørId(getSkjema().getSoeker().getAktoerId());
        dataWrapper.setForsendelseMottattTidspunkt(hentMottattDato());

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
            if(rettigheter!=null && rettigheter.isHarAnnenForelderRett()){
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
            throw MottakMeldingFeil.FACTORY.ulikBehandlingstemaKodeITynnMeldingOgSøknadsdokument(dataWrapper.getBehandlingTema().getKode(), behandlingTema.getKode()).toException();
        }
        if (!Objects.equals(dataWrapper.getAktørId().orElse(null), aktørId)) {
            throw MottakMeldingFeil.FACTORY.ulikAktørIdITynnMeldingOgSøknadsdokument().toException();
        }
        if (getYtelse() instanceof Endringssoeknad) {
            final String saksnummer = ((Endringssoeknad) getYtelse()).getSaksnummer();
            if (!Objects.equals(dataWrapper.getSaksnummer().orElse(null), saksnummer)) {
                throw MottakMeldingFeil.FACTORY.ulikSaksnummerITynnmeldingOgSøknadsdokument(dataWrapper.getSaksnummer().orElse(null), saksnummer).toException();
            }
        }
    }

    public Ytelse getYtelse() {
        final Ytelse ytelse = getSkjema().getOmYtelse().getAny().stream()
                .filter(it -> it instanceof JAXBElement)
                .map(jb -> ((JAXBElement<?>) jb).getValue())
                .map(o -> (Ytelse) o)
                .findFirst()
                .orElse(null);

        if (ytelse != null) {
            return ytelse;
        }
        return getSkjema().getOmYtelse().getAny().stream()
                .filter(it -> it instanceof Ytelse)
                .map(o -> (Ytelse) o)
                .findFirst()
                .orElse(null);
    }

    public void sjekkNødvendigeFeltEksisterer(UUID forsendelseId) {
        if (getSkjema().getMottattDato() == null || getSkjema().getOmYtelse() == null || getSkjema().getSoeker() == null) {
            throw MeldingKonverteringFeil.FACTORY.ukjentFormatPåSøknad(forsendelseId).toException();
        }
    }

    public BehandlingTema hentBehandlingTema() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Engangsstønad) { // NOSONAR
            return utledBehandlingTemaES(((Engangsstønad) ytelse).getSoekersRelasjonTilBarnet());
        } else if (ytelse instanceof Foreldrepenger) { // NOSONAR
            return utledBehandlingTemaFP(((Foreldrepenger) ytelse).getRelasjonTilBarnet());
        } else if (ytelse instanceof Endringssoeknad) { // NOSONAR
            return BehandlingTema.FORELDREPENGER;
        } else {
            return BehandlingTema.UDEFINERT;
        }
    }

    public LocalDateTime hentMottattDato() {
        return konverterTilLocalDateTime(getSkjema().getMottattDato());
    }

    public Optional<LocalDate> hentTermindato() {
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        if (relasjon instanceof Termin) { // NOSONAR
            return Optional.ofNullable(konverterTilLocalDate(((Termin) relasjon).getTermindato()));
        }
        return Optional.empty();
    }

    public Optional<LocalDate> hentFødselsdato() {
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        if (relasjon instanceof Foedsel) { // NOSONAR
            return Optional.ofNullable(konverterTilLocalDate(((Foedsel) relasjon).getFoedselsdato()));
        }
        return Optional.empty();
    }

    public List<LocalDate> hentFødselsdatoForAdopsjonsbarn() {
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        List<LocalDate> datoList = Collections.emptyList();
        if (relasjon instanceof Adopsjon) { // NOSONAR
            datoList = ((Adopsjon) relasjon).getFoedselsdato().stream().map(DateUtil::convertToLocalDate).collect(Collectors.toList());
        } else if (relasjon instanceof Omsorgsovertakelse) { // NOSONAR
            datoList = ((Omsorgsovertakelse) relasjon).getFoedselsdato().stream().map(DateUtil::convertToLocalDate).collect(Collectors.toList());
        }

        return datoList;
    }

    public Optional<LocalDate> hentOmsorgsovertakelsesdato() {
        SoekersRelasjonTilBarnet relasjon = getRelasjonTilBarnet();
        if (relasjon instanceof Omsorgsovertakelse) { // NOSONAR
            return Optional.ofNullable(konverterTilLocalDate(((Omsorgsovertakelse) relasjon).getOmsorgsovertakelsesdato()));
        } else if (relasjon instanceof Adopsjon) { // NOSONAR
            return Optional.ofNullable(konverterTilLocalDate(((Adopsjon) relasjon).getOmsorgsovertakelsesdato()));
        }
        return Optional.empty();
    }

    public Optional<LocalDate> hentFørsteUttaksdag() {
        Fordeling fordeling = getFordeling();
        LocalDate dato = null;
        if (fordeling != null) {
            dato = fordeling.getPerioder().stream()
                    .filter(Objects::nonNull)
                    .map(LukketPeriodeMedVedlegg::getFom)
                    .map(this::konverterTilLocalDate)
                    .min(LocalDate::compareTo).orElse(null);
        }
        return Optional.ofNullable(dato);
    }

    private SoekersRelasjonTilBarnet getRelasjonTilBarnet() {
        Ytelse ytelse = getYtelse();
        if (ytelse instanceof Foreldrepenger) { // NOSONAR
            return ((Foreldrepenger) ytelse).getRelasjonTilBarnet();
        }
        if (ytelse instanceof Engangsstønad) { // NOSONAR
            return ((Engangsstønad) ytelse).getSoekersRelasjonTilBarnet();
        }
        return null;
    }

    private BehandlingTema utledBehandlingTemaES(SoekersRelasjonTilBarnet relasjonTilBarnet) {
        if (relasjonTilBarnet instanceof Foedsel || relasjonTilBarnet instanceof Termin) { // NOSONAR
            return BehandlingTema.ENGANGSSTØNAD_FØDSEL;
        } else if (relasjonTilBarnet instanceof Adopsjon || relasjonTilBarnet instanceof Omsorgsovertakelse) { // NOSONAR
            return BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
        }
        return BehandlingTema.ENGANGSSTØNAD;
    }

    private BehandlingTema utledBehandlingTemaFP(SoekersRelasjonTilBarnet relasjonTilBarnet) {
        if (relasjonTilBarnet instanceof Foedsel || relasjonTilBarnet instanceof Termin) { // NOSONAR
            return BehandlingTema.FORELDREPENGER_FØDSEL;
        } else if (relasjonTilBarnet instanceof Adopsjon || relasjonTilBarnet instanceof Omsorgsovertakelse) { // NOSONAR
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

    private LocalDate konverterTilLocalDate(XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar == null) {
            return null;
        }
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDate();
    }

    private LocalDateTime konverterTilLocalDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
        if (xmlGregorianCalendar == null) {
            return null;
        }
        return xmlGregorianCalendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
    }
}
