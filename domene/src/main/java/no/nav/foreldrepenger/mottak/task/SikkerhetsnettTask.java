package no.nav.foreldrepenger.mottak.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.joark.HentDataFraJoarkTask;
import no.nav.foreldrepenger.mottak.task.sikkerhetsnett.SikkerhetsnettJournalpost;
import no.nav.foreldrepenger.mottak.task.sikkerhetsnett.SikkerhetsnettKlient;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@Dependent
@ProsessTask(value = "vedlikehold.tasks.sikkerhetsnett", cronExpression = "0 29 6 * * WED", maxFailedRuns = 1)
public class SikkerhetsnettTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SikkerhetsnettTask.class);

    private final Journalføringsoppgave journalføringsoppgave;
    private final SikkerhetsnettKlient sikkerhetsnettKlient;
    private final ProsessTaskTjeneste prosessTaskTjeneste;

    @Inject
    public SikkerhetsnettTask(Journalføringsoppgave journalføringsoppgave,
                              SikkerhetsnettKlient sikkerhetsnettKlient,
                              ProsessTaskTjeneste prosessTaskTjeneste) {
        this.journalføringsoppgave = journalføringsoppgave;
        this.sikkerhetsnettKlient = sikkerhetsnettKlient;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var åpneJournalposterUtenOppgave = sikkerhetsnettKlient.hentÅpneJournalposterEldreEnn(2).stream()
            .filter(jp -> jp.mottaksKanal() == null || !"EESSI".equals(jp.mottaksKanal()))
            .filter(jp -> !journalføringsoppgave.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(jp.journalpostId())))
            .sorted(Comparator.comparing(SikkerhetsnettJournalpost::journalpostId))
            .toList();
        var åpneJournalposterTekst = åpneJournalposterUtenOppgave.stream()
            .map(SikkerhetsnettJournalpost::journalpostId)
            .collect(Collectors.joining(","));
        LOG.info("FPFORDEL SIKKERHETSNETT fant {} journalposter uten oppgave: {}", åpneJournalposterUtenOppgave.size(), åpneJournalposterTekst);
        var tasks = åpneJournalposterUtenOppgave.stream().map(SikkerhetsnettTask::opprettTask).toList();
        if (!tasks.isEmpty()) {
            var gruppe = new ProsessTaskGruppe().addNesteParallell(tasks);
            prosessTaskTjeneste.lagre(gruppe);
        }
    }

    private static ProsessTaskData opprettTask(SikkerhetsnettJournalpost jp) {
        var taskdata = ProsessTaskData.forProsessTask(HentDataFraJoarkTask.class);
        MottakMeldingDataWrapper melding = new MottakMeldingDataWrapper(taskdata);
        melding.setArkivId(jp.journalpostId());
        melding.setTema(Tema.fraOffisiellKode(jp.tema()));
        melding.setBehandlingTema(BehandlingTema.fraOffisiellKode(jp.behandlingstema()));
        var oppdatertTaskdata = melding.getProsessTaskData();
        oppdatertTaskdata.setNesteKjøringEtter(LocalDateTime.now().plus(Duration.ofMinutes(1)));
        return oppdatertTaskdata;

    }
}
