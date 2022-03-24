package no.nav.foreldrepenger.fordel.dbstoette;

import java.io.File;

import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.foreldrepenger.konfig.Environment;

/**
 * Initielt skjemaoppsett + migrering av unittest-skjemaer
 */
public final class Databaseskjemainitialisering {

    private static final Logger LOG = LoggerFactory.getLogger(Databaseskjemainitialisering.class);
    private static final Environment ENV = Environment.current();
    private static final String FLYWAY_SCHEMA_TABLE = "schema_version";

    public static final DBProperties DEFAULT_DS_PROPERTIES = dbProperties("defaultDS", "fpfordel");
    public static final DBProperties DVH_DS_PROPERTIES = dbProperties("defaultDS", "fpfordel_unit");
    public static final String URL_DEFAULT = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcp) (HOST=127.0.0.1)(PORT=1522))(CONNECT_DATA=(SERVICE_NAME=XEPDB1)))";

    public static void main(String[] args) {
        migrer();
    }

    public static void migrer() {
        migrer(DEFAULT_DS_PROPERTIES);
        migrer(DVH_DS_PROPERTIES);
    }

    private static DBProperties dbProperties(String dsName, String schema) {
        return new DBProperties(dsName, schema, ds(dsName, schema), getScriptLocation(dsName));
    }

    public static void settJdniOppslag() {
        try {
            var props = DEFAULT_DS_PROPERTIES;
            new EnvEntry("jdbc/" + props.dsName(), props.dataSource());
        } catch (Exception e) {
            throw new RuntimeException("Feil under registrering av JDNI-entry for default datasource", e);
        }
    }

    private static void migrer(DBProperties dbProperties) {
        LOG.info("Migrerer {}", dbProperties.schema());
        var flyway = Flyway.configure()
                .baselineOnMigrate(true)
                .dataSource(dbProperties.dataSource())
                .table(FLYWAY_SCHEMA_TABLE)
                .locations(dbProperties.scriptLocation)
                .cleanOnValidationError(true)
                .load();
        if (!ENV.isLocal()) {
            throw new IllegalStateException("Forventer at denne migreringen bare kjøres lokalt");
        }
        flyway.migrate();
    }

    private static String getScriptLocation(String dsName) {
        if (Environment.current().getProperty("maven.cmd.line.args") != null) {
            return classpathScriptLocation(dsName);
        }
        return fileScriptLocation(dsName);
    }

    private static String classpathScriptLocation(String dsName) {
        return "classpath:/db/migration/" + dsName;
    }

    private static String fileScriptLocation(String dsName) {
        String relativePath = "fordel-migreringer/src/main/resources/db/migration/" + dsName;
        File baseDir = new File(".").getAbsoluteFile();
        File location = new File(baseDir, relativePath);
        while (!location.exists()) {
            baseDir = baseDir.getParentFile();
            if (baseDir == null || !baseDir.isDirectory()) {
                throw new IllegalArgumentException("Klarte ikke finne : " + baseDir);
            }
            location = new File(baseDir, relativePath);
        }
        return "filesystem:" + location.getPath();
    }

    private static DataSource ds(String dsName, String schema) {
        var ds = new HikariDataSource(hikariConfig(dsName, schema));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> ds.close()));
        return ds;
    }

    private static HikariConfig hikariConfig(String dsName, String schema) {
        var cfg = new HikariConfig();
        cfg.setJdbcUrl(ENV.getProperty(dsName + ".url", URL_DEFAULT));
        cfg.setUsername(ENV.getProperty(dsName + ".username", schema));
        cfg.setPassword(ENV.getProperty(dsName + ".password", schema));
        cfg.setConnectionTimeout(10000);
        cfg.setMinimumIdle(0);
        cfg.setMaximumPoolSize(4);
        cfg.setAutoCommit(false);
        return cfg;
    }

    public static class DBProperties {
        private final String schema;
        private final DataSource dataSource;
        private final String scriptLocation;
        private final String dsName;

        private DBProperties(String dsName, String schema, DataSource dataSource, String scriptLocation) {
            this.dsName = dsName;
            this.schema = schema;
            this.dataSource = dataSource;
            this.scriptLocation = scriptLocation;
        }

        public String dsName() {
            return dsName;
        }

        public String schema() {
            return schema;
        }

        public DataSource dataSource() {
            return dataSource;
        }

        public String scriptLocation() {
            return scriptLocation;
        }
    }
}
