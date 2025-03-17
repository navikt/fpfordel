package no.nav.foreldrepenger.journalføring.oppgave;

import static no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste.NK_ENHET_ID;
import static no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste.SKJERMINGENHETER;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.domene.NyOppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus;
import no.nav.foreldrepenger.journalføring.oppgave.lager.AktørId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveRepository;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;
import no.nav.foreldrepenger.journalføring.utils.YtelseTypeUtils;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.behandlendeenhet.LosEnheterCachedTjeneste;
import no.nav.foreldrepenger.mottak.klient.TilhørendeEnhetDto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.OpprettOppgave;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;

@Dependent
class OppgaverTjeneste implements Journalføringsoppgave {

    private static final Logger LOG = LoggerFactory.getLogger(OppgaverTjeneste.class);
    protected static final String LIMIT = "50";
    protected static final int FRIST_DAGER = 1;

    private OppgaveRepository oppgaveRepository;
    private Oppgaver oppgaveKlient;
    private EnhetsTjeneste enhetsTjeneste;
    private LosEnheterCachedTjeneste losEnheterCachedTjeneste;
    private PersonInformasjon personTjeneste;

    @Inject
    public OppgaverTjeneste(OppgaveRepository oppgaveRepository, Oppgaver oppgaveKlient, EnhetsTjeneste enhetsTjeneste,
                            LosEnheterCachedTjeneste losEnheterCachedTjeneste, PersonInformasjon personTjeneste) {
        this.oppgaveRepository = oppgaveRepository;
        this.oppgaveKlient = oppgaveKlient;
        this.enhetsTjeneste = enhetsTjeneste;
        this.losEnheterCachedTjeneste = losEnheterCachedTjeneste;
        this.personTjeneste = personTjeneste;
    }

    @Override
    public String opprettGosysJournalføringsoppgaveFor(NyOppgave nyOppgave) {
        var erSystem = !KontekstHolder.harKontekst() || KontekstHolder.getKontekst().getIdentType().erSystem();
        var request = OpprettOppgave.getBuilderTemaFOR(Oppgavetype.JOURNALFØRING, no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet.NORM,
                FRIST_DAGER)
            .medAktoerId(Optional.ofNullable(nyOppgave.aktørId()).map(AktørId::getId).orElse(null))
            .medSaksreferanse(nyOppgave.saksref())
            .medTildeltEnhetsnr(nyOppgave.enhetId())
            .medOpprettetAvEnhetsnr(erSystem ? null : nyOppgave.enhetId())
            .medJournalpostId(nyOppgave.journalpostId().getVerdi())
            .medBeskrivelse(nyOppgave.beskrivelse())
            .medBehandlingstema(Optional.ofNullable(nyOppgave.behandlingTema()).map(BehandlingTema::getOffisiellKode).orElse(null));
        var oppgave = oppgaveKlient.opprettetOppgave(request.build());
        var id = oppgave.id().toString();
        LOG.info("FPFORDEL GOSYS opprettet oppgave:{}", id);
        return id;
    }

    @Override
    public String opprettJournalføringsoppgaveFor(NyOppgave nyOppgave) {
        var eksisterende = oppgaveRepository.hentOppgave(nyOppgave.journalpostId().getVerdi());
        if (eksisterende != null) {
            return oppdaterJournalføringsoppgave(eksisterende, nyOppgave);
        }
        var oppgave = OppgaveEntitet.builder()
            .medJournalpostId(nyOppgave.journalpostId().getVerdi())
            .medEnhet(nyOppgave.enhetId())
            .medStatus(Status.AAPNET)
            .medBrukerId(nyOppgave.aktørId())
            .medFrist(helgeJustertFrist(LocalDate.now().plusDays(FRIST_DAGER)))
            .medBeskrivelse(nyOppgave.beskrivelse())
            .medYtelseType(YtelseTypeUtils.mapTilYtelseType(nyOppgave.behandlingTema()))
            .build();

        var id = oppgaveRepository.lagre(oppgave);
        LOG.info("FPFORDEL opprettet lokalt oppgave med journalpostId:{}", id);
        return id;
    }

    public String oppdaterJournalføringsoppgave(OppgaveEntitet eksisterende, NyOppgave nyOppgave) {
        eksisterende.setStatus(Status.AAPNET);
        eksisterende.setEnhet(nyOppgave.enhetId());
        eksisterende.setBrukerId(nyOppgave.aktørId());
        eksisterende.setFrist(helgeJustertFrist(LocalDate.now().plusDays(FRIST_DAGER)));
        eksisterende.setBeskrivelse(nyOppgave.beskrivelse());
        eksisterende.setYtelseType(YtelseTypeUtils.mapTilYtelseType(nyOppgave.behandlingTema()));

        var id = oppgaveRepository.lagre(eksisterende);
        LOG.info("FPFORDEL oppdatert/reåpnet lokal oppgave med journalpostId:{}", id);
        return id;
    }

