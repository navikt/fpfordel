package no.nav.foreldrepenger.fordel.dbstoette;

import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.log.metrics.LivenessAware;
import no.nav.vedtak.log.metrics.ReadinessAware;

@ApplicationScoped
public class DatabaseHealthCheck implements ReadinessAware, LivenessAware {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHealthCheck.class);

    private final DataSource dataSource;
    private static final String JDBC_DEFAULT_DS = "jdbc/defaultDS";

    private static final String SQL_QUERY = "select 1 from DUAL";
    // må være rask, og bruke et stabilt tabell-navn

    public DatabaseHealthCheck() throws NamingException {
        dataSource = (DataSource) new InitialContext().lookup(JDBC_DEFAULT_DS);
        LOG.trace("Datasource er {}", dataSource);
    }

    private boolean isOK() {
        try (var connection = dataSource.getConnection()) {
            try (var statement = connection.createStatement()) {
                if (!statement.execute(SQL_QUERY)) {
                    LOG.warn("Feil ved SQL-spørring {} mot databasen", SQL_QUERY);
                    return false;
                }
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
