package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.VurderFagsystemResultat;
import no.nav.foreldrepenger.mottak.tjeneste.DestinasjonsRuter;
import no.nav.foreldrepenger.mottak.tjeneste.VurderInfotrygd;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(MockitoExtension.class)
class HentOgVurderVLSakTaskTest {

    @Mock
    private Fagsak fagsakRestKlientMock;
    @Mock
    private VurderInfotrygd vurderInfotrygd;
    private String saksnummer = "123456";
    private String aktørId = "9000000000009";

    private LocalDate termindato = LocalDate.now();

    @Test
    void neste_steg_skal_være_til_journalføring_når_vurderFagsystem_returnerer_VL_og_saksnummer() {
        when(fagsakRestKlientMock.vurderFagsystem(any())).thenReturn(lagFagsystemSvar(saksnummer, true, false, false));

        var vurder = new DestinasjonsRuter(vurderInfotrygd, fagsakRestKlientMock);
        var dataWrapper = lagDataWrapper();

        var result = vurder.bestemDestinasjon(dataWrapper);

        assertThat(result.system()).isEqualTo(ForsendelseStatus.FPSAK);
        assertThat(result.saksnummer()).isEqualTo(saksnummer);
    }

    @Test
    void neste_steg_skal_være_til_journalføring_når_vurderFagsystem_returnerer_VL_uten_saksnummer() {
        when(fagsakRestKlientMock.vurderFagsystem(any())).thenReturn(lagFagsystemSvar(null, true, false, false));

        var vurder = new DestinasjonsRuter(vurderInfotrygd, fagsakRestKlientMock);
        var dataWrapper = lagDataWrapper();

        var result = vurder.bestemDestinasjon(dataWrapper);

        assertThat(result.system()).isEqualTo(ForsendelseStatus.FPSAK);
        assertThat(result.saksnummer()).isNull();
    }

    @Test
    void neste_steg_skal_være_hentOgVurderInfotrygd_når_vurderFagsystem_returnerer_sjekkInfotrygd() {
        when(fagsakRestKlientMock.vurderFagsystem(any())).thenReturn(lagFagsystemSvar(null, false, true, false));

        var vurder = new DestinasjonsRuter(vurderInfotrygd, fagsakRestKlientMock);
        var dataWrapper = lagDataWrapper();

        var result = vurder.bestemDestinasjon(dataWrapper);

        assertThat(result.system()).isEqualTo(ForsendelseStatus.FPSAK);
        assertThat(result.saksnummer()).isNull();
    }

    @Test
    void neste_steg_skal_være_opprettGsakOppgave_når_vurderFagsystem_returnerer_manuell_vurdering() {
        when(fagsakRestKlientMock.vurderFagsystem(any())).thenReturn(lagFagsystemSvar(null, false, false, true));

        var vurder = new DestinasjonsRuter(vurderInfotrygd, fagsakRestKlientMock);
        var dataWrapper = lagDataWrapper();

        var result = vurder.bestemDestinasjon(dataWrapper);

        assertThat(result.system()).isEqualTo(ForsendelseStatus.GOSYS);
        assertThat(result.saksnummer()).isNull();
    }

    @Test
    void neste_steg_skal_være_opprettGsakOppgave_når_vurderFagsystem_returnerer_VL_uten_saksnummer_og_barnet_er_født_før_19_og_annen_part_har_rett() {
        when(fagsakRestKlientMock.vurderFagsystem(any())).thenReturn(lagFagsystemSvar(null, true, false, false));

        var dataWrapper = lagDataWrapper();
        dataWrapper.setAnnenPartHarRett(true);
        dataWrapper.setBarnFodselsdato(LocalDate.of(2018, 5, 17));
        var vurder = new DestinasjonsRuter(vurderInfotrygd, fagsakRestKlientMock);

        var result = vurder.bestemDestinasjon(dataWrapper);

        assertThat(result.system()).isEqualTo(ForsendelseStatus.GOSYS);
        assertThat(result.saksnummer()).isNull();
    }

    @Test
    void neste_steg_skal_være_til_journalføring_når_vurderFagsystem_returnerer_VL_og_saksnummer_selv_om_barnet_er_født_før_19_og_annen_part_har_rett() {
        when(fagsakRestKlientMock.vurderFagsystem(any())).thenReturn(lagFagsystemSvar(saksnummer, true, false, false));

        var dataWrapper = lagDataWrapper();
        dataWrapper.setAnnenPartHarRett(true);
        dataWrapper.setBarnFodselsdato(LocalDate.of(2018, 5, 17));

        var vurder = new DestinasjonsRuter(vurderInfotrygd, fagsakRestKlientMock);

        var result = vurder.bestemDestinasjon(dataWrapper);

        assertThat(result.system()).isEqualTo(ForsendelseStatus.FPSAK);
        assertThat(result.saksnummer()).isEqualTo(saksnummer);
    }

    @Test
    void neste_steg_skal_være_til_journalføring_når_vurderFagsystem_returnerer_VL_og_saksnummer_når_omsorg_før_19() {
        when(fagsakRestKlientMock.vurderFagsystem(any())).thenReturn(lagFagsystemSvar(saksnummer, true, false, false));

        var dataWrapper = lagDataWrapper();
        dataWrapper.setOmsorgsovertakelsedato(LocalDate.of(2018, 5, 17));

        var vurder = new DestinasjonsRuter(vurderInfotrygd, fagsakRestKlientMock);

        var result = vurder.bestemDestinasjon(dataWrapper);

        assertThat(result.system()).isEqualTo(ForsendelseStatus.FPSAK);
        assertThat(result.saksnummer()).isEqualTo(saksnummer);
    }

    @Test
    void neste_steg_skal_være_tilJournalføring_når_vl_returnerer_saksnummer_for_svangerskapspenger() {
        when(fagsakRestKlientMock.vurderFagsystem(any())).thenReturn(lagFagsystemSvar(saksnummer, true, false, false));

        var data = ProsessTaskData.forTaskType(new TaskType("TEST"));
        var dataWrapper = new MottakMeldingDataWrapper(data);
        dataWrapper.setAktørId(aktørId);
        dataWrapper.setBarnTermindato(termindato);
        dataWrapper.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);

        var vurder = new DestinasjonsRuter(vurderInfotrygd, fagsakRestKlientMock);

        var result = vurder.bestemDestinasjon(dataWrapper);

        assertThat(result.system()).isEqualTo(ForsendelseStatus.FPSAK);
        assertThat(result.saksnummer()).isEqualTo(saksnummer);
    }

    private MottakMeldingDataWrapper lagDataWrapper() {
        var data = ProsessTaskData.forTaskType(new TaskType("TEST"));
        var dataWrapper = new MottakMeldingDataWrapper(data);
        dataWrapper.setAktørId(aktørId);
        dataWrapper.setBarnTermindato(termindato);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        return dataWrapper;
    }

    private static VurderFagsystemResultat lagFagsystemSvar(String saksnummer, boolean behandlesIVL,
                                                            boolean sjekkIT, boolean gsak) {
        VurderFagsystemResultat res = new VurderFagsystemResultat();
        res.setSaksnummer(saksnummer);
        res.setBehandlesIVedtaksløsningen(behandlesIVL);
        res.setManuellVurdering(gsak);
        res.setSjekkMotInfotrygd(sjekkIT);
        return res;
    }
}