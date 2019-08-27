package no.nav.foreldrepenger.mottak.task;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSakTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdFeil;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjenesteImpl;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdUgyldigInputException;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class HentOgVurderInfotrygdSakTaskTest {

    private static final String AKTØR_BRUKER = "1";
    private static final String AKTØR_ANNEN_PART = "124";
    private static final String AKTØR_BRUKER_1 = "2";
    private static final String AKTØR_ANNEN_PART_1 = "248";
    private static final String AKTØR_BRUKER_2 = "3";
    private static final String AKTØR_ANNEN_PART_2 = "369";


    private static final String FNR_BRUKER = "11118017368";
    private static final String FNR_ANNEN_PART = "18119206878";
    private static final String FNR_BRUKER_1 = "18119211879";
    private static final String FNR_ANNEN_PART_1 = "11118068368";
    private static final String FNR_BRUKER_2 = "18119211879";
    private static final String FNR_ANNEN_PART_2 = "18119206878";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private ProsessTaskRepository prosessTaskRepository = mock(ProsessTaskRepository.class);
    private GsakSakTjeneste gsakSakTjeneste = mock(GsakSakTjeneste.class);
    private AktørConsumerMedCache aktørConsumer = mock(AktørConsumerMedCache.class);
    private InfotrygdTjeneste infotrygdTjeneste = mock(InfotrygdTjeneste.class);
    private Period infotrygdSakGyldigPeriode = Period.parse("P10M");
    private Period infotrygdAnnenPartGyldigPeriode = Period.parse("P18M");

    @Mock
    private Instance<Period> infotrygdSakGyldigPeriodeInstance;
    @Mock
    private Instance<Period> infotrygdAnnenPartGyldigPeriodeInstance;

    @Before
    public void setup() {
        when(infotrygdSakGyldigPeriodeInstance.get()).thenReturn(infotrygdSakGyldigPeriode);
        when(infotrygdAnnenPartGyldigPeriodeInstance.get()).thenReturn(infotrygdAnnenPartGyldigPeriode);
        when(aktørConsumer.hentPersonIdentForAktørId(eq(AKTØR_BRUKER))).thenReturn(Optional.of(FNR_BRUKER));
        when(aktørConsumer.hentPersonIdentForAktørId(eq(AKTØR_ANNEN_PART))).thenReturn(Optional.of(FNR_ANNEN_PART));
        when(aktørConsumer.hentPersonIdentForAktørId(eq(AKTØR_BRUKER_1))).thenReturn(Optional.of(FNR_BRUKER_1));
        when(aktørConsumer.hentPersonIdentForAktørId(eq(AKTØR_ANNEN_PART_1))).thenReturn(Optional.of(FNR_ANNEN_PART_1));
        when(aktørConsumer.hentPersonIdentForAktørId(eq(AKTØR_BRUKER_2))).thenReturn(Optional.of(FNR_BRUKER_2));
        when(aktørConsumer.hentPersonIdentForAktørId(eq(AKTØR_ANNEN_PART_2))).thenReturn(Optional.of(FNR_ANNEN_PART_2));
    }

    @Test(expected = InfotrygdUgyldigInputException.class)
    public void skal_håndtere_ugyldigInput_feil_fra_infotrygd() throws Exception {
        InfotrygdSakConsumer infotrygdSakConsumer = mock(InfotrygdSakConsumer.class);
        when(infotrygdSakConsumer.finnSakListe(any())).thenThrow(FinnSakListeUgyldigInput.class);
        GsakSak sak1 = new GsakSak("06016921295", "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak("06016921295", "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak("06016921295", "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_ANNEN_PART)).thenReturn(Arrays.asList(sak1, sak2, sak3));
        infotrygdTjeneste = new InfotrygdTjenesteImpl(infotrygdSakConsumer);

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        dataWrapper.setAnnenPartId(AKTØR_ANNEN_PART);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);

        task.doTask(dataWrapper);
    }

    @Test
    public void skal_håndtere_personIkkeFunnet_feil_fra_infotryg() throws Exception {
        InfotrygdSakConsumer infotrygdSakConsumer = mock(InfotrygdSakConsumer.class);
        when(infotrygdSakConsumer.finnSakListe(any())).thenThrow(FinnSakListePersonIkkeFunnet.class);
        GsakSak sak1 = new GsakSak("06016921295", "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak("06016921295", "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak("06016921295", "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_ANNEN_PART)).thenReturn(Arrays.asList(sak1, sak2, sak3));
        infotrygdTjeneste = new InfotrygdTjenesteImpl(infotrygdSakConsumer);

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        dataWrapper.setAnnenPartId(AKTØR_ANNEN_PART);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);

        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
    }

    @Test
    public void skal_finne_relevant_registrert_infotrygdsak_for_inntektsmelding() throws Exception {
        GsakSak sak1 = new GsakSak(FNR_BRUKER, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(FNR_BRUKER, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak(FNR_BRUKER, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        GsakSak sak4 = new GsakSak(FNR_BRUKER, "id4", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_BRUKER)).thenReturn(Arrays.asList(sak1, sak2, sak3, sak4));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        InfotrygdSak infotrygdSak2 = new InfotrygdSak("id4", "FA", "FØ", null, LocalDate.now().minusMonths(1));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_BRUKER), any())).thenReturn(Arrays.asList(infotrygdSak1, infotrygdSak2));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.INNTEKTSMELDING);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now());

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);
    }

    @Test
    public void skal_finne_relevant_iverksatt_infotrygdsak_for_inntektsmelding_bruker1() throws Exception {
        GsakSak sak1 = new GsakSak(FNR_BRUKER_1, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(FNR_BRUKER_1, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak(FNR_BRUKER_1, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        GsakSak sak4 = new GsakSak(FNR_BRUKER_1, "id4", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_BRUKER_1)).thenReturn(Arrays.asList(sak1, sak2, sak3, sak4));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        InfotrygdSak infotrygdSak2 = new InfotrygdSak("id4", "FA", "FØ", LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_BRUKER_1), any())).thenReturn(Arrays.asList(infotrygdSak1, infotrygdSak2));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER_1);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.INNTEKTSMELDING);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now());

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);
    }


    @Test
    public void skal_finne_relevant_infotrygdsak_for_inntektsmelding_avsluttet_vedtak() throws Exception {
        GsakSak sak1 = new GsakSak(FNR_BRUKER, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(FNR_BRUKER, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);

        when(gsakSakTjeneste.finnSaker(FNR_BRUKER)).thenReturn(Arrays.asList(sak1, sak2));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id1", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        InfotrygdSak infotrygdSak2 = new InfotrygdSak("id2", "FA", "FØ", LocalDate.now().minusDays(50), LocalDate.now().minusDays(50));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_BRUKER), any())).thenReturn(Arrays.asList(infotrygdSak1, infotrygdSak2));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.INNTEKTSMELDING);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now().minusDays(51));

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);
    }

    @Test
    public void skal_ikke_lenger_sjekke_infotrygdsak_for_engangsstønad() throws Exception {
        GsakSak sak1 = new GsakSak(FNR_BRUKER, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(FNR_BRUKER, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak(FNR_BRUKER, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        GsakSak sak4 = new GsakSak(FNR_BRUKER, "id4", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_BRUKER)).thenReturn(Arrays.asList(sak1, sak2, sak3, sak4));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        InfotrygdSak infotrygdSak2 = new InfotrygdSak("id4", "FA", "FØ", LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_BRUKER), any())).thenReturn(Arrays.asList(infotrygdSak1, infotrygdSak2));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now());

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
    }

    @Test
    public void skal_finne_relevant_infotrygdsak_for_foreldrepenger() throws Exception {
        GsakSak sak1 = new GsakSak(FNR_ANNEN_PART, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(FNR_ANNEN_PART, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak(FNR_ANNEN_PART, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        GsakSak sak4 = new GsakSak(FNR_ANNEN_PART, "id4", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_ANNEN_PART)).thenReturn(Arrays.asList(sak1, sak2, sak3, sak4));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        InfotrygdSak infotrygdSak2 = new InfotrygdSak("id4", "FA", "FØ", LocalDate.now().minusDays(1), LocalDate.now().minusDays(1));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_ANNEN_PART), any())).thenReturn(Arrays.asList(infotrygdSak1, infotrygdSak2));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now());
        dataWrapper.setAnnenPartId(AKTØR_ANNEN_PART);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);
    }

    @Test
    public void skal_opprette_sak_når_ingen_sak_for_foreldrepenger() throws Exception {
        when(gsakSakTjeneste.finnSaker(FNR_ANNEN_PART)).thenReturn(Collections.emptyList());

        when(infotrygdTjeneste.finnSakListe(eq(FNR_ANNEN_PART), any())).thenReturn(Collections.emptyList());

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now());
        dataWrapper.setAnnenPartId(AKTØR_ANNEN_PART);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
    }

    @Test
    public void skal_opprette_sak_når_ingen_sak_for_svangerskapspenger() throws Exception {

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
    }

    @Test
    public void skal_til_gosys_når_det_finnes_sak_for_svangerskapspenger() throws Exception {
        GsakSak sak1 = new GsakSak(FNR_BRUKER_1, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(FNR_BRUKER_1, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak(FNR_BRUKER_1, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        GsakSak sak4 = new GsakSak(FNR_BRUKER_1, "id4", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_BRUKER_1)).thenReturn(Arrays.asList(sak1, sak2, sak3, sak4));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        InfotrygdSak infotrygdSak2 = new InfotrygdSak("id4", "FA", "SV", LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_BRUKER_1), any())).thenReturn(Arrays.asList(infotrygdSak1, infotrygdSak2));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER_1);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);
    }


    @Test
    public void skal_finne_relevant_registrert_infotrygdsak_for_inntektsmelding_svangerskapspenger() throws Exception {
        GsakSak sak1 = new GsakSak(FNR_BRUKER_1, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(FNR_BRUKER_1, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak(FNR_BRUKER_1, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        GsakSak sak4 = new GsakSak(FNR_BRUKER_1, "id4", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_BRUKER_1)).thenReturn(Arrays.asList(sak1, sak2, sak3, sak4));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        InfotrygdSak infotrygdSak2 = new InfotrygdSak("id4", "FA", "SV", LocalDate.now().minusMonths(1), LocalDate.now().minusMonths(1));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_BRUKER_1), any())).thenReturn(Arrays.asList(infotrygdSak1, infotrygdSak2));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER_1);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.INNTEKTSMELDING);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);
    }

    @Test
    public void skal_finne_relevant_infotrygdsak_for_medmor_foreldrepenger() throws Exception {
        GsakSak sak2 = new GsakSak(FNR_ANNEN_PART_2, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        when(gsakSakTjeneste.finnSaker(FNR_ANNEN_PART_2)).thenReturn(Arrays.asList(sak2));

        InfotrygdSak infotrygdSak2 = new InfotrygdSak("id4", "FA", "FØ", null, LocalDate.now().minusMonths(7));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_ANNEN_PART_2), any())).thenReturn(Arrays.asList(infotrygdSak2));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER_2);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now());
        dataWrapper.setAnnenPartId(AKTØR_ANNEN_PART_2);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);
    }

    @Test
    public void skal_ikke_sjekke_infotrygdsak_for_fedre_foreldrepenger() throws Exception {
        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER_1);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now());
        dataWrapper.setAnnenPartId(AKTØR_ANNEN_PART_1);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
    }

    @Test
    public void skal_sjekke_infotrygd_kun_for_bruker_ved_inntektsmelding() {
        GsakSak sak1 = new GsakSak(FNR_BRUKER, "id1", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD,  LocalDate.now().minusYears(2));
        when(gsakSakTjeneste.finnSaker(FNR_BRUKER)).thenReturn(Arrays.asList(sak1));
        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_BRUKER), any())).thenReturn(Arrays.asList(infotrygdSak1));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.INNTEKTSMELDING);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now());

        ArgumentCaptor<String> aktørCaptor = ArgumentCaptor.forClass(String.class);
        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);

        verify(aktørConsumer, times(1)).hentPersonIdentForAktørId(aktørCaptor.capture());
        List<String> fnrArguments = aktørCaptor.getAllValues();
        assertThat(fnrArguments).contains(AKTØR_BRUKER);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
    }

    @Test
    public void neste_steg_skal_være_opprettsak_hvis_relevant_infotrygdsak_ikke_finnes() throws Exception {
        GsakSak sak1 = new GsakSak("06016921295", "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak("06016921295", "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak("06016921295", "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(any())).thenReturn(Arrays.asList(sak1, sak2, sak3));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        when(infotrygdTjeneste.finnSakListe(any(), any())).thenReturn(Arrays.asList(infotrygdSak1));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        dataWrapper.setAnnenPartId(AKTØR_ANNEN_PART);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(task, dataWrapper);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettSakTask.TASKNAME);
    }

    @Test
    public void neste_steg_skal_være_retry_hvis_nedetid() {
        GsakSak sak1 = new GsakSak("06016921295", "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak("06016921295", "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak("06016921295", "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(any())).thenReturn(Arrays.asList(sak1, sak2, sak3));

        @SuppressWarnings("unused")
        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        when(infotrygdTjeneste.finnSakListe(any(), any())).thenThrow(InfotrygdFeil.FACTORY.nedetid("InfotrygdSak", null).toException());

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD);
        dataWrapper.setAnnenPartId(AKTØR_ANNEN_PART);
        try {
            doTaskWithPrecondition(task, dataWrapper);
        } catch (TekniskException e) {
            assertThat(e.getFeil().getKode()).isEqualTo("FP-180124");
        }
    }


    @Test
    public void neste_steg_skal_throw_exception_hvis_annen_part_er_ikke_funnet() throws Exception {
        GsakSak sak1 = new GsakSak(AKTØR_ANNEN_PART, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(AKTØR_ANNEN_PART, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak(AKTØR_ANNEN_PART, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_ANNEN_PART)).thenReturn(Arrays.asList(sak1, sak2, sak3));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_ANNEN_PART), any())).thenReturn(Arrays.asList(infotrygdSak1));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-941984");

        doTaskWithPrecondition(task, dataWrapper);
    }

    @Test
    public void skal_throw_exception_hvis_ukent_behandlings_tema() throws Exception {
        GsakSak sak1 = new GsakSak(FNR_ANNEN_PART, "id1", Tema.UDEFINERT, Fagsystem.INFOTRYGD);
        GsakSak sak2 = new GsakSak(FNR_ANNEN_PART, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.INFOTRYGD);
        GsakSak sak3 = new GsakSak(FNR_ANNEN_PART, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, Fagsystem.FPSAK);
        when(gsakSakTjeneste.finnSaker(FNR_ANNEN_PART)).thenReturn(Arrays.asList(sak1, sak2, sak3));

        InfotrygdSak infotrygdSak1 = new InfotrygdSak("id3", "FA", "FE", LocalDate.now().minusYears(2), LocalDate.now().minusYears(2));
        when(infotrygdTjeneste.finnSakListe(eq(FNR_ANNEN_PART), any())).thenReturn(Arrays.asList(infotrygdSak1));

        HentOgVurderInfotrygdSakTask task = new HentOgVurderInfotrygdSakTask(prosessTaskRepository, kodeverkRepository, gsakSakTjeneste, infotrygdTjeneste, aktørConsumer, infotrygdSakGyldigPeriodeInstance, infotrygdAnnenPartGyldigPeriodeInstance);

        ProsessTaskData data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, data);
        dataWrapper.setAktørId(AKTØR_BRUKER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.UDEFINERT);
        dataWrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        dataWrapper.setInntekstmeldingStartdato(LocalDate.now());

        expectedException.expect(VLException.class);
        expectedException.expectMessage("FP-286143");

        doTaskWithPrecondition(task, dataWrapper);
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(HentOgVurderInfotrygdSakTask task, MottakMeldingDataWrapper dataWrapper) {
        task.precondition(dataWrapper);
        return task.doTask(dataWrapper);
    }
}
