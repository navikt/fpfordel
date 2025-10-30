package no.nav.foreldrepenger.mottak.task;

import java.time.LocalDateTime;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.tjeneste.VLKlargjører;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

/*
 * Sender dokument til fpsak og evt til fptilbake
 */
@ApplicationScoped
@ProsessTask(value = VLKlargjørerTask.TASKNAME, maxFailedRuns = 4, firstDelay = 10, thenDelay = 30)
public class VLKlargjørerTask extends WrappedProsessTaskHandler {

    public static final String REINNSEND = "REINNSEND";
    static final String TASKNAME = "fordeling.klargjoering";
    private VLKlargjører klargjører;

    public VLKlargjørerTask() {

    }

    @Inject
    public VLKlargjørerTask(ProsessTaskTjeneste taskTjeneste, VLKlargjører klargjører) {
        super(taskTjeneste);
        this.klargjører = klargjører;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getSaksnummer().isEmpty()) {
            throw new TekniskException("FP-941984",
                String.format("Prosessering av preconditions for %s mangler %s. TaskId: %s", TASKNAME, MottakMeldingDataWrapper.SAKSNUMMER_KEY,
                    dataWrapper.getId()));
        }
        if (dataWrapper.getArkivId() == null) {
            throw new TekniskException("FP-941984",
                String.format("Prosessering av preconditions for %s mangler %s. TaskId: %s", TASKNAME, MottakMeldingDataWrapper.ARKIV_ID_KEY,
                    dataWrapper.getId()));
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {
        String xml = w.getPayloadAsString().orElse(null);
        String saksnummer = w.getSaksnummer().orElseThrow(() -> new IllegalStateException("Skulle allerede vært sjekket i precondition(...)"));
        String arkivId = w.getArkivId();
        var dokumenttypeId = w.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT);
        var dokumentKategori = w.getDokumentKategori().orElse(DokumentKategori.UDEFINERT);
        String journalEnhet = w.getJournalførendeEnhet().orElse(null);
        String eksternReferanseId = w.getEksternReferanseId().orElse(null);
        var behandlingsTema = w.getBehandlingTema();

        klargjører.klargjør(xml, saksnummer, arkivId, dokumenttypeId, w.getForsendelseMottattTidspunkt().orElseGet(LocalDateTime::now),
            behandlingsTema, dokumentKategori, journalEnhet, eksternReferanseId);

        return null; // Siste steg, fpsak overtar nå
    }

}
