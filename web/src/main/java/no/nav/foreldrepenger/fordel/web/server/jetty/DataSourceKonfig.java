package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.vedtak.util.env.Environment;

class DataSourceKonfig {

    private static final String MIGRATIONS_LOCATION = "classpath:/db/migration/";
    private final DBConnProp defaultDatasource;
    private final List<DBConnProp> dataSources;
    private static final Environment ENV = Environment.current();

    DataSourceKonfig() {
        defaultDatasource = new DBConnProp(createDatasource("defaultDS"), MIGRATIONS_LOCATION + "defaultDS");
        dataSources = List.of(defaultDatasource);
    }

    private static DataSource createDatasource(String dataSourceName) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(ENV.getRequiredProperty(dataSourceName + ".url"));
        config.setUsername(ENV.getRequiredProperty(dataSourceName + ".username"));
        config.setPassword(ENV.getRequiredProperty(dataSourceName + ".password"));
        config.setConnectionTimeout(1000);
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(30);
        config.setConnectionTestQuery("select 1 from dual");
        config.setDriverClassName("oracle.jdbc.OracleDriver");

        Properties dsProperties = new Properties();
        config.setDataSourceProperties(dsProperties);

        return new HikariDataSource(config);
    }

    DBConnProp getDefaultDatasource() {
        return defaultDatasource;
    }

    List<DBConnProp> getDataSources() {
        return dataSources;
    }

    static final class DBConnProp {
        private final DataSource datasource;
        private final String migrationScripts;

        public DBConnProp(DataSource datasource, String migrationScripts) {
            this.datasource = datasource;
            this.migrationScripts = migrationScripts;
        }

        public DataSource getDatasource() {
            return datasource;
        }

        public String getMigrationScripts() {
            return migrationScripts;
        }
    }

}
