package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.micrometer.core.instrument.Metrics;
import no.nav.foreldrepenger.konfig.Environment;

import static no.nav.foreldrepenger.fordel.web.server.jetty.VaultUtil.lesFilVerdi;

class DatasourceUtil {

    private DatasourceUtil() {
    }

    private static final Environment ENV = Environment.current();

    static DataSource createDatasource(int maxPoolSize, int minIdle) {
        var config = new HikariConfig();
        config.setJdbcUrl(hentEllerBeregnVerdiHvisMangler("defaultDS.url", "defaultDSconfig", "jdbc_url"));
        config.setUsername(hentEllerBeregnVerdiHvisMangler("defaultDS.username", "defaultDS", "username"));
        config.setPassword(hentEllerBeregnVerdiHvisMangler("defaultDS.password", "defaultDS", "password"));
        config.setConnectionTimeout(TimeUnit.SECONDS.toMillis(2));
        config.setMinimumIdle(minIdle);
        config.setMaximumPoolSize(maxPoolSize);
        config.setConnectionTestQuery("select 1 from dual");
        config.setDriverClassName("oracle.jdbc.OracleDriver");
        config.setMetricRegistry(Metrics.globalRegistry);

        var dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        return new HikariDataSource(config);
    }

    /* Denne gir lazy loading og feiler ikke ved lokalt kjøring uten vault mount */
    private static String hentEllerBeregnVerdiHvisMangler(String key, String mappeNavn, String filNavn) {
        if (ENV.getProperty(key) == null) {
            System.getProperties().computeIfAbsent(key, _ -> VaultUtil.lesFilVerdi(mappeNavn, filNavn));
        }
        return ENV.getRequiredProperty(key);
    }
}
