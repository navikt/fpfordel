package no.nav.foreldrepenger.fordel.konfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import javax.inject.Inject;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.KonfigVerdiGruppe;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class KonfigVerdiRepositoryTest {

    @Rule
    public final UnittestRepositoryRule repoRule =new UnittestRepositoryRule();
    
    @Inject
    private KonfigVerdiRepository konfigVerdiRepository;
    
    @Test
    public void skal_kjøre_kall_mot_konfig_verdi() throws Exception {
        var list = konfigVerdiRepository.finnAlleVerdier(LocalDate.now());
        assertThat(list).isNotEmpty();
        System.out.println(list);
    }
    
    @Test
    public void skal_hente_enkel_konfig_uten_gruppe() throws Exception {
        var list = konfigVerdiRepository.finnVerdierFor(KonfigVerdiGruppe.INGEN_GRUPPE, LocalDate.now());
        assertThat(list).isNotEmpty();
    }
    
    @Test
    public void skal_hente_enkel_konfig() throws Exception {
        var verdi = konfigVerdiRepository.finnVerdiFor(KonfigVerdiGruppe.INGEN_GRUPPE, "infotrygd.sak.gyldig.periode", LocalDate.now());
        assertThat(verdi).isPresent();
    }
    
}
;