package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.felles.ReadinessAware;

@ApplicationScoped
public class DatabaseHealthCheck implements ReadinessAware {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseHealthCheck.class);
    private static final String JDBC_DEFAULT_DS = "jdbc/defaultDS";

    private final String jndiName;

    private static final String SQL_QUERY = "select sysdate from DUAL";
    // må være rask, og bruke et stabilt tabell-navn

    public DatabaseHealthCheck() {
        this.jndiName = JDBC_DEFAULT_DS;
    }

    private boolean isOK() {

        DataSource dataSource = null;
        try {
            dataSource = (DataSource) new InitialContext().lookup(jndiName);
            LOG.trace("Datasource er {}", dataSource.getClass().getName());
        } catch (NamingException e) {
            LOG.warn("Feil ved JNDI-oppslag for {} exception", jndiName, e);
            return false;
        }

        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                if (!statement.execute(SQL_QUERY)) {
                    throw new SQLException("SQL-spørring ga ikke et resultatsett");
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
}
