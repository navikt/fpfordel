package no.nav.foreldrepenger.mottak.felles;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepositoryImpl;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class MottakMeldingDataWrapperForInntektsmeldingTest {

    private static final String PROSESSTASK_STEG1 = "prosesstask.steg1";

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    private ProsessTaskData eksisterendeData;

    private MottakMeldingDataWrapper testObjekt;

    @Before
    public void setUp() {
        eksisterendeData = new ProsessTaskData(PROSESSTASK_STEG1);
        eksisterendeData.setSekvens("1");

        testObjekt = new MottakMeldingDataWrapper(kodeverkRepository, eksisterendeData);
    }

    @Test
    public void skal_kunne_sette_inn_startdatoForeldrepengerPeriode_og_hente_ut_igjen() throws Exception {
        final LocalDate actualDate = LocalDate.now();
        testObjekt.setFørsteUttakssdag(actualDate);
        assertThat(testObjekt.getFørsteUttaksdag().get()).isEqualTo(actualDate);
    }

    @Test
    public void skal_kunne_sette_inn_årsakTilInnsending_og_hente_ut_igjen() throws Exception {
        final String actualÅrsak = "Endring";
        testObjekt.setÅrsakTilInnsending(actualÅrsak);
        assertThat(testObjekt.getÅrsakTilInnsending().get()).isEqualTo(actualÅrsak);
    }

}