package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.micrometer.core.instrument.Metrics;
import no.nav.foreldrepenger.konfig.Environment;

public class DatasourceUtil {
    private static final Environment ENV = Environment.current();

    public static DataSource createDatasource(int maxPoolSize) {
        var config = new HikariConfig();
        config.setJdbcUrl(ENV.getRequiredProperty("defaultDS.url"));
        config.setUsername(ENV.getRequiredProperty("defaultDS.username"));
        config.setPassword(ENV.getRequiredProperty("defaultDS.password"));
        config.setConnectionTimeout(1000);
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(maxPoolSize);
        config.setConnectionTestQuery("select 1 from dual");
        config.setDriverClassName("oracle.jdbc.OracleDriver");
        config.setMetricRegistry(Metrics.globalRegistry);

        var dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        return new HikariDataSource(config);
    }
}
