package no.nav.foreldrepenger.mottak.queue;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.joark.HentDataFraJoarkTask;
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.Forsendelsesinformasjon;
import no.nav.vedtak.felles.AktiverContextOgTransaksjon;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@AktiverContextOgTransaksjon
public class ProsesstaskMeldingsfordeler implements MeldingsFordeler {

    private ProsessTaskRepository prosessTaskRepository;
    private KodeverkRepository kodeverkRepository;

    ProsesstaskMeldingsfordeler() {//NOSONAR.
        // for CDI proxy
    }

    @Inject
    public ProsesstaskMeldingsfordeler(ProsessTaskRepository prosessTaskRepository,
                                       KodeverkRepository kodeverkRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.kodeverkRepository = kodeverkRepository;
    }

    @Override
    public void execute(Forsendelsesinformasjon forsendelsesinfo) {
        final String arkivId = forsendelsesinfo.getArkivId();
        final BehandlingTema behandlingTema;
        if (forsendelsesinfo.getBehandlingstema() != null) {
            var behandlingstemaOffisiellKode = forsendelsesinfo.getBehandlingstema().getValue();
            behandlingTema = kodeverkRepository.finnForKodeverkEiersKode(BehandlingTema.class, behandlingstemaOffisiellKode, BehandlingTema.UDEFINERT);
        } else {
            behandlingTema = BehandlingTema.UDEFINERT;
        }

        final String temaOffisiellKode = forsendelsesinfo.getTema().getValue();
        final Tema tema = kodeverkRepository.finnForKodeverkEiersKode(Tema.class, temaOffisiellKode);

        MottakMeldingDataWrapper hentFraJoarkMelding = new MottakMeldingDataWrapper(kodeverkRepository, new ProsessTaskData(HentDataFraJoarkTask.TASKNAME));
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
