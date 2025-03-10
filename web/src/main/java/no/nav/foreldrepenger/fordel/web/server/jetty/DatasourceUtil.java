package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.micrometer.core.instrument.Metrics;
import no.nav.foreldrepenger.konfig.Environment;

class DatasourceUtil {

    private DatasourceUtil() {
    }

    private static final Environment ENV = Environment.current();

    static DataSource createDatasource(int maxPoolSize, int minIdle) {
        var config = new HikariConfig();
        config.setJdbcUrl(ENV.getRequiredProperty("defaultDS.url"));
        config.setUsername(ENV.getRequiredProperty("defaultDS.username"));
        config.setPassword(ENV.getRequiredProperty("defaultDS.password"));
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
}
