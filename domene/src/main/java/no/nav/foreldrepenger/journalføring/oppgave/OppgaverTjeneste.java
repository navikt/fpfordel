package no.nav.foreldrepenger.journalføring.oppgave;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.domene.BrukerId;
import no.nav.foreldrepenger.domene.YtelseType;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveRepository;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
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
    private boolean lagreOppgaverLokalt;

    OppgaverTjeneste() {
        // CDI
    }

    @Inject
    public OppgaverTjeneste(OppgaveRepository oppgaveRepository,
                            Oppgaver oppgaveKlient,
                            @KonfigVerdi(value = "lagre.oppgaver.lokalt.toggle", defaultVerdi = "false") boolean lagreOppgaverLokalt) {
        this.oppgaveRepository = oppgaveRepository;
        this.oppgaveKlient = oppgaveKlient;
        this.lagreOppgaverLokalt = lagreOppgaverLokalt;
    }

    @Override
    public String opprettJournalføringsoppgaveFor(String journalpostId,
                                                  String enhetId,
                                                  String aktørId,
                                                  String saksref,
                                                  String behandlingTema,
                                                  String beskrivelse) {
        if (lagreOppgaverLokalt) {
            var oppgave = OppgaveEntitet.builder()
                    .medJournalpostId(journalpostId)
                    .medEnhet(enhetId)
                    .medStatus(Status.AAPNET)
                    .medBrukerId(new BrukerId(aktørId))
                    .medFrist(helgeJustertFrist(LocalDate.now().plusDays(FRIST_DAGER)))
                    .medBeskrivelse(beskrivelse)
                    .medYtelseType(mapTilYtelseType(behandlingTema))
                    .build();

            var id = oppgaveRepository.lagre(oppgave);
            LOG.info("FPFORDEL opprettet lokalt oppgave med id:{}", id);
            return id;
        } else {
            var request = OpprettOppgave.getBuilderTemaFOR(Oppgavetype.JOURNALFØRING, no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet.NORM, FRIST_DAGER)
                .medAktoerId(aktørId)
                .medSaksreferanse(saksref)
                .medTildeltEnhetsnr(enhetId)
                .medOpprettetAvEnhetsnr(enhetId)
                .medJournalpostId(journalpostId)
                .medBeskrivelse(beskrivelse)
                .medBehandlingstema(behandlingTema);
            var oppgave = oppgaveKlient.opprettetOppgave(request.build());
            var id = oppgave.id().toString();
            LOG.info("FPFORDEL GOSYS opprettet oppgave:{}", id);
            return id;
        }
    }

    @Override
    public boolean finnesÅpeneJournalføringsoppgaverFor(String journalpostId) {
        return oppgaveRepository.harÅpenOppgave(journalpostId) ||
                !oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId).isEmpty();
    }

    @Override
    public void ferdigstillAlleÅpneJournalføringsoppgaverFor(String journalpostId) {
        oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId).forEach(o -> {
            LOG.info("FPFORDEL JFR-OPPGAVE: ferdigstiller oppgaver {} for journalpostId: {}", o.id(), journalpostId);
            oppgaveKlient.ferdigstillOppgave(String.valueOf(o.id()));
        });

        if (oppgaveRepository.harÅpenOppgave(journalpostId)) {
            oppgaveRepository.ferdigstillOppgave(journalpostId);
            LOG.info("FPFORDEL JFR-OPPGAVE: ferdigstiller lokal oppgave for journalpostId: {}", journalpostId);
        }
    }

    @Override
    public Oppgave hentOppgaveFor(String oppgaveId) {
        if (oppgaveRepository.harÅpenOppgave(oppgaveId)) {
            return mapTilOppgave(oppgaveRepository.hentOppgave(oppgaveId));
        } else {
            return mapTilOppgave(oppgaveKlient.hentOppgave(oppgaveId));
        }
    }

    @Override
    public void reserverOppgaveFor(String oppgaveId, String saksbehandlerId) {
        // oppgaveId er egentlig journalpostId i dette tilfellet.
        if (oppgaveRepository.harÅpenOppgave(oppgaveId)) {
            var oppgave = oppgaveRepository.hentOppgave(oppgaveId);
            oppgave.setReservertAv(saksbehandlerId);
            oppgaveRepository.lagre(oppgave);
        } else {
            oppgaveKlient.reserverOppgave(oppgaveId, saksbehandlerId);
        }
    }

    @Override
    public void avreserverOppgaveFor(String oppgaveId) {
        if (oppgaveRepository.harÅpenOppgave(oppgaveId)) {
            var oppgave = oppgaveRepository.hentOppgave(oppgaveId);
            oppgave.setReservertAv(null);
            oppgaveRepository.lagre(oppgave);
        } else {
            oppgaveKlient.avreserverOppgave(oppgaveId);
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
        return oppgaver.stream().sorted(
                Comparator.nullsLast(
                        Comparator.comparing(Oppgave::fristFerdigstillelse)
                                .thenComparing(Oppgave::tildeltEnhetsnr)))
                .toList();
    }

    private void finnOppgaver(String enhet, List<Oppgave> resultat) {
        resultat.addAll(finnLokaleOppgaver(enhet));
        resultat.addAll(finnGlobaleOppgaver(enhet));
    }

    private List<Oppgave> finnLokaleOppgaver(String enhet) {
        if (enhet == null) {
            return oppgaveRepository.hentAlleÅpneOppgaver()
                .stream()
                .map(OppgaverTjeneste::mapTilOppgave)
                .toList();
        }
        return oppgaveRepository.hentÅpneOppgaverFor(enhet)
            .stream()
            .map(OppgaverTjeneste::mapTilOppgave)
            .toList();
    }

    private List<Oppgave> finnGlobaleOppgaver(String enhet) {
        return oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, enhet, LIMIT)
            .stream()
            .map(OppgaverTjeneste::mapTilOppgave)
            .toList();
    }

    private static Oppgave mapTilOppgave(no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave oppgave) {
        return Oppgave.builder()
            .medId(oppgave.id().toString())
            .medStatus(Oppgavestatus.valueOf(oppgave.status().name()))
            .medTildeltEnhetsnr(oppgave.tildeltEnhetsnr())
            .medFristFerdigstillelse(oppgave.fristFerdigstillelse())
            .medAktoerId(oppgave.aktoerId())
            .medYtelseType(mapTilYtelseType(oppgave.behandlingstema()))
            .medBeskrivelse(oppgave.beskrivelse())
            .medTilordnetRessurs(oppgave.tilordnetRessurs())
            .medAktivDato(oppgave.aktivDato())
            .build();
    }

    private static Oppgave mapTilOppgave(OppgaveEntitet entitet) {
        return Oppgave.builder()
                .medId(entitet.getJournalpostId())
                .medStatus(Oppgavestatus.valueOf(entitet.getStatus().name()))
                .medTildeltEnhetsnr(entitet.getEnhet())
                .medFristFerdigstillelse(entitet.getFrist())
                .medAktoerId(entitet.getBrukerId().getId())
                .medYtelseType(YtelseType.valueOf(entitet.getYtelseType().name()))
                .medBeskrivelse(entitet.getBeskrivelse())
                .medTilordnetRessurs(entitet.getReservertAv())
                .medAktivDato(LocalDate.now())
                .build();
    }

    private static LocalDate helgeJustertFrist(LocalDate dato) {
        return dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue() ? dato.plusDays(
            1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue()) : dato;
    }

    static YtelseType mapTilYtelseType(String behandlingstema) {
        LOG.info("Oppgave med behandlingstema {}", behandlingstema);
        var behandlingTemaMappet = BehandlingTema.fraOffisiellKode(behandlingstema);
        LOG.info("Fant oppgave med behandlingTemaMappet {}", behandlingTemaMappet);

        return switch (behandlingTemaMappet) {
            case FORELDREPENGER, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FØDSEL -> YtelseType.FP;
            case SVANGERSKAPSPENGER -> YtelseType.SVP;
            case ENGANGSSTØNAD, ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_FØDSEL -> YtelseType.ES;
            default -> null;
        };
    }
}
