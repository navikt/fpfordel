package no.nav.foreldrepenger.mottak.felles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class MottakMeldingDataWrapperTest {

    private static final String PROSESSTASK_STEG1 = "prosesstask.steg1";
    private static final String PROSESSTASK_STEG2 = "prosesstask.steg2";
    private static final String PROSESSTASK_STEG3 = "prosesstask.steg3";

    private ProsessTaskData eksisterendeData;
    private MottakMeldingDataWrapper wrapper;

    @BeforeEach
    public void setUp() {
        eksisterendeData = new ProsessTaskData(PROSESSTASK_STEG1);
        eksisterendeData.setSekvens("1");
        wrapper = new MottakMeldingDataWrapper(eksisterendeData);
    }

    @Test
    public void test_kan_opprette_og_kopiere_wrapper_uten_eksisterende_properties() {
        assertThat(wrapper.hentAlleProsessTaskVerdier()).as("Forventer at wrapper i utgangspunktet blir opprettet uten properties").isEmpty();

        MottakMeldingDataWrapper wrapperNesteSteg = wrapper.nesteSteg(PROSESSTASK_STEG2);
        assertThat(wrapper.hentAlleProsessTaskVerdier()).as("").isEqualTo(wrapperNesteSteg.hentAlleProsessTaskVerdier());
    }

    @Test
    public void test_beholder_properties_og_payload_fra_forrige_steg() {
        wrapper.setArkivId("arkiv_1234");
        wrapper.setPayload("<xml>test</xml>");

        MottakMeldingDataWrapper wrapperNesteSteg = wrapper.nesteSteg(PROSESSTASK_STEG2);
        assertThat(wrapperNesteSteg.getArkivId()).as("Forventer at arkivId blir med til neste steg.").isEqualTo(wrapper.getArkivId());
        assertThat(wrapperNesteSteg.getProsessTaskData().getPayload()).as("Forventer at payload også blir kopiert over").isNotNull();
    }

    @Test
    public void test_overskriv_eksisterende_property() {
        final String INITIELL_ARKIVID = "arkiv_1234";
        final String NY_ARKIVID = "nyid_987";
        wrapper.setArkivId(INITIELL_ARKIVID);

        MottakMeldingDataWrapper wrapperNesteSteg = wrapper.nesteSteg(PROSESSTASK_STEG2);
        wrapperNesteSteg.setArkivId(NY_ARKIVID);

        assertThat(wrapperNesteSteg.getArkivId()).as("Forventer at arkivId blir overskrevet med nyTerminbekreftelse verdi.").isEqualTo(NY_ARKIVID);
    }

    @Test
    public void test_property_fra_flere_steg_blir_kopiert() {
        final String ARKIVID = "arkiv_1234";
        final Tema TEMA = Tema.FORELDRE_OG_SVANGERSKAPSPENGER;

        wrapper.setArkivId(ARKIVID);
        assertThat(wrapper.getArkivId()).as("Forventer at wrapper inneholder properties fra input dataklasse.").isEqualTo(ARKIVID);

        MottakMeldingDataWrapper wrapperAndreSteg = wrapper.nesteSteg(PROSESSTASK_STEG2);
        wrapperAndreSteg.setTema(TEMA);

        MottakMeldingDataWrapper wrapperTredjeSteg = wrapperAndreSteg.nesteSteg(PROSESSTASK_STEG3);

        assertThat(wrapperTredjeSteg.getArkivId()).as("Forventer at alle properties har blitt med hele veien gjennom flyten.").isEqualTo(ARKIVID);
        assertThat(wrapperTredjeSteg.getTema()).as("Forventer at alle properties har blitt med hele veien gjennom flyten.").isEqualTo(TEMA);
    }

    @Test
    public void test_skal_kunne_sette_behandlingstemakode_og_hente_ut_igjen() {
        wrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        assertThat(wrapper.getBehandlingTema().getKode()).isEqualTo(BehandlingTema.ENGANGSSTØNAD_FØDSEL.getKode());
    }

    @Test
    public void test_skal_kunne_sette_dokumenttypeid_og_hente_ut_igjen() {
        wrapper.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        assertThat(wrapper.getDokumentTypeId())
                .hasValueSatisfying(s -> assertThat(s.getKode()).isEqualTo(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getKode()));
    }

    @Test
    public void test_skal_kunne_sette_aktørid_og_hente_ut_igjen() {
        final String aktørId = "1";
        wrapper.setAktørId(aktørId);

        assertThat(wrapper.getAktørId()).hasValueSatisfying(it -> assertThat(it).isEqualTo(aktørId));
    }

    @Test
    public void skal_få_samme_dato_tilbake() {
        LocalDate now = LocalDate.now();
        LocalDate dato1 = now.minusDays(1);
        LocalDate dato2 = now.minusMonths(1);
        LocalDate dato3 = now.plusDays(1);

        wrapper.setAdopsjonsbarnFodselsdatoer(Arrays.asList(now, dato1, dato2));
        wrapper.setBarnFodselsdato(dato1);
        wrapper.setBarnTermindato(dato3);
        wrapper.setOmsorgsovertakelsedato(dato2);

        assertThat(wrapper.getAdopsjonsbarnFodselsdatoer()).isEqualTo(Arrays.asList(now, dato1, dato2));
        assertThat(wrapper.getBarnTermindato()).hasValueSatisfying(s -> assertThat(s).isEqualTo(dato3));
        assertThat(wrapper.getOmsorgsovertakelsedato()).hasValueSatisfying(s -> assertThat(s).isEqualTo(dato2));
        assertThat(wrapper.getBarnFodselsdato()).hasValueSatisfying(s -> assertThat(s).isEqualTo(dato1));
    }

    @Test
    public void test_skal_gi_prosess_task_id() {

        eksisterendeData.setId(1377L);
        MottakMeldingDataWrapper wrapper = new MottakMeldingDataWrapper(eksisterendeData);

        assertThat(wrapper.getId()).isEqualTo(1377L);
    }

    @Test
    public void test_skal_gi_null_prosess_task_id_for_log() {

        eksisterendeData.setId(null);
        MottakMeldingDataWrapper wrapper = new MottakMeldingDataWrapper(eksisterendeData);

        assertThat(wrapper.getId()).isNull();
    }

    @Test
    public void skal_sette_og_hente_er_strukturert_dokument() {
        assertThat(wrapper.erStrukturertDokument()).isNotPresent();
        wrapper.setStrukturertDokument(true);
        assertThat(wrapper.erStrukturertDokument()).hasValue(true);
    }

    @Test
    public void skal_kunne_sette_inn_strukturertDokument_og_hente_ut_igjen() {
        wrapper.setStrukturertDokument(true);
        assertThat(wrapper.erStrukturertDokument()).isPresent();
        assertThat(wrapper.erStrukturertDokument().get()).isTrue();
    }

    @Test
    public void skal_kunne_sette_inn_terminbekreftelseDato_og_hente_ut_igjen() {
        LocalDate dato = LocalDate.now();
        wrapper.setBarnTerminbekreftelsedato(dato);
        assertThat(wrapper.getBarnTerminbekreftelsedato()).hasValue(dato);
    }

    @Test
    public void skal_kunne_sette_antall_barn_og_hente_ut_igjen() {
        wrapper.setAntallBarn(2);
        assertThat(wrapper.getAntallBarn()).hasValue(2);
    }

    @Test
    public void skal_returnere_tom_optional_når_inntektsmelding_startdato_ikke_er_satt() {
        Optional<LocalDate> result = wrapper.getInntektsmeldingStartDato();
        assertThat(result).isEmpty();
    }

    @Test
    public void skal_kaste_illegalstate_hvis_inntektsmelding_startdato_inneholder_flere_datoer() {
        eksisterendeData.setProperty(MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY, "1234;1234");
        MottakMeldingDataWrapper wrapper = new MottakMeldingDataWrapper(eksisterendeData);
        var e = assertThrows(IllegalStateException.class, () -> wrapper.getInntektsmeldingStartDato());
        assertTrue(e.getMessage().contains("Inneholder flere startdatoer"));
    }
}
