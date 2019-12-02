package no.nav.foreldrepenger.mottak.task;

import static java.time.LocalDate.now;
import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema.ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema.FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema.SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverk.Fagsystem.FPSAK;
import static no.nav.foreldrepenger.fordel.kodeverk.Fagsystem.INFOTRYGD;
import static no.nav.foreldrepenger.fordel.kodeverk.Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
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
import no.nav.foreldrepenger.mottak.infotrygd.rest.RelevantSakSjekker;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class HentOgVurderInfotrygdSakTaskTest {
    private static final String FNR = "06016921295";
    private static final Period IT_GYLDIG_PERIODE = Period.ofMonths(10);
    private static final Period IT_ANNENPART_GYLDIG_PERIODE = Period.ofMonths(18);

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
    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    @Mock
    private GsakSakTjeneste gsak;
    @Mock
    private AktørConsumerMedCache aktør;
    @Mock
    private InfotrygdTjeneste infotrygd;
    @Mock
    private InfotrygdTjeneste svp;
    @Mock
    private InfotrygdTjeneste fp;
    @Mock
    private Unleash unleash;
    @Mock
    private Instance<Period> itSakGyldigPeriode;
    @Mock
    private Instance<Period> itAnnenPartGyldigPeriode;

    @Before
    public void setup() {
        when(unleash.isEnabled(anyString())).thenReturn(true);
        when(itSakGyldigPeriode.get()).thenReturn(IT_GYLDIG_PERIODE);
        when(itAnnenPartGyldigPeriode.get()).thenReturn(IT_ANNENPART_GYLDIG_PERIODE);
        expectAktørFnrMappings();
    }

    @Test(expected = InfotrygdUgyldigInputException.class)
    public void skal_håndtere_ugyldigInput_feil_fra_infotrygd() throws Exception {
        var infotrygdKonsument = mock(InfotrygdSakConsumer.class);
        when(infotrygdKonsument.finnSakListe(any())).thenThrow(FinnSakListeUgyldigInput.class);
        expectGsaker(FNR_ANNEN_PART, gsaker(FNR));
        infotrygd = new InfotrygdTjenesteImpl(infotrygdKonsument);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setAnnenPartId(AKTØR_ANNEN_PART);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);

        doTask(w);
    }

    @Test
    public void skal_håndtere_personIkkeFunnet_feil_fra_infotryg() throws Exception {
        var infotrygdSakConsumer = mock(InfotrygdSakConsumer.class);
        when(fp.finnSakListe(any(), any())).thenReturn(Collections.emptyList());
        when(svp.finnSakListe(any(), any())).thenReturn(Collections.emptyList());
        when(infotrygdSakConsumer.finnSakListe(any())).thenThrow(FinnSakListePersonIkkeFunnet.class);
        expectGsaker(FNR_ANNEN_PART, gsaker(FNR));
        infotrygd = new InfotrygdTjenesteImpl(infotrygdSakConsumer);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setAnnenPartId(AKTØR_ANNEN_PART);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);
        doAndAssertOpprettet(w);
    }

    @Test
    public void skal_finne_relevant_registrert_infotrygdsak_for_inntektsmelding() throws Exception {
        var g1 = new GsakSak(FNR_BRUKER, "id1", Tema.UDEFINERT, INFOTRYGD);
        var g2 = new GsakSak(FNR_BRUKER, "id2", FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD);
        var g3 = new GsakSak(FNR_BRUKER, "id3", FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        var g4 = new GsakSak(FNR_BRUKER, "id4", FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        expectGsak(FNR_BRUKER, g1, g2, g3, g4);
        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2),
                now().minusYears(2));
        var it2 = new InfotrygdSak("id4", "FA", "FØ", null, now().minusMonths(1));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now());

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_finne_relevant_iverksatt_infotrygdsak_for_inntektsmelding_bruker1() throws Exception {
        var g1 = new GsakSak(FNR_BRUKER_1, "id1", Tema.UDEFINERT, INFOTRYGD);
        var g2 = new GsakSak(FNR_BRUKER_1, "id2", FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD);
        var g3 = new GsakSak(FNR_BRUKER_1, "id3", FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        var g4 = new GsakSak(FNR_BRUKER_1, "id4", FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        expectGsak(FNR_BRUKER_1, g1, g2, g3, g4);

        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak("id4", "FA", "FØ", now().minusDays(1), now().minusDays(1));
        expectIT(FNR_BRUKER_1, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER_1);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now());

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_finne_relevant_infotrygdsak_for_inntektsmelding_avsluttet_vedtak() throws Exception {
        var g1 = new GsakSak(FNR_BRUKER, "id1", Tema.UDEFINERT, INFOTRYGD);
        var g2 = new GsakSak(FNR_BRUKER, "id2", FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD);
        expectGsak(FNR_BRUKER, g1, g2);

        var it1 = new InfotrygdSak("id1", "FA", "FE", now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak("id2", "FA", "FØ", now().minusDays(50), now().minusDays(50));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now().minusDays(51));

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_ikke_lenger_sjekke_infotrygdsak_for_engangsstønad() throws Exception {
        var g1 = new GsakSak(FNR_BRUKER, "id1", Tema.UDEFINERT, INFOTRYGD);
        var g2 = new GsakSak(FNR_BRUKER, "id2", FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD);
        var g3 = new GsakSak(FNR_BRUKER, "id3", FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        var g4 = new GsakSak(FNR_BRUKER, "id4", FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        expectGsak(FNR_BRUKER, g1, g2, g3, g4);

        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak("id4", "FA", "FØ", now().minusDays(1), now().minusDays(1));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(ENGANGSSTØNAD_FØDSEL);
        w.setDokumentTypeId(SØKNAD_ENGANGSSTØNAD_FØDSEL);
        w.setInntekstmeldingStartdato(now());

        doAndAssertOpprettet(w);
    }

    @Test
    public void skal_finne_relevant_infotrygdsak_for_foreldrepenger() throws Exception {
        var g1 = new GsakSak(FNR_ANNEN_PART, "id1", Tema.UDEFINERT, INFOTRYGD);
        var g2 = new GsakSak(FNR_ANNEN_PART, "id2", FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD);
        var g3 = new GsakSak(FNR_ANNEN_PART, "id3", FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        var g4 = new GsakSak(FNR_ANNEN_PART, "id4", FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        expectGsak(FNR_ANNEN_PART, g1, g2, g3, g4);

        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak("id4", "FA", "FØ", now().minusDays(1), now().minusDays(1));
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
        when(gsak.finnSaker(FNR_ANNEN_PART)).thenReturn(emptyList());
        when(infotrygd.finnSakListe(eq(FNR_ANNEN_PART), any())).thenReturn(emptyList());

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
        var g1 = new GsakSak(FNR_BRUKER_1, "id1", Tema.UDEFINERT, INFOTRYGD);
        var g2 = new GsakSak(FNR_BRUKER_1, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD);
        var g3 = new GsakSak(FNR_BRUKER_1, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        var g4 = new GsakSak(FNR_BRUKER_1, "id4", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        expectGsak(FNR_BRUKER_1, g1, g2, g3, g4);

        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak("id4", "FA", "SV", now().minusMonths(1), now().minusMonths(1));
        expectIT(FNR_BRUKER_1, it1, it2);
        expectITRest(FNR_BRUKER_1, it2);

        var w = dataWrapper(AKTØR_BRUKER_1);
        w.setBehandlingTema(SVANGERSKAPSPENGER);
        w.setDokumentTypeId(SØKNAD_SVANGERSKAPSPENGER);

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_finne_relevant_registrert_infotrygdsak_for_inntektsmelding_svangerskapspenger() throws Exception {
        var g1 = new GsakSak(FNR_BRUKER_1, "id1", Tema.UDEFINERT, INFOTRYGD);
        var g2 = new GsakSak(FNR_BRUKER_1, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD);
        var g3 = new GsakSak(FNR_BRUKER_1, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        var g4 = new GsakSak(FNR_BRUKER_1, "id4", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        expectGsak(FNR_BRUKER_1, g1, g2, g3, g4);

        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak("id4", "FA", "SV", now().minusMonths(1), now().minusMonths(1));
        expectIT(FNR_BRUKER_1, it1, it2);
        expectITRest(FNR_BRUKER_1, it2);

        var w = dataWrapper(AKTØR_BRUKER_1);
        w.setBehandlingTema(SVANGERSKAPSPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);

        doAndAssertJournalført(w);
    }

    @Test
    public void skal_finne_relevant_infotrygdsak_for_medmor_foreldrepenger() throws Exception {
        var g1 = new GsakSak(FNR_ANNEN_PART_2, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD);
        expectGsak(FNR_ANNEN_PART_2, g1);

        var it = new InfotrygdSak("id4", "FA", "FØ", null, now().minusMonths(7));
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
        var g1 = new GsakSak(FNR_BRUKER, "id1", FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD,
                now().minusYears(2));
        expectGsak(FNR_BRUKER, g1);
        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2), now().minusYears(2));
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
        when(gsak.finnSaker(any())).thenReturn(gsaker(FNR));
        expectGsaker(FNR_BRUKER, gsaker(FNR));
        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2), now().minusYears(2));
        expectIT(FNR_BRUKER, it1);
        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setAnnenPartId(AKTØR_ANNEN_PART);

        doAndAssertOpprettet(w);
    }

    @Test
    public void neste_steg_skal_være_retry_hvis_nedetid() {

        when(gsak.finnSaker(any())).thenReturn(gsaker(FNR));
        when(infotrygd.finnSakListe(any(), any()))
                .thenThrow(InfotrygdFeil.FACTORY.nedetid("InfotrygdSak", null).toException());

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(FORELDREPENGER_ENDRING_SØKNAD);
        w.setAnnenPartId(AKTØR_ANNEN_PART);
        expect(TekniskException.class, "FP-180124");
        doWithPrecondition(w);
    }

    @Test
    public void neste_steg_skal_throw_exception_hvis_annen_part_er_ikke_funnet() throws Exception {

        expectGsaker(FNR_ANNEN_PART, gsaker(FNR_ANNEN_PART));
        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2), now().minusYears(2));
        expectIT(FNR_ANNEN_PART, it1);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);

        expect(TekniskException.class, "FP-941984");
        doWithPrecondition(w);
    }

    @Test
    public void skal_throw_exception_hvis_ukjent_behandlings_tema() throws Exception {
        expectGsaker(FNR_ANNEN_PART, gsaker(FNR_ANNEN_PART));

        var it1 = new InfotrygdSak("id3", "FA", "FE", now().minusYears(2), now().minusYears(2));
        expectIT(FNR_ANNEN_PART, it1);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(BehandlingTema.UDEFINERT);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());

        expect(VLException.class, "FP-286143");
        doWithPrecondition(w);
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

    private void assertTaskType(MottakMeldingDataWrapper wrapper, String taskname) {
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(taskname);
    }

    private MottakMeldingDataWrapper dataWrapper(String aktørBruker) {
        var w = new MottakMeldingDataWrapper(kodeverkRepository, taskData());
        w.setAktørId(aktørBruker);
        w.setTema(FORELDRE_OG_SVANGERSKAPSPENGER);
        return w;

    }

    private ProsessTaskData taskData() {
        var data = new ProsessTaskData(HentOgVurderInfotrygdSakTask.TASKNAME);
        data.setSekvens("1");
        return data;
    }

    private HentOgVurderInfotrygdSakTask task() {
        return new HentOgVurderInfotrygdSakTask(prosessTaskRepository,
                kodeverkRepository,
                new RelevantSakSjekker(svp, fp, infotrygd, gsak, unleash),
                aktør
        );
    }

    private static List<GsakSak> gsaker(String fnr) {
        GsakSak sak1 = new GsakSak(fnr, "id1", Tema.UDEFINERT, INFOTRYGD);
        GsakSak sak2 = new GsakSak(fnr, "id2", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, INFOTRYGD);
        GsakSak sak3 = new GsakSak(fnr, "id3", Tema.FORELDRE_OG_SVANGERSKAPSPENGER, FPSAK);
        return List.of(sak1, sak2, sak3);
    }

    private MottakMeldingDataWrapper doWithPrecondition(MottakMeldingDataWrapper wrapper) {
        var task = task();
        task.precondition(wrapper);
        return task.doTask(wrapper);
    }

    private MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper wrapper) {
        return task().doTask(wrapper);
    }

    private void expect(Class<? extends Throwable> clazz, String msg) {
        expectedException.expect(clazz);
        expectedException.expectMessage(msg);
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
        when(infotrygd.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
        when(svp.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
        when(fp.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
    }

    private void expectITRest(String fnr, InfotrygdSak... itsaker) {
        when(svp.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
        when(fp.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
    }

    private void expectGsak(String fnr, GsakSak... gsaker) {
        expectGsaker(fnr, Arrays.asList(gsaker));
    }

    private void expectGsaker(String fnr, List<GsakSak> gsaker) {
        when(gsak.finnSaker(fnr)).thenReturn(gsaker);
    }

    private void expect(String aktørId, String fnr) {
        when(aktør.hentPersonIdentForAktørId(eq(aktørId))).thenReturn(Optional.of(fnr));
    }

}