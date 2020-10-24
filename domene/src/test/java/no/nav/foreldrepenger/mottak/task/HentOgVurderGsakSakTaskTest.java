package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
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

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.rest.RelevantSakSjekker;
import no.nav.foreldrepenger.mottak.person.PersonTjeneste;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

//@RunWith(CdiRunner.class)
@ExtendWith(MockitoExtension.class)
public class HentOgVurderGsakSakTaskTest {

    private static final String BRUKER_FNR = "99999999899";
    private static final String ANNEN_PART_FNR = "99999999699";
    private static final String BRUKER_AKTØR_ID = "123";
    private static final String ANNEN_PART_ID = "124";

    @Mock
    Instance<Period> infotrygdSakGyldigPeriodeInstance;
    @Mock
    private Instance<Period> infotrygdAnnenPartGyldigPeriodeInstance;
    private HentOgVurderInfotrygdSakTask task;
    @Mock
    private InfotrygdTjeneste ws;
    @Mock
    private InfotrygdTjeneste fp;
    @Mock
    private PersonTjeneste mockAktørConsumer;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    @BeforeEach
    public void setup() {
        ProsessTaskRepository mockProsessTaskRepository = mock(ProsessTaskRepository.class);
        lenient().when(infotrygdSakGyldigPeriodeInstance.get()).thenReturn(Period.parse("P10M"));
        lenient().when(infotrygdAnnenPartGyldigPeriodeInstance.get()).thenReturn(Period.parse("P18M"));
        lenient().when(mockAktørConsumer.hentPersonIdentForAktørId(BRUKER_AKTØR_ID)).thenReturn(Optional.of(BRUKER_FNR));
        lenient().when(mockAktørConsumer.hentAktørIdForPersonIdent(BRUKER_FNR)).thenReturn(Optional.of(BRUKER_AKTØR_ID));

        lenient().when(mockAktørConsumer.hentPersonIdentForAktørId(ANNEN_PART_ID)).thenReturn(Optional.of(ANNEN_PART_FNR));
        lenient().when(mockAktørConsumer.hentAktørIdForPersonIdent(ANNEN_PART_FNR)).thenReturn(Optional.of(ANNEN_PART_ID));
        RelevantSakSjekker relevansSjekker = new RelevantSakSjekker(fp);
        task = new HentOgVurderInfotrygdSakTask(mockProsessTaskRepository, relevansSjekker, mockAktørConsumer);

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
    public void test_doTask_ingenMatchendeInfotrygdSak() {

        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();

        MottakMeldingDataWrapper wrapperOut = doTaskWithPrecondition(wrapperIn);

        assertThat(wrapperOut).isNotNull();
        assertThat(wrapperOut.getTema()).isEqualTo(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        assertThat(wrapperOut.getAktørId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(BRUKER_AKTØR_ID));
        assertThat(wrapperOut.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper wrapperIn) {
        task.precondition(wrapperIn);
        return task.doTask(wrapperIn);
    }

    @Test
    public void test_doTask_infotrygdsak_i_gsak_men_ikke_relevant_sak_i_infotrygd() {
        lenient().when(ws.finnSakListe(eq(BRUKER_FNR), any())).thenReturn(createInfotrygdSaker(false));

        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();

        MottakMeldingDataWrapper wrapperOut = doTaskWithPrecondition(wrapperIn);

        assertThat(wrapperOut).isNotNull();
        assertThat(wrapperOut.getTema()).isEqualTo(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        assertThat(wrapperOut.getAktørId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(BRUKER_AKTØR_ID));
        assertThat(wrapperOut.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
        verify(fp, times(1)).finnSakListe(any(), any());
    }

    @Test
    public void test_doTask_gammel_infotrygdsak_i_gsak_skip_infotrygd() {

        lenient().when(ws.finnSakListe(eq(BRUKER_FNR), any())).thenReturn(new ArrayList<>());

        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();

        MottakMeldingDataWrapper wrapperOut = doTaskWithPrecondition(wrapperIn);

        assertThat(wrapperOut).isNotNull();
        assertThat(wrapperOut.getTema()).isEqualTo(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        assertThat(wrapperOut.getAktørId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(BRUKER_AKTØR_ID));
        assertThat(wrapperOut.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
        verify(ws, times(0)).finnSakListe(eq(BRUKER_FNR), any());
    }

    @Test
    public void test_doTask_infotrygdsak_i_gsak_og_relevant_i_infotrygd() {

        lenient().when(fp.finnSakListe(eq(BRUKER_FNR), any())).thenReturn(createInfotrygdSaker(true));

        when(fp.finnSakListe(eq(ANNEN_PART_FNR), any())).thenReturn(createInfotrygdSaker(true));

        MottakMeldingDataWrapper wrapperIn = opprettMottaksMelding();

        MottakMeldingDataWrapper wrapperOut = doTaskWithPrecondition(wrapperIn);

        assertTaskResult_WhenExceptingManuellJornalføring(wrapperOut);
    }

    @Test
    public void test_validerDatagrunnlag_skal_feile_ved_manglende_temakode_og_behandlingstype() {
        MottakMeldingDataWrapper meldingDataWrapper = new MottakMeldingDataWrapper(
                new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME));
        meldingDataWrapper.setArkivId("123454");
        meldingDataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        assertThrows(TekniskException.class, () -> task.precondition(meldingDataWrapper));
    }

    @Test
    public void test_validerDatagrunnlag_uten_feil() {
        MottakMeldingDataWrapper meldingIn = opprettMottaksMelding();
        meldingIn.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        task.precondition(meldingIn);
    }

    private static MottakMeldingDataWrapper opprettMottaksMelding() {
        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper wrapperIn = new MottakMeldingDataWrapper(data);
        wrapperIn.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        wrapperIn.setAktørId(BRUKER_AKTØR_ID);
        wrapperIn.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        wrapperIn.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        wrapperIn.setInntekstmeldingStartdato(LocalDate.now());
        wrapperIn.setAnnenPartId(ANNEN_PART_ID);
        return wrapperIn;
    }

    private static void assertTaskResult_WhenExceptingManuellJornalføring(MottakMeldingDataWrapper wrapperOut) {
        assertThat(wrapperOut).isNotNull();
        assertThat(wrapperOut.getTema()).isEqualTo(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        assertThat(wrapperOut.getAktørId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(BRUKER_AKTØR_ID));
        assertThat(wrapperOut.getProsessTaskData().getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);
    }
}
