package no.nav.foreldrepenger.mottak.queue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
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

    ProsesstaskMeldingsfordeler() {// NOSONAR.
        // for CDI proxy
    }

    @Inject
    public ProsesstaskMeldingsfordeler(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public void execute(XMLForsendelsesinformasjon forsendelsesinfo) {
        final String arkivId = forsendelsesinfo.getArkivId();
        final BehandlingTema behandlingTema;
        if (forsendelsesinfo.getBehandlingstema() != null) {
            var behandlingstemaOffisiellKode = forsendelsesinfo.getBehandlingstema().getValue();
            behandlingTema = BehandlingTema.fraOffisiellKode(behandlingstemaOffisiellKode);
        } else {
            behandlingTema = BehandlingTema.UDEFINERT;
        }

        final Tema tema = Tema.fraOffisiellKode(forsendelsesinfo.getTema().getValue());

        // I påvente av MMA-4323
        if (Tema.OMS.equals(tema)) {
            LOG.info("FPFORDEL ignorerer journalpost {} med tema OMS", arkivId);
            return;
        }

        MottakMeldingDataWrapper hentFraJoarkMelding = new MottakMeldingDataWrapper(
                new ProsessTaskData(HentDataFraJoarkTask.TASKNAME));
        settForsendelseInformasjonPåWrapper(arkivId, behandlingTema, tema, hentFraJoarkMelding);

        ProsessTaskData prosessTaskData = hentFraJoarkMelding.getProsessTaskData();
        prosessTaskData.setCallIdFraEksisterende();
        prosessTaskRepository.lagre(prosessTaskData);

    }

    private void settForsendelseInformasjonPåWrapper(String arkivId,
            BehandlingTema behandlingTema,
            Tema tema,
            MottakMeldingDataWrapper dataWrapper) {
        dataWrapper.setArkivId(arkivId);
        dataWrapper.setTema(tema);
        dataWrapper.setBehandlingTema(behandlingTema);
    }
}
