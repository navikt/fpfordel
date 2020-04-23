package no.nav.foreldrepenger.mottak.queue;

import java.time.LocalDateTime;

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
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.XMLForsendelsesinformasjon;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class ProsesstaskMeldingsfordeler implements MeldingsFordeler {

    private static final Logger LOG = LoggerFactory.getLogger(ProsesstaskMeldingsfordeler.class);

    private ProsessTaskRepository prosessTaskRepository;
    private DokumentRepository dokumentRepository;

    ProsesstaskMeldingsfordeler() {// NOSONAR.
        // for CDI proxy
    }

    @Inject
    public ProsesstaskMeldingsfordeler(ProsessTaskRepository prosessTaskRepository,
                                       DokumentRepository dokumentRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void execute(String correlationId, XMLForsendelsesinformasjon forsendelsesinfo) {
        final String arkivId = forsendelsesinfo.getArkivId();
        final Tema tema = Tema.fraOffisiellKode(forsendelsesinfo.getTema().getValue());
        final BehandlingTema behandlingTema = forsendelsesinfo.getBehandlingstema() != null ?
                BehandlingTema.fraOffisiellKode(forsendelsesinfo.getBehandlingstema().getValue()) : BehandlingTema.UDEFINERT;

        // I påvente av MMA-4323
        if (Tema.OMS.equals(tema)) {
            LOG.info("FPFORDEL ignorerer journalpost {} med tema OMS", arkivId);
            return;
        }

        if (dokumentRepository.hentJournalposter(arkivId).stream().map(Journalpost::getOpprettetAv).anyMatch("KAFKA"::equalsIgnoreCase))
            LOG.info("FPFORDEL Mottatt melding på kø allerede mottatt på KAFKA {}", arkivId);


        lagreJoarkTask(arkivId, tema, behandlingTema);

        dokumentRepository.lagreJournalpost(arkivId, "MIDLERTIDIG", null, correlationId, "XMQ");
    }

    private void lagreJoarkTask(String arkivId, Tema tema, BehandlingTema behandlingTema) {
        var taskdata = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskdata.setCallIdFraEksisterende();
        taskdata.setNesteKjøringEtter(LocalDateTime.now().plusMinutes(1));
        MottakMeldingDataWrapper hentFraJoarkMelding = new MottakMeldingDataWrapper(taskdata);
        hentFraJoarkMelding.setArkivId(arkivId);
        hentFraJoarkMelding.setTema(tema);
        hentFraJoarkMelding.setBehandlingTema(behandlingTema);
        hentFraJoarkMelding.setMeldingsKildeExitMQ();
        prosessTaskRepository.lagre(hentFraJoarkMelding.getProsessTaskData());
    }


}
