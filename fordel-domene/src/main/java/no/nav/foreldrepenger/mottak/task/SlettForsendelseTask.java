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
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>
 * ProssessTask som utleder journalføringsbehov og forsøker rette opp disse.
 * </p>
 */
@ApplicationScoped
@ProsessTask(SlettForsendelseTask.TASKNAME)
public class SlettForsendelseTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.slettForsendelse";

    private DokumentRepository dokumentRepository;

    public SlettForsendelseTask() {

    }

    @Inject
    public SlettForsendelseTask(ProsessTaskRepository prosessTaskRepository,
            DokumentRepository dokumentRepository) {
        super(prosessTaskRepository);
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
            Optional<DokumentMetadata> metadata = dokumentRepository.hentUnikDokumentMetadata(forsendelseId.get());
            if (metadata.isPresent() && metadata.get().getArkivId().isPresent()
                    && (metadata.get().getStatus() != ForsendelseStatus.PENDING)) {
                dokumentRepository.slettForsendelse(forsendelseId.get());
            }
        }
        return null; // Siste steg, fpsak overtar nå
    }
}
