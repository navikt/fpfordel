package no.nav.foreldrepenger.mottak.hendelse;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.dokument.Journalpost;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.joark.HentDataFraJoarkTask;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.log.mdc.MDCOperations;

@Transactional
@ActivateRequestContext
@ApplicationScoped
public class JournalføringHendelseHåndterer {

    private static final Logger LOG = LoggerFactory.getLogger(JournalføringHendelseHåndterer.class);
    private ProsessTaskRepository taskRepository;
    private DokumentRepository dokumentRepository;

    JournalføringHendelseHåndterer() {
        // CDI
    }

    @Inject
    public JournalføringHendelseHåndterer(ProsessTaskRepository taskRepository,
                                          DokumentRepository dokumentRepository) {
        this.taskRepository = taskRepository;
    }

    void handleMessage(String key, JournalfoeringHendelseRecord payload) {
        setCallIdForHendelse(payload);

        LOG.info("FPFORDEL Mottatt Journalføringhendelse '{}'", key);

        var arkivId = payload.getJournalpostId().toString();
        var eksternReferanseId = payload.getKanalReferanseId().toString();

        if (dokumentRepository.erLokalForsendelse(eksternReferanseId)) {
            LOG.info("FPFORDEL Mottatt Hendelse egen journalføring callid {}", arkivId);
            return;
        }
        if (!dokumentRepository.hentDoumentMetadataForArkivId(arkivId).isEmpty()) {
            LOG.info("FPFORDEL Mottatt Hendelse egen journalføring arkivid {}", arkivId);
            return;
        }
        if (dokumentRepository.hentJournalposter(arkivId).stream().map(Journalpost::getOpprettetAv).anyMatch("FORDEL"::equalsIgnoreCase)) {
            LOG.info("FPFORDEL Mottatt Hendelse egen journalføring journalpost {}", arkivId);
            return;
        }

        if (dokumentRepository.hentJournalposter(arkivId).stream().map(Journalpost::getOpprettetAv).anyMatch("MQ"::equalsIgnoreCase)) {
            LOG.info("FPFORDEL Mottatt Hendelse allerede mottatt på MQ {}", arkivId);
            return;
        }

        // lagreJoarkTask(payload, arkivId, eksternReferanseId); TODO: switchover

        dokumentRepository.lagreJournalpost(arkivId, payload.getJournalpostStatus().toString(), payload.getMottaksKanal().toString(), eksternReferanseId, "KAFKA");
    }

    private void lagreJoarkTask(JournalfoeringHendelseRecord payload, String arkivId, String eksternReferanse) {
        var taskdata = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskdata.setCallIdFraEksisterende();
        MottakMeldingDataWrapper melding = new MottakMeldingDataWrapper(taskdata);
        melding.setArkivId(arkivId);
        melding.setTema(Tema.fraOffisiellKode(payload.getTemaNytt().toString()));
        melding.setBehandlingTema(BehandlingTema.fraOffisiellKode(payload.getBehandlingstema().toString()));
        melding.setEksternReferanseId(eksternReferanse);
        melding.setMeldingsKildeKafka();
        taskRepository.lagre(melding.getProsessTaskData());
    }

    private void setCallIdForHendelse(JournalfoeringHendelseRecord payload) {
        var hendelsesId = payload.getHendelsesId();
        if (hendelsesId == null) {
            LOG.info("HendelseId er null, generer callId.");
            hendelsesId = UUID.randomUUID().toString();
        }
        MDCOperations.putCallId(hendelsesId.toString());
    }
}
