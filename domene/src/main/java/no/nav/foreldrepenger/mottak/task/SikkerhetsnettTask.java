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
@ProsessTask(value = "vedlikehold.tasks.sikkerhetsnett", cronExpression = "0 29 6 * * WED", maxFailedRuns = 1)
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
        var åpneJournalposterUtenOppgave = sikkerhetsnettKlient.hentÅpneJournalposterEldreEnn(2).stream()
            .filter(jp -> jp.mottaksKanal() == null || !"EESSI".equals(jp.mottaksKanal()))
            .filter(jp -> !journalføringsoppgave.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(jp.journalpostId())))
            .toList();
        var åpneJournalposterTekst = åpneJournalposterUtenOppgave.stream()
            .map(SikkerhetsnettJournalpost::journalpostId)
            .collect(Collectors.joining(","));
        LOG.info("FPFORDEL SIKKERHETSNETT fant {} journalposter uten oppgave: {}", åpneJournalposterUtenOppgave.size(), åpneJournalposterTekst);
        åpneJournalposterUtenOppgave.forEach(this::opprettOppgave);
    }

    private void opprettOppgave(SikkerhetsnettJournalpost jp) {
        try {
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
        } catch (Exception e) {
            LOG.warn("FPFORDEL SIKKERHETSNETT klarte ikke å opprette oppgave for journalpost {}", jp.journalpostId(), e);
        }
    }
}
