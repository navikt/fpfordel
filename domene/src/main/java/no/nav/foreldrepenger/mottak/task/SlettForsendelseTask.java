package no.nav.foreldrepenger.mottak.task;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

/**
 * <p>
 * ProssessTask som sletter forsendelse etter journalføring
 * </p>
 */
@ApplicationScoped
@ProsessTask(SlettForsendelseTask.TASKNAME)
public class SlettForsendelseTask extends WrappedProsessTaskHandler {

    static final String TASKNAME = "fordeling.slettForsendelse";

    public static final String FORCE_SLETT_KEY = "force.slett";


    private DokumentRepository dokumentRepository;

    public SlettForsendelseTask() {

    }

    @Inject
    public SlettForsendelseTask(ProsessTaskTjeneste taskTjeneste,
                                DokumentRepository dokumentRepository) {
        super(taskTjeneste);
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getForsendelseId().isEmpty()) {
            throw new TekniskException("FP-941984",
                    String.format("Prosessering av preconditions for %s mangler %s. TaskId: %s", TASKNAME,
                            MottakMeldingDataWrapper.FORSENDELSE_ID_KEY, dataWrapper.getId()));
        }
    }

    @Transactional
    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        Optional<UUID> forsendelseId = dataWrapper.getForsendelseId();
        if (forsendelseId.isPresent()) {
            var metadata = dokumentRepository.hentUnikDokumentMetadata(forsendelseId.get());
            if (dataWrapper.getProsessTaskData().getPropertyValue(FORCE_SLETT_KEY) != null ||
                    (metadata.flatMap(DokumentMetadata::getArkivId).isPresent() &&
                            metadata.filter(m -> !ForsendelseStatus.PENDING.equals(m.getStatus())).isPresent())) {
                dokumentRepository.slettForsendelse(forsendelseId.get());
            }
        }
        return null; // Siste steg, fpsak overtar nå
    }
}
