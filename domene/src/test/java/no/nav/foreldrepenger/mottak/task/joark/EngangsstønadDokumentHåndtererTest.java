package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.task.joark.JoarkTestsupport.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.sql.Clob;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@RunWith(MockitoJUnitRunner.class)
public class EngangsstønadDokumentHåndtererTest {

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    private ProsessTaskData taskData;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;
    private JoarkDokumentHåndterer håndterer;
    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();
    private String fastsattInntektsmeldingStartdatoFristForManuellBehandling = "2019-01-01";


    @Before
    public void setUp() throws Exception {
        initMocks(this);

        ProsessTaskRepository ptr = mock(ProsessTaskRepository.class);
        håndterer = mock(JoarkDokumentHåndterer.class);
        AktørConsumer aktørConsumer = mock(AktørConsumer.class);
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, kodeverkRepository, aktørConsumer, håndterer, fastsattInntektsmeldingStartdatoFristForManuellBehandling));
        when(håndterer.hentGyldigAktørFraMetadata(any())).thenReturn(Optional.of(AKTØR_ID));
        when(håndterer.hentGyldigAktørFraPersonident(any())).thenReturn(Optional.of(AKTØR_ID));
        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
        dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, taskData);
        dataWrapper.setArkivId(ARKIV_ID);
    }


    @Test
    public void skal_hente_skjema_om_engangsstoenad_foedsel_ikke_foedt_og_legge_informasjon_paa_wrapper() throws Exception {

        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        JournalMetadata<DokumentTypeId> metadata = joarkTestsupport.lagJournalMetadataStrukturert();
        doReturn(Collections.singletonList(metadata)).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        String xml = joarkTestsupport.readFile("testsoknader/foedsel-mor.xml");
        JournalDokument<DokumentTypeId> jdMock = new JournalDokument<DokumentTypeId>(metadata, xml);
        doReturn(jdMock).when(håndterer).hentJournalDokument(Collections.singletonList(metadata));

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(wrapper).isNotNull();

        assertThat(wrapper.getBarnTermindato()).hasValueSatisfying(s -> assertThat(s).isEqualTo("2017-07-01"));
        assertThat(wrapper.getBarnFodselsdato()).isNotPresent(); // fra søknadsxml
        assertThat(wrapper.getOmsorgsovertakelsedato()).isNotPresent(); // fra søknadsxml
        assertThat(wrapper.getDokumentTypeId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL));
        assertThat(wrapper.erStrukturertDokument()).hasValue(true);

        final Clob payload = wrapper.getProsessTaskData().getPayload();
        assertThat(payload).isNotNull();
        String payloadString = payload.getSubString(1, (int) payload.length());
        assertThat(payloadString).isEqualTo(xml);
    }

    @Test
    public void skal_hente_skjema_om_engangsstoenad_foedsel_foedt_og_legge_informasjon_paa_wrapper() throws Exception {

        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        JournalMetadata<DokumentTypeId> metadata = joarkTestsupport.lagJournalMetadataStrukturert();
        doReturn(Collections.singletonList(metadata)).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        String xml = joarkTestsupport.readFile("testsoknader/foedsel-mor-foedt.xml");
        JournalDokument<DokumentTypeId> jdMock = new JournalDokument<DokumentTypeId>(metadata, xml);
        doReturn(jdMock).when(håndterer).hentJournalDokument(Collections.singletonList(metadata));

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(wrapper).isNotNull();

        assertThat(wrapper.getBarnTermindato()).hasValueSatisfying(s -> assertThat(s).isEqualTo("2017-08-24"));
        assertThat(wrapper.getBarnFodselsdato()).hasValueSatisfying(s -> assertThat(s).isEqualTo("2017-08-30"));
        assertThat(wrapper.getOmsorgsovertakelsedato()).isNotPresent(); // fra søknadsxml
        assertThat(wrapper.erStrukturertDokument()).hasValue(true);

        assertThat(wrapper.getDokumentTypeId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL));

        final Clob payload = wrapper.getProsessTaskData().getPayload();
        assertThat(payload).isNotNull();
        String payloadString = payload.getSubString(1, (int) payload.length());
        assertThat(payloadString).isEqualTo(xml);
    }

    @Test
    public void skal_hente_skjema_om_engangsstoenad_adopsjon_og_legge_informasjon_paa_wrapper() throws Exception {

        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        JournalMetadata<DokumentTypeId> metadata = joarkTestsupport.lagJournalMetadataStrukturert(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        doReturn(Collections.singletonList(metadata)).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        String xml = joarkTestsupport.readFile("testsoknader/adopsjon-mor.xml");
        JournalDokument<DokumentTypeId> jdMock = new JournalDokument<DokumentTypeId>(metadata, xml);
        doReturn(jdMock).when(håndterer).hentJournalDokument(Collections.singletonList(metadata));

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(wrapper).isNotNull();

        assertThat(wrapper.getBarnTermindato()).isNotPresent(); // fra søknadsxml
        assertThat(wrapper.getOmsorgsovertakelsedato()).hasValueSatisfying(s -> assertThat(s).isEqualTo("2017-08-30"));
        assertThat(wrapper.getAdopsjonsbarnFodselsdatoer()).contains(LocalDate.parse("2007-03-30"), LocalDate.parse("2013-05-30")); // fra søknadsxml

        assertThat(wrapper.getDokumentTypeId()).hasValueSatisfying(s -> assertThat(s).isEqualTo(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON));

        final Clob payload = wrapper.getProsessTaskData().getPayload();
        assertThat(payload).isNotNull();
        String payloadString = payload.getSubString(1, (int) payload.length());
        assertThat(payloadString).isEqualTo(xml);
    }

    @Test
    public void test_skal_kaste_error_naar_behandlingstema_ulikt_i_wrapper_og_payload() throws Exception {

        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        JournalMetadata<DokumentTypeId> metadata = joarkTestsupport.lagJournalMetadataStrukturert();
        doReturn(Collections.singletonList(metadata)).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        String xml = joarkTestsupport.readFile("testsoknader/foedsel-mor.xml");
        JournalDokument<DokumentTypeId> jdMock = new JournalDokument<DokumentTypeId>(metadata, xml);
        doReturn(jdMock).when(håndterer).hentJournalDokument(Collections.singletonList(metadata));

        try {
            joarkTaskTestobjekt.doTask(dataWrapper);
            fail("forventet TekniskException");
        } catch (TekniskException e) {
            assertThat(e.getFeil().getKode()).isEqualTo("FP-404782");
        }
    }
}