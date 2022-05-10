package no.nav.foreldrepenger.mottak.felles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

class MottakMeldingDataWrapperForInntektsmeldingTest {

    private static final TaskType PROSESSTASK_STEG1 = new TaskType("prosesstask.steg1");

    private ProsessTaskData eksisterendeData;

    private MottakMeldingDataWrapper testObjekt;

    @BeforeEach
    void setUp() {
        eksisterendeData = ProsessTaskData.forTaskType(PROSESSTASK_STEG1);
        testObjekt = new MottakMeldingDataWrapper(eksisterendeData);
    }

    @Test
    void skal_kunne_sette_inn_startdatoForeldrepengerPeriode_og_hente_ut_igjen() {
        final LocalDate actualDate = LocalDate.now();
        testObjekt.setFørsteUttakssdag(actualDate);
        assertThat(testObjekt.getFørsteUttaksdag()).contains(actualDate);
    }

    @Test
    void skal_kunne_sette_inn_årsakTilInnsending_og_hente_ut_igjen() {
        final String actualÅrsak = "Endring";
        testObjekt.setÅrsakTilInnsending(actualÅrsak);
        assertThat(testObjekt.getÅrsakTilInnsending()).contains(actualÅrsak);
    }

}