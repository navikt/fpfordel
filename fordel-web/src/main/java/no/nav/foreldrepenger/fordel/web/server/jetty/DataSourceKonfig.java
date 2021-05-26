package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.micrometer.core.instrument.Metrics;
import no.nav.foreldrepenger.konfig.Environment;

class DataSourceKonfig {

    private static final Environment ENV = Environment.current();

    private final DBConnProp defaultDS;
    private final List<DBConnProp> dataSources;

    DataSourceKonfig() {
        var defaultDSName = "defaultDS";
        this.defaultDS = new DBConnProp(ds(defaultDSName), defaultDSName);
        dataSources = List.of(defaultDS);
    }

    private static DataSource ds(String dataSourceName) {
        var config = new HikariConfig();
        config.setJdbcUrl(ENV.getProperty(dataSourceName + ".url"));
        config.setUsername(ENV.getProperty(dataSourceName + ".username"));
        config.setPassword(ENV.getProperty(dataSourceName + ".password"));
        config.setMetricRegistry(Metrics.globalRegistry);
        config.setConnectionTimeout(1000);
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(30);
        config.setConnectionTestQuery("select 1 from dual");
        config.setDriverClassName("oracle.jdbc.OracleDriver");

        Properties dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        return new HikariDataSource(config);
    }

    List<DBConnProp> getDataSources() {
        return dataSources;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [defaultDS=" + defaultDS + ", dataSources=" + dataSources + "]";
    }

    public DataSource defaultDS() {
        return defaultDS.getDatasource();
    }
}
