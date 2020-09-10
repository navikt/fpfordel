package no.nav.foreldrepenger.mottak.hendelse;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
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
    private static final String EESSI = MottakKanal.EESSI.getKode();

    private ProsessTaskRepository taskRepository;
    private DokumentRepository dokumentRepository;

    JournalføringHendelseHåndterer() {
        // CDI
    }

    @Inject
    public JournalføringHendelseHåndterer(ProsessTaskRepository taskRepository,
            DokumentRepository dokumentRepository) {
        this.taskRepository = taskRepository;
        this.dokumentRepository = dokumentRepository;
    }

    void handleMessage(String key, JournalfoeringHendelseRecord payload) {
        setCallIdForHendelse(payload);

        var arkivId = payload.getJournalpostId().toString();
        var hendelseType = payload.getHendelsesType().toString();
        var mottaksKanal = payload.getMottaksKanal().toString();
        var eksternReferanseId = (payload.getKanalReferanseId() == null) || payload.getKanalReferanseId().toString().isEmpty()
                ? null
                : payload.getKanalReferanseId().toString();

        // EESSI har egen mottaksprosess m/BEH_SED-oppgaver. De uten kanalreferanse er
        // "klonet" av SBH og journalført fra Gosys.
        if (EESSI.equals(mottaksKanal) || (eksternReferanseId == null)) {
            LOG.info("FPFORDEL Mottatt Journalføringhendelse ignorerer journalpost {} kanal {}", arkivId, mottaksKanal);
            return;
        }

        LOG.info("FPFORDEL Mottatt Journalføringhendelse type {} journalpost {} referanse {}", hendelseType, arkivId, eksternReferanseId);

        // All journalføring av innsendinger fra SB gir en Midlertidig-hendelse. De skal
        // vi ikke reagere på før evt full refaktorering
        if (dokumentRepository.erLokalForsendelse(eksternReferanseId)) {
            LOG.info("FPFORDEL Mottatt Hendelse egen journalføring callid {}", arkivId);
            return;
        }
        if (!dokumentRepository.hentJournalposter(arkivId).isEmpty()) {
            LOG.info("FPFORDEL Mottatt Hendelse egen journalføring journalpost {}", arkivId);
            return;
        }

        lagreJoarkTask(payload, arkivId, eksternReferanseId);
    }

    private void lagreJoarkTask(JournalfoeringHendelseRecord payload, String arkivId, String eksternReferanse) {
        var taskdata = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskdata.setCallIdFraEksisterende();
        MottakMeldingDataWrapper melding = new MottakMeldingDataWrapper(taskdata);
        melding.setArkivId(arkivId);
        melding.setTema(Tema.fraOffisiellKode(payload.getTemaNytt().toString()));
        melding.setBehandlingTema(
                BehandlingTema.fraOffisiellKode(payload.getBehandlingstema() != null ? payload.getBehandlingstema().toString() : null));
        melding.setEksternReferanseId(eksternReferanse);
        taskRepository.lagre(melding.getProsessTaskData());
    }

    private static void setCallIdForHendelse(JournalfoeringHendelseRecord payload) {
        var hendelsesId = payload.getHendelsesId();
        if ((hendelsesId == null) || hendelsesId.toString().isEmpty()) {
            MDCOperations.putCallId(UUID.randomUUID().toString());
        } else {
            MDCOperations.putCallId(hendelsesId.toString());
        }
    }
}
