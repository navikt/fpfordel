package no.nav.foreldrepenger.mottak.task;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@Dependent
@ProsessTask(KlargjorForVLTask.TASKNAME)
public class KlargjorForVLTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.klargjoering";
    public static final String REINNSEND = "REINNSEND";
    private final KlargjørForVLTjeneste klargjørForVLTjeneste;
    private final DokumentRepository dokumentRepository;

    @Inject
    public KlargjorForVLTask(ProsessTaskRepository prosessTaskRepository,
            KlargjørForVLTjeneste klargjørForVLTjeneste,
            DokumentRepository dokumentRepository) {
        super(prosessTaskRepository);
        this.klargjørForVLTjeneste = klargjørForVLTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getSaksnummer().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId());
        }
        if (dataWrapper.getArkivId() == null) {
            throw MottakMeldingFeil.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.ARKIV_ID_KEY, dataWrapper.getId());
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        String xml = dataWrapper.getPayloadAsString().orElse(null);
        String saksnummer = dataWrapper.getSaksnummer()
                .orElseThrow(() -> new IllegalStateException("Skulle allerede vært sjekket i precondition(...)"));
        String arkivId = dataWrapper.getArkivId();
        var dokumenttypeId = dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT);
        var dokumentKategori = dataWrapper.getDokumentKategori().orElse(DokumentKategori.UDEFINERT);
        String journalEnhet = dataWrapper.getJournalførendeEnhet().orElse(null);
        String eksternReferanseId = dataWrapper.getEksternReferanseId().orElse(null);
        var behandlingsTema = dataWrapper.getBehandlingTema();
        Optional<UUID> forsendelseId = dataWrapper.getForsendelseId();
        boolean erReinnsend = dataWrapper.getRetryingTask().map(REINNSEND::equals).orElse(Boolean.FALSE);
        if (forsendelseId.isPresent() && !erReinnsend) {
            dokumentRepository.oppdaterForsendelseMetadata(forsendelseId.get(), arkivId, saksnummer,
                    ForsendelseStatus.FPSAK);
        }

        klargjørForVLTjeneste.klargjørForVL(xml, saksnummer, arkivId, dokumenttypeId,
                dataWrapper.getForsendelseMottattTidspunkt().orElse(null), behandlingsTema,
                dataWrapper.getForsendelseId().orElse(null), dokumentKategori, journalEnhet, eksternReferanseId);

        if (forsendelseId.isPresent() && !erReinnsend) {
            // Gi selvbetjening tid til å polle ferdig + Kafka-hendelse tid til å nå fram
            // (og bli ignorert)
            return dataWrapper.nesteSteg(SlettForsendelseTask.TASKNAME, true, LocalDateTime.now().plusHours(2));
        }
        return null; // Siste steg, fpsak overtar nå
    }

}
