package no.nav.foreldrepenger.mottak.task;

import static java.time.LocalDate.now;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.rest.RelevantSakSjekker;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
public class HentOgVurderInfotrygdSakTaskTest {

    private static final String AKTØR_BRUKER = "1";
    private static final String AKTØR_ANNEN_PART = "9";
    private static final String AKTØR_BRUKER_1 = "2";
    private static final String AKTØR_ANNEN_PART_1 = "8";
    private static final String AKTØR_BRUKER_2 = "3";
    private static final String AKTØR_ANNEN_PART_2 = "7";

    private static final String FNR_BRUKER = "99999999999";
    private static final String FNR_ANNEN_PART = "99999999899";
    private static final String FNR_BRUKER_1 = "99999999899";
    private static final String FNR_ANNEN_PART_1 = "99999999999";
    private static final String FNR_BRUKER_2 = "99999999899";
    private static final String FNR_ANNEN_PART_2 = "99999999899";

    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    @Mock
    private AktørConsumerMedCache aktør;
    @Mock
    private InfotrygdTjeneste svp;
    @Mock
    private InfotrygdTjeneste fp;

    @BeforeEach
    public void setup() {
        expectAktørFnrMappings();
    }

    @Test
    public void skal_finne_relevant_registrert_infotrygdsak_for_inntektsmelding() throws Exception {
        var it1 = new InfotrygdSak(now().minusYears(2),
                now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusMonths(1), now().minusMonths(1));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now());

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_finne_relevant_iverksatt_infotrygdsak_for_inntektsmelding_bruker1() throws Exception {
        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusDays(1), now().minusDays(1));
        expectIT(FNR_BRUKER_1, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER_1);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now());

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_finne_relevant_infotrygdsak_for_inntektsmelding_avsluttet_vedtak() throws Exception {
        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusDays(50), now().minusDays(50));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now().minusDays(51));

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_ikke_lenger_sjekke_infotrygdsak_for_engangsstønad() throws Exception {
        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusDays(1), now().minusDays(1));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(ENGANGSSTØNAD_FØDSEL);
        w.setDokumentTypeId(SØKNAD_ENGANGSSTØNAD_FØDSEL);
        w.setInntekstmeldingStartdato(now());

        doAndAssertOpprettet(w);
    }

    @Test
    public void skal_finne_relevant_infotrygdsak_for_foreldrepenger() throws Exception {
        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusDays(1), now().minusDays(1));
        expectIT(FNR_ANNEN_PART, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        w.setAnnenPartId(AKTØR_ANNEN_PART);

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_opprette_sak_når_ingen_sak_for_foreldrepenger() throws Exception {

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        w.setAnnenPartId(AKTØR_ANNEN_PART);
        doAndAssertOpprettet(w);
    }

    @Test
    public void skal_opprette_sak_når_ingen_sak_for_svangerskapspenger() throws Exception {

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(SVANGERSKAPSPENGER);
        w.setDokumentTypeId(SØKNAD_SVANGERSKAPSPENGER);

        doAndAssertOpprettet(w);
    }

    @Test
    public void skal_til_gosys_når_det_finnes_sak_for_svangerskapspenger() throws Exception {

        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusMonths(1), now().minusMonths(1));
        expectIT(FNR_BRUKER_1, it1, it2);
        expectITRest(FNR_BRUKER_1, it2);

        var w = dataWrapper(AKTØR_BRUKER_1);
        w.setBehandlingTema(SVANGERSKAPSPENGER);
        w.setDokumentTypeId(SØKNAD_SVANGERSKAPSPENGER);

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_finne_relevant_registrert_infotrygdsak_for_inntektsmelding_svangerskapspenger() throws Exception {

        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusMonths(1), now().minusMonths(1));
        expectIT(FNR_BRUKER_1, it1, it2);
        expectITRest(FNR_BRUKER_1, it2);

        var w = dataWrapper(AKTØR_BRUKER_1);
        w.setBehandlingTema(SVANGERSKAPSPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_finne_relevant_infotrygdsak_for_medmor_foreldrepenger() throws Exception {

        var it = new InfotrygdSak(null, now().minusMonths(7));
        expectIT(FNR_ANNEN_PART_2, it);

        var w = dataWrapper(AKTØR_BRUKER_2);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        w.setAnnenPartId(AKTØR_ANNEN_PART_2);

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_ikke_sjekke_infotrygdsak_for_fedre_foreldrepenger() throws Exception {

        var w = dataWrapper(AKTØR_BRUKER_1);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        w.setAnnenPartId(AKTØR_ANNEN_PART_1);

        doAndAssertOpprettet(w);
    }

    @Test
    public void skal_sjekke_infotrygd_kun_for_bruker_ved_inntektsmelding() {

        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        expectIT(FNR_BRUKER, it1);
        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now());

        ArgumentCaptor<String> aktørCaptor = ArgumentCaptor.forClass(String.class);
        doAndAssertOpprettet(w);

        verify(aktør).hentPersonIdentForAktørId(aktørCaptor.capture());
        assertThat(aktørCaptor.getAllValues()).contains(AKTØR_BRUKER);
    }

    @Test
    public void neste_steg_skal_være_opprettsak_hvis_relevant_infotrygdsak_ikke_finnes() throws Exception {

        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        expectIT(FNR_BRUKER, it1);
        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setAnnenPartId(AKTØR_ANNEN_PART);

        doAndAssertOpprettet(w);
    }

    public void neste_steg_skal_throw_exception_hvis_annen_part_er_ikke_funnet() throws Exception {

        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        expectIT(FNR_ANNEN_PART, it1);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        doWithPrecondition(w);
    }

    @Test // (expected = VLException.class)
    public void skal_throw_exception_hvis_ukjent_behandlings_tema() throws Exception {

        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        expectIT(FNR_ANNEN_PART, it1);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(BehandlingTema.UDEFINERT);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        assertThrows(VLException.class, () -> doWithPrecondition(w));
    }

    private void doAndAssertOpprettet(MottakMeldingDataWrapper w) {
        doAndAssert(w, OpprettSakTask.TASKNAME);
    }

    private void doAndAssertJournalført(MottakMeldingDataWrapper w) {
        doAndAssert(w, MidlJournalføringTask.TASKNAME);
    }

    private void doAndAssert(MottakMeldingDataWrapper w, String name) {
        assertTaskType(doWithPrecondition(w), name);
    }

    private static void assertTaskType(MottakMeldingDataWrapper wrapper, String taskname) {
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(taskname);
    }

    private static MottakMeldingDataWrapper dataWrapper(String aktørBruker) {
        var w = new MottakMeldingDataWrapper(taskData());
        w.setAktørId(aktørBruker);
        w.setTema(FORELDRE_OG_SVANGERSKAPSPENGER);
        return w;

    }

    private static ProsessTaskData taskData() {
        var data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        return data;
    }

    private HentOgVurderInfotrygdSakTask task() {
        return new HentOgVurderInfotrygdSakTask(prosessTaskRepository,
                new RelevantSakSjekker(svp, fp),
                aktør);
    }

    private MottakMeldingDataWrapper doWithPrecondition(MottakMeldingDataWrapper wrapper) {
        var task = task();
        task.precondition(wrapper);
        return task.doTask(wrapper);
    }

    private void expectAktørFnrMappings() {
        expect(AKTØR_BRUKER, FNR_BRUKER);
        expect(AKTØR_BRUKER_1, FNR_BRUKER_1);
        expect(AKTØR_BRUKER_2, FNR_BRUKER_2);
        expect(AKTØR_ANNEN_PART, FNR_ANNEN_PART);
        expect(AKTØR_ANNEN_PART_1, FNR_ANNEN_PART_1);
        expect(AKTØR_ANNEN_PART_2, FNR_ANNEN_PART_2);
    }

    private void expectIT(String fnr, InfotrygdSak... itsaker) {
        lenient().when(svp.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
        lenient().when(fp.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
    }

    private void expectITRest(String fnr, InfotrygdSak... itsaker) {
        lenient().when(svp.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
        lenient().when(fp.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
    }

    private void expect(String aktørId, String fnr) {
        lenient().when(aktør.hentPersonIdentForAktørId(eq(aktørId))).thenReturn(Optional.of(fnr));
    }

}