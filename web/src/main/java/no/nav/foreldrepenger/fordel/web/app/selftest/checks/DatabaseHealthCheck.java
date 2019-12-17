package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@ApplicationScoped
public class DatabaseHealthCheck extends ExtHealthCheck {

    private static final String JDBC_DEFAULT_DS = "jdbc/defaultDS";

    private String jndiName;

    private static final String SQL_QUERY = "select count(1) from PROSESS_TASK_TYPE";
    // må være rask, og bruke et stabilt tabell-navn

    private String endpoint = null; // ukjent frem til første gangs test

    public DatabaseHealthCheck() {
        this.jndiName = JDBC_DEFAULT_DS;
    }

    @Override
    protected String getDescription() {
        return "Test av databaseforbindelse (" + jndiName + ")";
    }

    @Override
    protected String getEndpoint() {
        return endpoint;
    }

    @Override
    protected InternalResult performCheck() {

        InternalResult intTestRes = new InternalResult();

        DataSource dataSource = null;
        try {
            dataSource = (DataSource) new InitialContext().lookup(jndiName);
        } catch (NamingException e) {
            intTestRes.setMessage("Feil ved JNDI-oppslag for " + jndiName + " - " + e);
            intTestRes.setException(e);
            return intTestRes;
        }

        try (Connection connection = dataSource.getConnection()) {
            if (endpoint == null) {
                endpoint = extractEndpoint(connection);
            }
            try (Statement statement = connection.createStatement()) {
                if (!statement.execute(SQL_QUERY)) {
                    throw new SQLException("SQL-spørring ga ikke et resultatsett");
                }
            }
        } catch (SQLException e) {
            intTestRes.setMessage("Feil ved SQL-spørring (" + SQL_QUERY + ") mot databasen");
            intTestRes.setException(e);
            return intTestRes;
        }

        intTestRes.noteResponseTime();
        intTestRes.setOk(true);
        return intTestRes;
    }

    private String extractEndpoint(Connection connection) {
        String result = "?";
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            if (url != null) {
                if (!url.toUpperCase(Locale.US).contains("SERVICE_NAME=")) { // don't care about Norwegian letters here
                    url = url + "/" + connection.getSchema();
                }
                result = url;
            }
        } catch (SQLException e) { //NOSONAR
            // ikke fatalt
        }
        return result;
    }
}
