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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.RelevantSakSjekker;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.tjeneste.VurderInfotrygd;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class HentOgVurderInfotrygdSakTaskTest {

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
    private PersonInformasjon aktør;
    @Mock
    private InfotrygdTjeneste fp;

    @BeforeEach
    public void setup() {
        expectAktørFnrMappings();
    }

    @Test
    void skal_finne_relevant_registrert_infotrygdsak_for_inntektsmelding() {
        var it1 = new InfotrygdSak(now().minusYears(2),
                now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusMonths(1), now().minusMonths(1));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now());

        doAndAssertManuell(w);
    }

    @Test
    void skal_finne_relevant_iverksatt_infotrygdsak_for_inntektsmelding_bruker1() {
        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusDays(1), now().minusDays(1));
        expectIT(FNR_BRUKER_1, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER_1);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now());

        doAndAssertAutomatisert(w); // Kvinne - skal til VL, ignorer infotrygd
    }

    @Test
    void skal_finne_relevant_iverksatt_infotrygdsak_for_inntektsmelding_bruker() {
        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusDays(1), now().minusDays(1));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now());

        doAndAssertManuell(w);
    }

    @Test
    void skal_finne_relevant_infotrygdsak_for_inntektsmelding_avsluttet_vedtak() {
        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusDays(50), now().minusDays(50));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now().minusDays(51));

        doAndAssertManuell(w);
    }

    @Test
    void skal_ikke_lenger_sjekke_infotrygdsak_for_engangsstønad() {
        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusDays(1), now().minusDays(1));
        expectIT(FNR_BRUKER, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(ENGANGSSTØNAD_FØDSEL);
        w.setDokumentTypeId(SØKNAD_ENGANGSSTØNAD_FØDSEL);
        w.setInntekstmeldingStartdato(now());

        doAndAssertAutomatisert(w);
    }

    @Test
    void skal_finne_relevant_infotrygdsak_for_foreldrepenger() {
        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        var it2 = new InfotrygdSak(now().minusDays(1), now().minusDays(1));
        expectIT(FNR_ANNEN_PART, it1, it2);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        w.setAnnenPartId(AKTØR_ANNEN_PART);

        doAndAssertManuell(w);
    }

    @Test
    void skal_opprette_sak_når_ingen_sak_for_foreldrepenger() {

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        w.setAnnenPartId(AKTØR_ANNEN_PART);
        doAndAssertAutomatisert(w);
    }

    @Test
    void skal_opprette_sak_når_ingen_sak_for_svangerskapspenger() {

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(SVANGERSKAPSPENGER);
        w.setDokumentTypeId(SØKNAD_SVANGERSKAPSPENGER);

        doAndAssertAutomatisert(w);
    }

    @Test
    void skal_finne_relevant_infotrygdsak_for_medmor_foreldrepenger() {

        var it = new InfotrygdSak(null, now().minusMonths(7));
        expectIT(FNR_ANNEN_PART_2, it);

        var w = dataWrapper(AKTØR_BRUKER_2);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        w.setAnnenPartId(AKTØR_ANNEN_PART_2);

        doAndAssertManuell(w);
    }

    @Test
    void skal_ikke_sjekke_infotrygdsak_for_fedre_foreldrepenger() {

        var w = dataWrapper(AKTØR_BRUKER_1);
        w.setBehandlingTema(FORELDREPENGER_FØDSEL);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        w.setAnnenPartId(AKTØR_ANNEN_PART_1);

        doAndAssertAutomatisert(w);
    }

    @Test
    void skal_sjekke_infotrygd_kun_for_bruker_ved_inntektsmelding() {

        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        expectIT(FNR_BRUKER, it1);
        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(INNTEKTSMELDING);
        w.setInntekstmeldingStartdato(now());

        ArgumentCaptor<String> aktørCaptor = ArgumentCaptor.forClass(String.class);
        doAndAssertAutomatisert(w);

        verify(aktør).hentPersonIdentForAktørId(aktørCaptor.capture());
        assertThat(aktørCaptor.getAllValues()).contains(AKTØR_BRUKER);
    }

    @Test
    void neste_steg_skal_være_opprettsak_hvis_relevant_infotrygdsak_ikke_finnes() {

        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        expectIT(FNR_BRUKER, it1);
        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(FORELDREPENGER);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setAnnenPartId(AKTØR_ANNEN_PART);

        doAndAssertAutomatisert(w);
    }

    @Test
    void skal_throw_exception_hvis_ukjent_behandlings_tema() {

        var it1 = new InfotrygdSak(now().minusYears(2), now().minusYears(2));
        expectIT(FNR_ANNEN_PART, it1);

        var w = dataWrapper(AKTØR_BRUKER);
        w.setBehandlingTema(BehandlingTema.UDEFINERT);
        w.setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL);
        w.setInntekstmeldingStartdato(now());
        assertThrows(VLException.class, () -> kreverManuellVurdering(w));
    }

    private void doAndAssertAutomatisert(MottakMeldingDataWrapper w) {
        doAndAssert(w, false);
    }

    private void doAndAssertManuell(MottakMeldingDataWrapper w) {
        doAndAssert(w, true);
    }

    private void doAndAssert(MottakMeldingDataWrapper w, boolean manuellVurdering) {
        assertThat(kreverManuellVurdering(w)).isEqualTo(manuellVurdering);
    }

    private static MottakMeldingDataWrapper dataWrapper(String aktørBruker) {
        var w = new MottakMeldingDataWrapper(taskData());
        w.setAktørId(aktørBruker);
        w.setTema(FORELDRE_OG_SVANGERSKAPSPENGER);
        return w;

    }

    private static ProsessTaskData taskData() {
        var data = new ProsessTaskData("DUMMY");
        data.setSekvens("1");
        return data;
    }

    private boolean kreverManuellVurdering(MottakMeldingDataWrapper wrapper) {
        var vurderInfotrygd = new VurderInfotrygd(new RelevantSakSjekker(fp), aktør);
        return vurderInfotrygd.kreverManuellVurdering(wrapper);
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
        when(fp.finnSakListe(eq(fnr), any())).thenReturn(List.of(itsaker));
    }

    private void expect(String aktørId, String fnr) {
        when(aktør.hentPersonIdentForAktørId(eq(aktørId))).thenReturn(Optional.of(fnr));
    }

}