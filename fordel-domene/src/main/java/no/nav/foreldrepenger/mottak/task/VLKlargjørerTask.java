package no.nav.foreldrepenger.mottak.task;

import java.time.LocalDateTime;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.tjeneste.VLKlargjører;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@Dependent
@ProsessTask(VLKlargjørerTask.TASKNAME)
public class VLKlargjørerTask extends WrappedProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(VLKlargjørerTask.class);
    public static final String TASKNAME = "fordeling.klargjoering";
    public static final String REINNSEND = "REINNSEND";
    private final VLKlargjører klargjører;

    @Inject
    public VLKlargjørerTask(ProsessTaskRepository prosessTaskRepository, VLKlargjører klargjører) {
        super(prosessTaskRepository);
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
        String saksnummer = w.getSaksnummer()
                .orElseThrow(() -> new IllegalStateException("Skulle allerede vært sjekket i precondition(...)"));
        String arkivId = w.getArkivId();
        var dokumenttypeId = w.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT);
        var dokumentKategori = w.getDokumentKategori().orElse(DokumentKategori.UDEFINERT);
        String journalEnhet = w.getJournalførendeEnhet().orElse(null);
        String eksternReferanseId = w.getEksternReferanseId().orElse(null);
        var behandlingsTema = w.getBehandlingTema();
        var forsendelseId = w.getForsendelseId();
        boolean erReinnsend = w.getRetryingTask().map(REINNSEND::equals).orElse(Boolean.FALSE);

        klargjører.klargjør(xml, saksnummer, arkivId, dokumenttypeId,
                w.getForsendelseMottattTidspunkt().orElse(null), behandlingsTema,
                w.getForsendelseId().orElse(null), dokumentKategori, journalEnhet, eksternReferanseId);

        if (forsendelseId.isPresent() && !erReinnsend) {
            // Gi selvbetjening tid til å polle ferdig + Kafka-hendelse tid til å nå fram
            // (og bli ignorert)
            return w.nesteSteg(SlettForsendelseTask.TASKNAME, true, LocalDateTime.now().plusHours(2));
        }
        return null; // Siste steg, fpsak overtar nå
    }

}
