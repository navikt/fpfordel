package no.nav.foreldrepenger.mottak.felles;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class MottakMeldingDataWrapperForInntektsmeldingTest {

    private static final String PROSESSTASK_STEG1 = "prosesstask.steg1";

    private ProsessTaskData eksisterendeData;

    private MottakMeldingDataWrapper testObjekt;

    @BeforeEach
    public void setUp() {
        eksisterendeData = new ProsessTaskData(PROSESSTASK_STEG1);
        eksisterendeData.setSekvens("1");
        testObjekt = new MottakMeldingDataWrapper(eksisterendeData);
    }

    @Test
    public void skal_kunne_sette_inn_startdatoForeldrepengerPeriode_og_hente_ut_igjen() {
        final LocalDate actualDate = LocalDate.now();
        testObjekt.setFørsteUttakssdag(actualDate);
        assertThat(testObjekt.getFørsteUttaksdag().get()).isEqualTo(actualDate);
    }

    @Test
    public void skal_kunne_sette_inn_årsakTilInnsending_og_hente_ut_igjen() {
        final String actualÅrsak = "Endring";
        testObjekt.setÅrsakTilInnsending(actualÅrsak);
        assertThat(testObjekt.getÅrsakTilInnsending().get()).isEqualTo(actualÅrsak);
    }

}