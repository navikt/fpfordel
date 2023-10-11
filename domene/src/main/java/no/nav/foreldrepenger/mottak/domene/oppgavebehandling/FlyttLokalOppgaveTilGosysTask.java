package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.NyOppgave;
import no.nav.foreldrepenger.journalføring.oppgave.lager.AktørId;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

/**
 * <p>
 * ProsessTask som oppretter en oppgave i GSAK for manuell behandling av
 * tilfeller som ikke kan håndteres automatisk av vedtaksløsningen.
 * <p>
 * </p>
 * @deprecated Bør fjernes ved neste prodsetting.
 */
@Dependent
@ProsessTask(value = "integrasjon.gsak.flyttOppgave", maxFailedRuns = 2)
public class FlyttLokalOppgaveTilGosysTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FlyttLokalOppgaveTilGosysTask.class);

    private final Journalføringsoppgave oppgaverTjeneste;
    private final EnhetsTjeneste enhetsTjeneste;

    @Inject
    public FlyttLokalOppgaveTilGosysTask(Journalføringsoppgave oppgaverTjeneste, EnhetsTjeneste enhetsTjeneste) {
        this.oppgaverTjeneste = oppgaverTjeneste;
        this.enhetsTjeneste = enhetsTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var journalpostId = JournalpostId.fra(prosessTaskData.getPropertyValue(ARKIV_ID_KEY));
        var oppgave = oppgaverTjeneste.hentLokalOppgaveFor(journalpostId).orElse(null);
        if (oppgave == null) {
            return;
        }

        var behandlingTema = switch (oppgave.ytelseType()) {
            case ES -> BehandlingTema.ENGANGSSTØNAD;
            case FP -> BehandlingTema.FORELDREPENGER;
            case SVP -> BehandlingTema.SVANGERSKAPSPENGER;
        };
        var enhet = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, behandlingTema,
            Optional.ofNullable(oppgave.tildeltEnhetsnr()), oppgave.aktørId());

        var nyOppgave = NyOppgave.builder()
            .medJournalpostId(journalpostId)
            .medEnhetId(enhet)
            .medAktørId(new AktørId(oppgave.aktørId()))
            .medBehandlingTema(behandlingTema)
            .medBeskrivelse(oppgave.beskrivelse())
            .build();

        LOG.info("Oppretter en gosys oppgave for journalpost {} ", journalpostId.getVerdi());
        oppgaverTjeneste.opprettGosysJournalføringsoppgaveFor(nyOppgave);
        oppgaverTjeneste.ferdigstillLokalOppgaveFor(journalpostId);
    }
}
