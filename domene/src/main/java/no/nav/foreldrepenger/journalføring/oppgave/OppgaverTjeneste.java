package no.nav.foreldrepenger.journalføring.oppgave;

import static no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType.ES;
import static no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType.FP;
import static no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType.SVP;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.domene.NyOppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus;
import no.nav.foreldrepenger.journalføring.oppgave.lager.AktørId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveRepository;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.OpprettOppgave;

@Dependent
class OppgaverTjeneste implements Journalføringsoppgave {

    private static final Logger LOG = LoggerFactory.getLogger(OppgaverTjeneste.class);
    protected static final String LIMIT = "50";
    protected static final int FRIST_DAGER = 1;

    private OppgaveRepository oppgaveRepository;
    private Oppgaver oppgaveKlient;
    private EnhetsTjeneste enhetsTjeneste;

    OppgaverTjeneste() {
        // CDI
    }

    @Inject
    public OppgaverTjeneste(OppgaveRepository oppgaveRepository, Oppgaver oppgaveKlient,
                            EnhetsTjeneste enhetsTjeneste) {
        this.oppgaveRepository = oppgaveRepository;
        this.oppgaveKlient = oppgaveKlient;
        this.enhetsTjeneste = enhetsTjeneste;
    }

    @Override
    public String opprettGosysJournalføringsoppgaveFor(NyOppgave nyOppgave) {
        var request = OpprettOppgave.getBuilderTemaFOR(Oppgavetype.JOURNALFØRING, no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet.NORM,
                FRIST_DAGER)
            .medAktoerId(Optional.ofNullable(nyOppgave.aktørId()).map(AktørId::getId).orElse(null))
            .medSaksreferanse(nyOppgave.saksref())
            .medTildeltEnhetsnr(nyOppgave.enhetId())
            .medOpprettetAvEnhetsnr(nyOppgave.enhetId())
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
        var oppgave = OppgaveEntitet.builder()
            .medJournalpostId(nyOppgave.journalpostId().getVerdi())
            .medEnhet(nyOppgave.enhetId())
            .medStatus(Status.AAPNET)
            .medBrukerId(nyOppgave.aktørId())
            .medFrist(helgeJustertFrist(LocalDate.now().plusDays(FRIST_DAGER)))
            .medBeskrivelse(nyOppgave.beskrivelse())
            .medYtelseType(mapTilYtelseType(nyOppgave.behandlingTema()))
            .build();

        var id = oppgaveRepository.lagre(oppgave);
        LOG.info("FPFORDEL opprettet lokalt oppgave med journalpostId:{}", id);
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
            oppgaveRepository.ferdigstillOppgave(verdi);
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
    public void ferdigstillLokalOppgaveFor(JournalpostId journalpostId) {
        oppgaveRepository.ferdigstillOppgave(journalpostId.getVerdi());
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
    public List<Oppgave> finnÅpneOppgaverFor(Set<String> enheter) {
        List<Oppgave> oppgaver = new ArrayList<>();

        if (enheter == null || enheter.isEmpty()) {
            finnOppgaver(null, oppgaver);
        } else {
            enheter.forEach(enhet -> finnOppgaver(enhet, oppgaver));
        }
        return oppgaver.stream()
            .sorted(Comparator.nullsLast(Comparator.comparing(Oppgave::fristFerdigstillelse).thenComparing(Oppgave::tildeltEnhetsnr)))
            .toList();
    }

    @Override
    public void flyttLokalOppgaveTilGosys(JournalpostId journalpostId) {
        var oppgaveOpt = hentLokalOppgaveFor(journalpostId);
        if (oppgaveOpt.isPresent()) {
            var oppgave = oppgaveOpt.get();

            var behandlingTema = oppgave.ytelseType() == null ? null: switch (oppgave.ytelseType()) {
                case ES -> BehandlingTema.ENGANGSSTØNAD;
                case FP -> BehandlingTema.FORELDREPENGER;
                case SVP -> BehandlingTema.SVANGERSKAPSPENGER;
            };
            var enhet = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, behandlingTema,
                Optional.ofNullable(oppgave.tildeltEnhetsnr()), oppgave.aktørId());

            var nyOppgave = NyOppgave.builder()
                .medJournalpostId(journalpostId)
                .medEnhetId(enhet)
                .medAktørId(oppgave.aktørId())
                .medBehandlingTema(behandlingTema)
                .medBeskrivelse(oppgave.beskrivelse())
                .build();

            LOG.info("Oppretter en gosys oppgave for journalpost {} ", journalpostId);
            opprettGosysJournalføringsoppgaveFor(nyOppgave);
            ferdigstillLokalOppgaveFor(journalpostId);
        } else {
            LOG.warn("Skulle flytte en oppgave til GOSYS, men fant ikke oppgaven lokalt: {}.", journalpostId);
        }
    }

    private void finnOppgaver(String enhet, List<Oppgave> resultat) {
        resultat.addAll(finnLokaleOppgaver(enhet));
        resultat.addAll(finnGlobaleOppgaver(enhet));
    }

    private List<Oppgave> finnLokaleOppgaver(String enhet) {
        if (enhet == null) {
            return oppgaveRepository.hentAlleÅpneOppgaver().stream().map(OppgaverTjeneste::mapTilOppgave).toList();
        }
        return oppgaveRepository.hentÅpneOppgaverFor(enhet).stream().map(OppgaverTjeneste::mapTilOppgave).toList();
    }

    private List<Oppgave> finnGlobaleOppgaver(String enhet) {
        return oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, enhet, LIMIT)
            .stream()
            .filter(o -> o.journalpostId() != null)
            .map(OppgaverTjeneste::mapTilOppgave)
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
            .medAktivDato(LocalDate.now())
            .medKilde(Oppgave.Kilde.LOKAL)
            .build();
    }

    private static LocalDate helgeJustertFrist(LocalDate dato) {
        return dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue() ? dato.plusDays(
            1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue()) : dato;
    }

    static YtelseType mapTilYtelseType(String behandlingstema) {
        var behandlingTemaMappet = BehandlingTema.fraOffisiellKode(behandlingstema);

        return mapTilYtelseType(behandlingTemaMappet);
    }

    static YtelseType mapTilYtelseType(BehandlingTema behandlingstema) {
        if (behandlingstema == null) {
            return null;
        }
        return switch (behandlingstema) {
            case FORELDREPENGER, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FØDSEL -> FP;
            case SVANGERSKAPSPENGER -> SVP;
            case ENGANGSSTØNAD, ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_FØDSEL -> ES;
            default -> null;
        };
    }
}
