package no.nav.foreldrepenger.mottak.task;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.NyOppgave;
import no.nav.foreldrepenger.journalføring.oppgave.lager.AktørId;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.task.sikkerhetsnett.SikkerhetsnettJournalpost;
import no.nav.foreldrepenger.mottak.task.sikkerhetsnett.SikkerhetsnettKlient;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(value = "vedlikehold.tasks.sikkerhetsnett", cronExpression = "0 29 6 * * *", maxFailedRuns = 1) // Endre siste asterisk til WED
public class SikkerhetsnettTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SikkerhetsnettTask.class);

    private final ArkivTjeneste arkivTjeneste;
    private final EnhetsTjeneste enhetsTjeneste;
    private final Journalføringsoppgave journalføringsoppgave;
    private final SikkerhetsnettKlient sikkerhetsnettKlient;

    @Inject
    public SikkerhetsnettTask(ArkivTjeneste arkivTjeneste,
                              EnhetsTjeneste enhetsTjeneste,
                              Journalføringsoppgave journalføringsoppgave,
                              SikkerhetsnettKlient sikkerhetsnettKlient) {
        this.arkivTjeneste = arkivTjeneste;
        this.enhetsTjeneste = enhetsTjeneste;
        this.journalføringsoppgave = journalføringsoppgave;
        this.sikkerhetsnettKlient = sikkerhetsnettKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var åpneJournalposter = sikkerhetsnettKlient.hentÅpneJournalposterEldreEnn(1).stream()
            .filter(jp -> jp.mottaksKanal() == null || !"EESSI".equals(jp.mottaksKanal()))
            .toList();
        LOG.info("FPFORDEL SIKKERHETSNETT fant {} journalposter: {}", åpneJournalposter.size(),
            åpneJournalposter.stream().map(SikkerhetsnettJournalpost::journalpostId).collect(Collectors.joining(",")));
        var åpneJournalposterUtenOppgave = åpneJournalposter.stream()
            .filter(jp -> !journalføringsoppgave.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(jp.journalpostId())))
            .toList();
        LOG.info("FPFORDEL SIKKERHETSNETT fant {} journalposter uten oppgave: {}", åpneJournalposterUtenOppgave.size(),
            åpneJournalposterUtenOppgave.stream().map(SikkerhetsnettJournalpost::journalpostId).collect(Collectors.joining(",")));
        åpneJournalposterUtenOppgave.forEach(this::opprettOppgave);
    }

    private void opprettOppgave(SikkerhetsnettJournalpost jp) {
        var journalpostId = JournalpostId.fra(jp.journalpostId());
        var journalpost = arkivTjeneste.hentArkivJournalpost(journalpostId.getVerdi());
        var aktørId = journalpost.getBrukerAktørId().map(AktørId::new).orElse(null);
        var aktørIdString = journalpost.getBrukerAktørId().orElse(null);
        var enhet = enhetsTjeneste.hentFordelingEnhetId(journalpost.getJournalfoerendeEnhet(), aktørIdString);
        var nyoppgave = new NyOppgave(journalpostId, enhet, aktørId, null, journalpost.getBehandlingstema(), "Journalføring");
        if (EnhetsTjeneste.NK_ENHET_ID.equals(enhet)) {
            journalføringsoppgave.opprettGosysJournalføringsoppgaveFor(nyoppgave);
        } else {
            journalføringsoppgave.opprettJournalføringsoppgaveFor(nyoppgave);
        }
    }
}
