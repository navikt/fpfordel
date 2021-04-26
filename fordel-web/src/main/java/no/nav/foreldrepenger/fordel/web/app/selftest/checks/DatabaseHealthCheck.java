package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.felles.LivenessAware;
import no.nav.foreldrepenger.mottak.felles.ReadinessAware;

@ApplicationScoped
public class DatabaseHealthCheck implements ReadinessAware, LivenessAware {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHealthCheck.class);

    private final DataSource dataSource;

    private static final String SQL_QUERY = "select sysdate from DUAL";
    // må være rask, og bruke et stabilt tabell-navn

    public DatabaseHealthCheck() {
        dataSource = (DataSource) new InitialContext().lookup(JDBC_DEFAULT_DS);

    }

    private boolean isOK() {
        LOG.trace("Datasource er {}", dataSource.getClass().getName());
        try (var connection = dataSource.getConnection()) {
            var statement = connection.createStatement();
            if (!statement.execute(SQL_QUERY)) {
                LOG.warn("Feil ved SQL-spørring {} mot databasen", SQL_QUERY);
                return false;
            }
        } catch (SQLException e) {
            LOG.warn("Feil ved SQL-spørring {} mot databasen", SQL_QUERY);
            return false;
        }

        return true;
    }

    @Override
    public boolean isReady() {
        return isOK();
    }

    @Override
    public boolean isAlive() {
        return isOK();
    }
}
