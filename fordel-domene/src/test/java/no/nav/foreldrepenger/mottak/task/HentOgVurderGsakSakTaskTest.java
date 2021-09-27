package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.RelevantSakSjekker;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.tjeneste.VurderInfotrygd;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class HentOgVurderGsakSakTaskTest {

    private static final String BRUKER_FNR = "99999999899";
    private static final String ANNEN_PART_FNR = "99999999699";
    private static final String BRUKER_AKTØR_ID = "123";
    private static final String ANNEN_PART_ID = "124";

    @Mock
    Instance<Period> infotrygdSakGyldigPeriodeInstance;
    @Mock
    private Instance<Period> infotrygdAnnenPartGyldigPeriodeInstance;
    private VurderInfotrygd vurderInfotrygd;
    @Mock
    private InfotrygdTjeneste ws;
    @Mock
    private InfotrygdTjeneste fp;
    @Mock
    private PersonInformasjon mockAktørConsumer;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    @BeforeEach
    void setup() {
        when(infotrygdSakGyldigPeriodeInstance.get()).thenReturn(Period.parse("P10M"));
        when(infotrygdAnnenPartGyldigPeriodeInstance.get()).thenReturn(Period.parse("P18M"));
        when(mockAktørConsumer.hentPersonIdentForAktørId(BRUKER_AKTØR_ID)).thenReturn(Optional.of(BRUKER_FNR));
        when(mockAktørConsumer.hentAktørIdForPersonIdent(BRUKER_FNR)).thenReturn(Optional.of(BRUKER_AKTØR_ID));
        when(mockAktørConsumer.hentPersonIdentForAktørId(ANNEN_PART_ID)).thenReturn(Optional.of(ANNEN_PART_FNR));
        when(mockAktørConsumer.hentAktørIdForPersonIdent(ANNEN_PART_FNR)).thenReturn(Optional.of(ANNEN_PART_ID));
        RelevantSakSjekker relevansSjekker = new RelevantSakSjekker(fp);
        vurderInfotrygd = new VurderInfotrygd(relevansSjekker, mockAktørConsumer);

    }

    private static List<InfotrygdSak> createInfotrygdSaker(boolean inkluderInntektsmelding) {
        final List<InfotrygdSak> saker = new ArrayList<>();
        InfotrygdSak sak = new InfotrygdSak(LocalDate.now(), LocalDate.now());
        saker.add(sak);
        if (inkluderInntektsmelding) {
            sak = new InfotrygdSak(LocalDate.now(), LocalDate.now());
            saker.add(sak);
        }
        return saker;
    }

    @Test
    void test_doTask_ingenMatchendeInfotrygdSak() {
        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();
        assertThat(kreverManuellBehandling(wrapperIn)).isFalse();
    }

    private boolean kreverManuellBehandling(MottakMeldingDataWrapper wrapperIn) {
        return vurderInfotrygd.kreverManuellVurdering(wrapperIn);
    }

    @Test
    void test_doTask_infotrygdsak_i_gsak_men_ikke_relevant_sak_i_infotrygd() {
        when(ws.finnSakListe(eq(BRUKER_FNR), any())).thenReturn(createInfotrygdSaker(false));
        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();
        assertThat(kreverManuellBehandling(wrapperIn)).isFalse();
        verify(fp, times(1)).finnSakListe(any(), any());
    }

    @Test
    void test_doTask_gammel_infotrygdsak_i_gsak_skip_infotrygd() {

        when(ws.finnSakListe(eq(BRUKER_FNR), any())).thenReturn(new ArrayList<>());

        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();

        assertThat(kreverManuellBehandling(wrapperIn)).isFalse();

        verify(ws, times(0)).finnSakListe(eq(BRUKER_FNR), any());
    }

    @Test
    void test_doTask_infotrygdsak_i_gsak_og_relevant_i_infotrygd() {
        when(fp.finnSakListe(eq(BRUKER_FNR), any())).thenReturn(createInfotrygdSaker(true));
        when(fp.finnSakListe(eq(ANNEN_PART_FNR), any())).thenReturn(createInfotrygdSaker(true));
        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();
        assertThat(kreverManuellBehandling(wrapperIn)).isTrue();
    }

    private static MottakMeldingDataWrapper opprettMottaksMelding() {
        ProsessTaskData data = ProsessTaskData.forTaskType(new TaskType("DUMMY"));
        MottakMeldingDataWrapper wrapperIn = new MottakMeldingDataWrapper(data);
        wrapperIn.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        wrapperIn.setAktørId(BRUKER_AKTØR_ID);
        wrapperIn.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        wrapperIn.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        wrapperIn.setInntekstmeldingStartdato(LocalDate.now());
        wrapperIn.setAnnenPartId(ANNEN_PART_ID);
        return wrapperIn;
    }

}
