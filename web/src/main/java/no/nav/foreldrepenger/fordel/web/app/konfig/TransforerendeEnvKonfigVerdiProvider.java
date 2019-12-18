package no.nav.foreldrepenger.fordel.web.app.konfig;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.konfig.EnvPropertiesKonfigVerdiProvider;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class TransforerendeEnvKonfigVerdiProvider extends EnvPropertiesKonfigVerdiProvider {

    @Override
    public int getPrioritet() {
        return 20;
    }

    @Override
    public <V> V getVerdi(String key, KonfigVerdi.Converter<V> converter) {
        V verdi = super.getVerdi(key, converter);
        if (verdi == null) {
            verdi = super.getVerdi(upperKey(key), converter);
        }
        if (verdi == null) {
            verdi = super.getVerdi(endpointUrlKey(key), converter);
        }
        return verdi;
    }

    @Override
    public boolean harVerdi(String key) {
        return super.harVerdi(key) || super.harVerdi(upperKey(key)) || super.harVerdi(endpointUrlKey(key));
    }

    private String endpointUrlKey(String key) {
        // hack diff mellom NAIS og SKYA env for endepunkter
        return key == null ? null : upperKey(key).replaceAll("_URL$", "_ENDPOINTURL");
    }

    private String upperKey(String key) {
        // hack diff mellom NAIS og SKYA env (upper vs. lower case og '_' istdf. '.')
        return key == null ? null : key.toUpperCase().replace('.', '_');
    }
}
