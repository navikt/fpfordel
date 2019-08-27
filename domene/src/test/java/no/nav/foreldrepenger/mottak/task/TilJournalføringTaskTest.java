package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.journal.JournalFeil;
import no.nav.foreldrepenger.mottak.journal.JournalPost;
import no.nav.foreldrepenger.mottak.journal.JournalPostMangler;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class TilJournalføringTaskTest {
    private static final String ARKIV_ID = "234567";
    private static final String ARKIV_SAK_SYSTEM = Fagsystem.GOSYS.getOffisiellKode();
    private static final String SAKSNUMMER = "9876543";
    private static final String AKTØR_ID = "1234";
    private static final String BRUKER_FNR = "3011981234";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;

    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    @Mock
    private JournalTjeneste journalTjenesteMock;
    @Mock
    private EnhetsTjeneste enhetsTjenesteMock;
    @Mock
    private DokumentRepository dokumentRepositoryMock;
    @Mock
    private AktørConsumerMedCache aktørConsumerMock;

    private TilJournalføringTask task;
    private ProsessTaskData ptd;
    private String enhetId = "1234";
    private UUID forsendelseId;

    @Before
    public void setup() {
        forsendelseId = UUID.randomUUID();
        journalTjenesteMock = mock(JournalTjeneste.class);
        prosessTaskRepositoryMock = mock(ProsessTaskRepository.class);
        enhetsTjenesteMock = mock(EnhetsTjeneste.class);
        dokumentRepositoryMock = mock(DokumentRepository.class);
        aktørConsumerMock = mock(AktørConsumerMedCache.class);
        when(aktørConsumerMock.hentPersonIdentForAktørId(AKTØR_ID)).thenReturn(Optional.of(BRUKER_FNR));

        TilJournalføringTjeneste tilJournalføringTjeneste = new TilJournalføringTjeneste(journalTjenesteMock, dokumentRepositoryMock);

        task = new TilJournalføringTask(prosessTaskRepositoryMock, tilJournalføringTjeneste, enhetsTjenesteMock, kodeverkRepository, dokumentRepositoryMock, aktørConsumerMock);

        ptd = new ProsessTaskData(TilJournalføringTask.TASKNAME);
        ptd.setSekvens("1");

        when(enhetsTjenesteMock.hentFordelingEnhetId(any(), any(), any(), any())).thenReturn(enhetId);

    }

    @Test
    public void skal_teste_precondition() throws Exception {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);

        try {
            task.precondition(data);
            fail();
        } catch (VLException e) {
            assertThat(e.getFeil().getKode()).isEqualTo(MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty("", "", 0L).getKode());
        }
        data.setArkivId(ARKIV_ID);
        try {
            task.precondition(data);
            fail();
        } catch (VLException e) {
            assertThat(e.getFeil().getKode()).isEqualTo(MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty("", "", 0L).getKode());
        }
        data.setAktørId(AKTØR_ID);

        try {
            task.precondition(data);
            fail();
        } catch (VLException e) {
            assertThat(e.getFeil().getKode()).isEqualTo(MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty("", "", 0L).getKode());
        }
        data.setSaksnummer("asdf");

        task.precondition(data);
    }

    @Test
    public void test_utfor_uten_mangler() throws Exception {

        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        data.setArkivId(ARKIV_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setSaksnummer("123");
        data.setAktørId(AKTØR_ID);
        data.setForsendelseId(forsendelseId);

        final JournalPostMangler journalføringsbehov = new JournalPostMangler();
        when(journalTjenesteMock.utledJournalføringsbehov(ARKIV_ID)).thenReturn(journalføringsbehov);


        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType()).as("Forventer at sak uten mangler går videre til neste steg").isNotEqualTo(TilJournalføringTask.TASKNAME);
    }

    @Test
    public void test_mangler_arkivsak() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setForsendelseId(forsendelseId);
        final JournalPostMangler journalføringsbehov = new JournalPostMangler();
        journalføringsbehov.leggTilJournalMangel(JournalPostMangler.JournalMangelType.ARKIVSAK, true);
        when(journalTjenesteMock.utledJournalføringsbehov(ARKIV_ID)).thenReturn(journalføringsbehov);
        ArgumentCaptor<JournalPost> captor = ArgumentCaptor.forClass(JournalPost.class);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        verify(journalTjenesteMock).oppdaterJournalpost(captor.capture());

        JournalPost journalPost = captor.getValue();

        assertThat(journalPost.getArkivSakId()).isEqualTo(SAKSNUMMER);
        assertThat(journalPost.getArkivSakSystem().get()).isEqualTo(ARKIV_SAK_SYSTEM);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType()).as("Forventer at sak uten mangler går videre til neste steg").isNotEqualTo(TilJournalføringTask.TASKNAME);
    }

    @Test
    public void test_mangler_journalforing_gir_mangel_exception() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setForsendelseId(forsendelseId);
        final JournalPostMangler journalføringsbehov = new JournalPostMangler();
        when(journalTjenesteMock.utledJournalføringsbehov(ARKIV_ID)).thenReturn(journalføringsbehov);

        VLException funkExc = JournalFeil.FACTORY.journalfoeringFerdigstillingIkkeMulig(null).toException();
        doThrow(funkExc).when(journalTjenesteMock).ferdigstillJournalføring(any(String.class), any(String.class));

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType()).as("Forventer at sak med mangler går til Gosys").isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper data) {
        task.precondition(data);
        return task.doTask(data);
    }

    @Test
    public void test_mangler_ting_som_ikke_kan_rettes() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        final JournalPostMangler journalføringsbehov = new JournalPostMangler();
        journalføringsbehov.leggTilJournalMangel(JournalPostMangler.JournalMangelType.ARKIVSAK, true);
        journalføringsbehov.leggTilJournalMangel(JournalPostMangler.JournalMangelType.AVSENDERID, true);
        journalføringsbehov.leggTilJournalMangel(JournalPostMangler.JournalMangelType.BRUKER, true);
        journalføringsbehov.leggTilJournalMangel(JournalPostMangler.JournalMangelType.TEMA, true);
        journalføringsbehov.leggTilJournalMangel(JournalPostMangler.JournalMangelType.VEDLEGG_TITTEL, true);
        when(journalTjenesteMock.utledJournalføringsbehov(ARKIV_ID)).thenReturn(journalføringsbehov);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType()).as("Forventer at sak med dokumentmangler går til Gosys").isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test(expected = IntegrasjonException.class)
    public void skal_ved_funksjonell_feil_i_utledJournalføringsbehov_kaste_integrasjonException_noe_som_trigger_feilhåndterer_til_å_gå_til_manuell_behandling() {

        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        data.setArkivId(ARKIV_ID);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setSaksnummer("123");
        data.setAktørId(AKTØR_ID);

        VLException funkExc = JournalFeil.FACTORY.utledJournalfoeringsbehovJournalpostIkkeInngaaende(null).toException();
        when(journalTjenesteMock.utledJournalføringsbehov(any(String.class))).thenThrow(funkExc);

        doTaskWithPrecondition(data);
    }

    @Test(expected = IntegrasjonException.class)
    public void skal_ved_funksjonell_feil_i_oppdaterJournalpost_kaste_integrasjonException_noe_som_trigger_feilhåndterer_til_å_gå_til_manuell_behandling() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer("123");
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        final JournalPostMangler journalføringsbehov = new JournalPostMangler();
        journalføringsbehov.leggTilJournalMangel(JournalPostMangler.JournalMangelType.ARKIVSAK, true);
        when(journalTjenesteMock.utledJournalføringsbehov(ARKIV_ID)).thenReturn(journalføringsbehov);

        VLException funkExc = JournalFeil.FACTORY.oppdaterJournalpostObjektIkkeFunnet(null).toException();
        doThrow(funkExc).when(journalTjenesteMock).oppdaterJournalpost(any(JournalPost.class));

        doTaskWithPrecondition(data);
    }

    @Test(expected = IntegrasjonException.class)
    public void skal_ved_funksjonell_feil_i_ferdigstillJournalføring_kaste_integrasjonException_noe_som_trigger_feilhåndterer_til_å_gå_til_manuell_behandling() {

        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);
        data.setArkivId(ARKIV_ID);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setSaksnummer("123");
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        final JournalPostMangler journalføringsbehov = new JournalPostMangler();
        journalføringsbehov.leggTilJournalMangel(JournalPostMangler.JournalMangelType.ARKIVSAK, false);
        when(journalTjenesteMock.utledJournalføringsbehov(ARKIV_ID)).thenReturn(journalføringsbehov);

        VLException funkExc = JournalFeil.FACTORY.ferdigstillJournalfoeringObjektIkkeFunnet(null).toException();
        doThrow(funkExc).when(journalTjenesteMock).ferdigstillJournalføring(any(String.class), any(String.class));

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);
    }

    @Test
    public void test_validerDatagrunnlag_uten_feil() throws Exception {
        ProsessTaskData prosessTaskData = ptd;
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, prosessTaskData);

        data.setSaksnummer("saksnummer");
        data.setAktørId(AKTØR_ID);
        task.precondition(data);
    }

    @Test
    public void test_skalVedJournalføringAvDokumentForsendelseFåJournalTilstandEndeligJournalført() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);

        List<Dokument> dokumenter = DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        when(journalTjenesteMock.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class))).thenReturn(DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(JournalTilstand.ENDELIG_JOURNALFØRT, 3));
        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAKSNUMMER));
        when(dokumentRepositoryMock.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setSaksnummer(SAKSNUMMER);
        data.setRetryingTask("ABC");

        ArgumentCaptor<DokumentforsendelseRequest> dokCapture = ArgumentCaptor.forClass(DokumentforsendelseRequest.class);

        task.doTask(data);

        verify(journalTjenesteMock).journalførDokumentforsendelse(dokCapture.capture());
        verify(dokumentRepositoryMock).oppdaterForsendelseMetadata(any(UUID.class), any(), any(), any(ForsendelseStatus.class));

        DokumentforsendelseRequest request = dokCapture.getValue();
        assertThat(request.isRetrying()).isTrue();
        assertThat(request.getForsøkEndeligJF()).isTrue();
        assertThat(request.getSaksnummer()).isEqualTo(SAKSNUMMER);
    }

    @Test
    public void test_skalKasteTekniskExceptionNårJournalTilstandIkkeErEndelig() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(kodeverkRepository, ptd);

        List<Dokument> dokumenter = DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        when(journalTjenesteMock.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class))).thenReturn(DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(JournalTilstand.MIDLERTIDIG_JOURNALFØRT, 3));
        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAKSNUMMER));
        when(dokumentRepositoryMock.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setSaksnummer(SAKSNUMMER);

        data = task.doTask(data);

        verify(journalTjenesteMock).journalførDokumentforsendelse(any(DokumentforsendelseRequest.class));
        verify(dokumentRepositoryMock, times(1)).oppdaterForseldelseMedArkivId(any(UUID.class), any(), any(ForsendelseStatus.class));
        assertThat(data).isNotNull();
        assertThat(data.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }
}