    @Override
    public boolean finnesÅpeneJournalføringsoppgaverFor(JournalpostId journalpostId) {
        return oppgaveRepository.harÅpenOppgave(journalpostId.getVerdi()) || !oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(
            journalpostId.getVerdi()).isEmpty();
    }

    @Override
    public void ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId journalpostId) {
        var verdi = journalpostId.getVerdi();
        oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(verdi).forEach(o -> {
            LOG.info("FPFORDEL JFR-OPPGAVE: ferdigstiller oppgaver {} for journalpostId: {}", o.id(), journalpostId);
            oppgaveKlient.ferdigstillOppgave(String.valueOf(o.id()));
        });

        if (oppgaveRepository.harÅpenOppgave(verdi)) {
            oppgaveRepository.avsluttOppgaveMedStatus(verdi, Status.FERDIGSTILT);
            LOG.info("FPFORDEL JFR-OPPGAVE: ferdigstiller lokal oppgave for journalpostId: {}", journalpostId);
        }
    }

    @Override
    public Oppgave hentOppgaveFor(JournalpostId journalpostId) {
        if (oppgaveRepository.harÅpenOppgave(journalpostId.getVerdi())) {
            return mapTilOppgave(oppgaveRepository.hentOppgave(journalpostId.getVerdi()));
        } else {
            return oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId.getVerdi())
                .stream()
                .filter(o -> o.journalpostId() != null)
                .map(OppgaverTjeneste::mapTilOppgave)
                .findFirst()
                .orElse(null);
        }
    }

    @Override
    public Optional<Oppgave> hentLokalOppgaveFor(JournalpostId journalpostId) {
        if (oppgaveRepository.harÅpenOppgave(journalpostId.getVerdi())) {
            return Optional.of(mapTilOppgave(oppgaveRepository.hentOppgave(journalpostId.getVerdi())));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void reserverOppgaveFor(Oppgave oppgave, String saksbehandlerId) {
        // lokale oppgaver bruker journalpostId som nøkkel og da bør oppgaveId = journalpostId
        if (oppgaveRepository.harÅpenOppgave(oppgave.journalpostId())) {
            var oppdaterOppgave = oppgaveRepository.hentOppgave(oppgave.journalpostId());
            oppdaterOppgave.setReservertAv(saksbehandlerId);
            oppgaveRepository.lagre(oppdaterOppgave);
        } else {
            oppgaveKlient.reserverOppgave(oppgave.oppgaveId(), saksbehandlerId);
        }
    }

    @Override
    public void avreserverOppgaveFor(Oppgave oppgave) {
        // lokale oppgaver bruker journalpostId som nøkkel og da bør oppgaveId = journalpostId
        if (oppgaveRepository.harÅpenOppgave(oppgave.journalpostId())) {
            var oppdaterOppgave = oppgaveRepository.hentOppgave(oppgave.journalpostId());
            oppdaterOppgave.setReservertAv(null);
            oppgaveRepository.lagre(oppdaterOppgave);
        } else {
            oppgaveKlient.avreserverOppgave(oppgave.oppgaveId());
        }
    }

    @Override
    public List<Oppgave> finnÅpneOppgaverFiltrert() {
        var saksbehandlersEnheter = losEnheterCachedTjeneste.hentLosEnheterFor(KontekstHolder.getKontekst().getUid())
            .stream()
            .map(TilhørendeEnhetDto::enhetsnummer)
            .collect(Collectors.toUnmodifiableSet());

        if (saksbehandlersEnheter.isEmpty()) {
            return List.of();
        }

        return finnAlleOppgaver().stream()
            .filter(oppgave -> !NK_ENHET_ID.equals(oppgave.tildeltEnhetsnr())) // Klager går gjennom gosys
            .filter(ikkeEnOppgaveMedBeskyttelsesbehov().or(saksbehandlerHarTilgangTilSpesjalenheten(saksbehandlersEnheter))) // må ha tilgang til Spesialenheten
            .sorted(Comparator.nullsLast(Comparator.comparing(Oppgave::fristFerdigstillelse).thenComparing(Oppgave::tildeltEnhetsnr)))
            .toList();
    }

    private Predicate<Oppgave> ikkeEnOppgaveMedBeskyttelsesbehov() {
        return oppgave -> !SKJERMINGENHETER.contains(oppgave.tildeltEnhetsnr());
    }

    private Predicate<Oppgave> saksbehandlerHarTilgangTilSpesjalenheten(Set<String> enheter) {
        return oppgave -> enheter.contains(oppgave.tildeltEnhetsnr());
    }

    @Override
    public void flyttLokalOppgaveTilGosys(JournalpostId journalpostId) {
        var oppgaveOpt = hentLokalOppgaveFor(journalpostId);
        if (oppgaveOpt.isPresent()) {
            var oppgave = oppgaveOpt.get();

            var behandlingTema = switch (oppgave.ytelseType()) {
                case ES -> BehandlingTema.ENGANGSSTØNAD;
                case FP -> BehandlingTema.FORELDREPENGER;
                case SVP -> BehandlingTema.SVANGERSKAPSPENGER;
                case null -> null;
            };
            var enhet = enhetsTjeneste.hentFordelingEnhetId(Optional.ofNullable(oppgave.tildeltEnhetsnr()), oppgave.aktørId());

            var nyOppgave = NyOppgave.builder()
                .medJournalpostId(journalpostId)
                .medEnhetId(enhet)
                .medAktørId(oppgave.aktørId())
                .medBehandlingTema(behandlingTema)
                .medBeskrivelse(oppgave.beskrivelse())
                .build();

            LOG.info("Oppretter en gosys oppgave for journalpost {} ", journalpostId);
            opprettGosysJournalføringsoppgaveFor(nyOppgave);
            oppgaveRepository.avsluttOppgaveMedStatus(journalpostId.getVerdi(), Status.FLYTTET_TIL_GOSYS);
        } else {
            LOG.warn("Skulle flytte en oppgave til GOSYS, men fant ikke oppgaven lokalt: {}.", journalpostId);
        }
    }

    @Override
    public void oppdaterBruker(Oppgave oppgave, String fødselsnummer) {
        // lokale oppgaver bruker journalpostId som nøkkel og da bør oppgaveId = journalpostId
        if (oppgaveRepository.harÅpenOppgave(oppgave.journalpostId())) {
            var oppdaterOppgave = oppgaveRepository.hentOppgave(oppgave.journalpostId());
            var aktørId = personTjeneste.hentAktørIdForPersonIdent(fødselsnummer).map(AktørId::new).orElseThrow();
            oppdaterOppgave.setBrukerId(aktørId);
            oppgaveRepository.lagre(oppdaterOppgave);
        }
    }

    private List<Oppgave> finnAlleOppgaver() {
        List<Oppgave> resultat = new ArrayList<>();
        resultat.addAll(finnLokaleOppgaver());
        resultat.addAll(finnGlobaleOppgaver());
        return resultat;
    }

    private List<Oppgave> finnLokaleOppgaver() {
        return oppgaveRepository.hentAlleÅpneOppgaver().stream().map(OppgaverTjeneste::mapTilOppgave).toList();
    }

    private List<Oppgave> finnGlobaleOppgaver() {
        List<String> lukkedeLokaleJournalpostIder = finnOppgaverFlyttetTilGosys();
        return oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT).stream()
                .filter(o -> o.journalpostId() != null && !lukkedeLokaleJournalpostIder.contains(o.journalpostId()))
                .map(OppgaverTjeneste::mapTilOppgave)
                .toList();
    }

    private List<String> finnOppgaverFlyttetTilGosys() {
        return oppgaveRepository.hentOppgaverFlyttetTilGosys().stream()
            .map(OppgaveEntitet::getJournalpostId)
            .toList();
    }

    private static Oppgave mapTilOppgave(no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave oppgave) {
        return Oppgave.builder()
            .medJournalpostId(oppgave.journalpostId())
            .medOppgaveId(oppgave.id().toString())
            .medStatus(Oppgavestatus.valueOf(oppgave.status().name()))
            .medTildeltEnhetsnr(oppgave.tildeltEnhetsnr())
            .medFristFerdigstillelse(oppgave.fristFerdigstillelse())
            .medAktørId(oppgave.aktoerId())
            .medYtelseType(mapTilYtelseType(oppgave.behandlingstema()))
            .medBeskrivelse(oppgave.beskrivelse())
            .medTilordnetRessurs(oppgave.tilordnetRessurs())
            .medAktivDato(oppgave.aktivDato())
            .medKilde(Oppgave.Kilde.GOSYS)
            .build();
    }

    private static Oppgave mapTilOppgave(OppgaveEntitet entitet) {
        return Oppgave.builder()
            .medJournalpostId(entitet.getJournalpostId())
            .medOppgaveId(entitet.getJournalpostId())
            .medStatus(Oppgavestatus.valueOf(entitet.getStatus().name()))
            .medTildeltEnhetsnr(entitet.getEnhet())
            .medFristFerdigstillelse(entitet.getFrist())
            .medAktørId(Optional.ofNullable(entitet.getBrukerId()).map(AktørId::getId).orElse(null))
            .medYtelseType(entitet.getYtelseType())
            .medBeskrivelse(entitet.getBeskrivelse())
            .medTilordnetRessurs(entitet.getReservertAv())
            .medAktivDato(entitet.getOpprettetTidspunkt().toLocalDate())
            .medKilde(Oppgave.Kilde.LOKAL)
            .build();
    }

    private static LocalDate helgeJustertFrist(LocalDate dato) {
        return dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue() ? dato.plusDays(
            1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue()) : dato;
    }

    static YtelseType mapTilYtelseType(String behandlingstema) {
        var behandlingTemaMappet = BehandlingTema.fraOffisiellKode(behandlingstema);

        return YtelseTypeUtils.mapTilYtelseType(behandlingTemaMappet);
    }

}
