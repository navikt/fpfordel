package no.nav.foreldrepenger.fordel.konfig;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.KonfigVerdiGruppe;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.konfig.KonfigVerdi.Converter;
import no.nav.vedtak.konfig.KonfigVerdiProvider;

@ApplicationScoped
public class DatabaseKonfigVerdiProvider implements KonfigVerdiProvider {

    private KonfigVerdiRepository konfigVerdiRepository;

    DatabaseKonfigVerdiProvider() {
        // for CDI proxying
    }

    @Inject
    public DatabaseKonfigVerdiProvider(KonfigVerdiRepository konfigVerdiRepository) {
        this.konfigVerdiRepository = konfigVerdiRepository;
    }

    @Override
    public <V> V getVerdi(String key, KonfigVerdi.Converter<V> converter) {
        return converter.tilVerdi(getVerdi(key));
    }

    private String getVerdi(String key) {
        Optional<KonfigVerdiEntitet> verdi = getVerdiInternal(key);
        if (verdi.isPresent()) {
            return verdi.get().getVerdi();
        } else {
            return null;
        }
    }

    private Optional<KonfigVerdiEntitet> getVerdiInternal(String key) {
        Optional<KonfigVerdiEntitet> verdi = konfigVerdiRepository.finnVerdiFor(KonfigVerdiGruppe.INGEN_GRUPPE, key, LocalDate.now());
        return verdi;
    }

    /**
     * Antar alle verdier ligger på en gruppe.
     */
    @Override
    public <V> List<V> getVerdier(String key, Converter<V> converter) {
        List<KonfigVerdiEntitet> list = konfigVerdiRepository.finnVerdierFor(KonfigVerdiGruppe.forGruppe(key), LocalDate.now());
        return list.stream().map(v -> converter.tilVerdi(v.getVerdi())).collect(Collectors.toList());
    }

    /**
     * Antar alle verdier ligger på en gruppe.
     */
    @Override
    public <V> Map<String, V> getVerdierAsMap(String key, Converter<V> converter) {
        List<KonfigVerdiEntitet> list = konfigVerdiRepository.finnVerdierFor(KonfigVerdiGruppe.forGruppe(key), LocalDate.now());
        Map<String, V> map = list.stream()
                .collect(Collectors.toMap(KonfigVerdiEntitet::getKode, kve -> converter.tilVerdi(kve.getVerdi())));

        return map;

    }

    @Override
    public boolean harVerdi(String key) {
        return getVerdiInternal(key).isPresent();
    }

    @Override
    public int getPrioritet() {
        return 100; // lavere enn system properties, unittest.properties
    }

}
