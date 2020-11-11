package no.nav.foreldrepenger.fordel.dbstoette;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import no.nav.vedtak.util.env.Environment;

/**
 * Initielt skjemaoppsett + migrering av unittest-skjemaer
 */
public final class Databaseskjemainitialisering {

    private static final Environment ENV = Environment.current();

    public static final List<DBProperties> UNIT_TEST = List.of(cfg("fpfordel.unit"));

    public static final List<DBProperties> DBA = List.of(cfg("fpfordel.dba"), cfg("fpfordel.default"));

    private static final Logger LOG = LoggerFactory.getLogger(Databaseskjemainitialisering.class);

    public static void main(String[] args) {
        migrer();
        migrerUnit();
    }

    public static void migrer() {
        try {
            migrer(DBA);
        } catch (Exception e) {
            throw new RuntimeException("Feil under migrering av lokale skjema", e);
        }
    }

    public static void migrerUnit() {
        try {
            migrer(UNIT_TEST);
        } catch (Exception e) {
            throw new RuntimeException("Feil under migrering av enhetstest-skjema", e);
        }
    }

    public static void settJdniOppslag() {
        try {
            var props = defaultProperties();
            new EnvEntry("jdbc/" + props.getDatasource(), ds(props));
        } catch (Exception e) {
            throw new RuntimeException("Feil under registrering av JDNI-entry for default datasource", e);
        }
    }

    public static void settJdniOppslagUnitTest() {
        try {
            var props = defaultPropertiesUnitTest();
            new EnvEntry("jdbc/" + props.getDatasource(), ds(props));
        } catch (Exception e) {
            throw new RuntimeException("Feil under registrering av JDNI-entry for default datasource", e);
        }
    }

    public static DBProperties defaultPropertiesUnitTest() {
        return UNIT_TEST.stream()
                .filter(DBProperties::isDefaultDataSource)
                .findFirst()
                .orElseThrow();
    }

    public static DBProperties defaultProperties() {
        return DBA.stream()
                .filter(DBProperties::isDefaultDataSource)
                .findFirst()
                .orElseThrow();
    }

    private static void migrer(List<DBProperties> props) {
        props.forEach(p -> migrer(ds(p), p));
    }

    private static void migrer(DataSource ds, DBProperties props) {
        var cfg = new FlywayKonfig(ds);
        if (!cfg
                .medUsername(props.getUser())
                .medSqlLokasjon(scriptLocation(props))
                .medCleanup(props.isMigrateClean())
                .medMetadataTabell(props.getVersjonstabell())
                .migrerDb()) {
            LOG.warn(
                    "Kunne ikke starte inkrementell oppdatering av databasen. Det finnes trolig endringer i allerede kjørte script.\nKjører full migrering...");
            if (!cfg.medCleanup(true).migrerDb()) {
                throw new IllegalStateException("\n\nFeil i script. Avslutter...");
            }
        }
    }

    private static DBProperties cfg(String prefix) {
        String schema = ENV.getRequiredProperty(prefix + ".schema");
        return new DBProperties.Builder()
                .user(schema)
                .versjonstabell("schema_version")
                .password(schema)
                .datasource(ENV.getRequiredProperty(prefix + ".datasource"))
                .schema(schema)
                .defaultSchema(ENV.getProperty(prefix + ".defaultschema", schema))
                .defaultDataSource(ENV.getProperty(prefix + ".default", boolean.class, false))
                .migrateClean(ENV.getProperty(prefix + ".migrateclean", boolean.class, true))
                .url(ENV.getRequiredProperty(prefix + ".url"))
                .migrationScriptsFilesystemRoot(ENV.getRequiredProperty(prefix + ".ms")).build();
    }

    private static String scriptLocation(DBProperties props) {
        return Optional.ofNullable(props.getMigrationScriptsClasspathRoot())
                .map(p -> "classpath:/" + p + "/" + props.getSchema())
                .orElse(fileScriptLocation(props));
    }

    private static String fileScriptLocation(DBProperties props) {
        String relativePath = props.getMigrationScriptsFilesystemRoot() + props.getDatasource();
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

    public static DataSource ds(DBProperties props) {
        var ds = new HikariDataSource(hikariConfig(props));
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                ds.close();
            }
        }));
        return ds;
    }

    private static HikariConfig hikariConfig(DBProperties props) {
        var cfg = new HikariConfig();
        cfg.setJdbcUrl(props.getUrl());
        cfg.setUsername(props.getUser());
        cfg.setPassword(props.getPassword());
        cfg.setConnectionTimeout(1000);
        cfg.setMinimumIdle(0);
        cfg.setMaximumPoolSize(4);
        cfg.setAutoCommit(false);
        return cfg;
    }
}
