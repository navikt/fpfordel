package no.nav.foreldrepenger.mottak.task;

import java.time.LocalDateTime;

import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;
import no.nav.vedtak.felles.prosesstask.impl.feilhåndtering.SimpelFeilhåndteringsalgoritme;
import no.nav.vedtak.felles.prosesstask.spi.ForsinkelseStrategi;


abstract class OverførTilGsakFeilhåndteringsalgoritme extends SimpelFeilhåndteringsalgoritme {

    private ProsessTaskRepository prosessTaskRepository;

    public OverførTilGsakFeilhåndteringsalgoritme(ForsinkelseStrategi forsinkelseStrategi, ProsessTaskRepository prosessTaskRepository) {
        super(forsinkelseStrategi);
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public boolean skalKjørePåNytt(ProsessTaskTypeInfo taskType, int antallFeilet, Exception exception) {
        if (exception instanceof IntegrasjonException) {
            return false;
        }
        return super.skalKjørePåNytt(taskType, antallFeilet, exception);
    }

    @Override
    public Feil hendelserNårIkkeKjøresPåNytt(Exception exception, ProsessTaskData prosessTaskData) {
        if (exception instanceof IntegrasjonException) {
            MottakMeldingDataWrapper wrapper = new MottakMeldingDataWrapper(prosessTaskData);
            MottakMeldingDataWrapper nesteSteg;
            if (wrapper.getArkivId() == null) {
                nesteSteg = wrapper.nesteSteg(MidlJournalføringTask.TASKNAME, false, LocalDateTime.now());
            } else {
                nesteSteg = wrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME, false, LocalDateTime.now());
            }
            ProsessTaskData nyProsessTaskData = nesteSteg.getProsessTaskData();
            prosessTaskRepository.lagre(nyProsessTaskData);

            return ((IntegrasjonException) exception).getFeil();
        }
        return super.hendelserNårIkkeKjøresPåNytt(exception, prosessTaskData);
    }

}
