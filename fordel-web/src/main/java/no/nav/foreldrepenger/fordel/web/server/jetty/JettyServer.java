package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.util.List;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.web.app.konfig.ApplicationConfig;
import no.nav.vedtak.isso.IssoApplication;

public class JettyServer extends AbstractJettyServer {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);

    private static final String FLYWAY_SCHEMA_TABLE = "schema_version";
    private DataSourceKonfig dataSourceKonfig;

    public JettyServer(int serverPort) {
        super(new JettyWebKonfigurasjon(serverPort));
    }

    public static void main(String[] args) throws Exception {
        jettyServer(args).bootStrap();
    }

    private static JettyServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyServer(8080);
    }

    @Override
    protected void konfigurerMiljø() {
        dataSourceKonfig = new DataSourceKonfig();
    }

    @Override
    protected void konfigurerJndi() throws Exception {
        new EnvEntry("jdbc/defaultDS", dataSourceKonfig.defaultDS());
    }

    @Override
    protected void migrerDatabaser() {
        for (var cfg : dataSourceKonfig.getDataSources()) {
            LOG.info("Migrerer {}", cfg);
            var flyway = Flyway.configure()
                    .dataSource(cfg.getDatasource())
                    .locations(cfg.getLocations())
                    .baselineOnMigrate(true)
                    .table(FLYWAY_SCHEMA_TABLE)
                    .load();

            flyway.migrate();
        }
    }

    @Override
    protected List<Class<?>> getWebInfClasses() {
        return List.of(ApplicationConfig.class, IssoApplication.class);
    }
}
