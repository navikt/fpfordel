package no.nav.foreldrepenger.fordel.dbstoette;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.testutilities.db.DbMigreringFeil;

public class FlywayKonfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayKonfig.class);

    private DataSource ds;
    private boolean cleanup;
    private String sqlLokasjon;
    private String tabellnavn;

    private String username;

    public FlywayKonfig(DataSource ds) {
        this.ds = ds;
    }

    public String getUsername() {
        return username;
    }

    public FlywayKonfig medUsername(String username) {
        this.username = username;
        return this;
    }

    FlywayKonfig medCleanup(boolean utførFullMigrering) {
        this.cleanup = utførFullMigrering;
        return this;
    }

    FlywayKonfig medSqlLokasjon(String sqlLokasjon) {
        this.sqlLokasjon = sqlLokasjon;
        return this;
    }

    FlywayKonfig medMetadataTabell(String tabellnavn) {
        this.tabellnavn = tabellnavn;
        return this;
    }

    boolean migrerDb() {
        Flyway flyway = new Flyway();
        flyway.setBaselineOnMigrate(true);
        flyway.setDataSource(ds);

        if (tabellnavn != null) {
            flyway.setTable(tabellnavn);
        }

        if (sqlLokasjon != null) {
            flyway.setLocations(sqlLokasjon);
        } else {
            /**
             * Default leter flyway etter classpath:db/migration. Her vet vi at vi ikke skal
             * lete i classpath
             */
            flyway.setLocations("denne/stien/finnes/ikke");
        }

        if (cleanup) {
            if (isPostgres(ds)) {
                clean(ds, username);
            } else {
                flyway.clean();
            }
        }

        try {
            flyway.migrate();
            return true;
        } catch (FlywayException flywayException) {
            FeilFactory.create(DbMigreringFeil.class).flywayMigreringFeilet(flywayException).log(LOGGER);
            return false;
        }
    }

    private boolean isPostgres(DataSource dataSource) {
        try (var conn = dataSource.getConnection()) {
            String databaseProductName = conn.getMetaData().getDatabaseProductName();
            return "PostgreSQL".equalsIgnoreCase(databaseProductName);
        } catch (SQLException e) {
            throw FeilFactory.create(DbMigreringFeil.class).kanIkkeDetektereDatbaseType(e).toException();
        }
    }

    /**
     * For å postgres - unngår issue med manglende support i flyway.
     */
    private void clean(DataSource dataSource, String username) {
        try (Connection c = dataSource.getConnection();
                Statement stmt = c.createStatement()) {
            stmt.execute("drop owned by \"" + username.replaceAll("[^a-zA-Z0-9_-]", "_") + "\""); // NOSONAR ok her, test konfig
        } catch (SQLException e) {
            throw new IllegalStateException("Kunne ikke kjøre clean på db", e);
        }
    }

}