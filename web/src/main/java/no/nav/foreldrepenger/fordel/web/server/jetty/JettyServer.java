package no.nav.foreldrepenger.fordel.web.server.jetty;

import static no.nav.vedtak.util.env.Cluster.NAIS_CLUSTER_NAME;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.web.app.konfig.ApplicationConfig;
import no.nav.foreldrepenger.fordel.web.server.jetty.DataSourceKonfig.DBConnProp;
import no.nav.vedtak.isso.IssoApplication;

public class JettyServer extends AbstractJettyServer {

    private static final Logger LOG = LoggerFactory.getLogger(JettyServer.class);
    private DataSourceKonfig dataSourceKonfig;
    private static final Environment ENV = Environment.current();

    public JettyServer() {
        this(new JettyWebKonfigurasjon());
    }

    public JettyServer(int serverPort) {
        this(new JettyWebKonfigurasjon(serverPort));
    }

    JettyServer(AppKonfigurasjon appKonfigurasjon) {
        super(appKonfigurasjon);
    }

    public static void main(String[] args) throws Exception {
        // for logback import to work
        System.setProperty(NAIS_CLUSTER_NAME, ENV.clusterName());
        jettyServer(args).bootStrap();
    }

    private static AbstractJettyServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyServer();
    }

    @Override
    protected void konfigurerMiljø() throws Exception {
        dataSourceKonfig = new DataSourceKonfig();
        hacks4Nais();
    }

    private void hacks4Nais() {
        wsMedLTPAmåIgjennomServiceGateway();
        LOG.info("ISSOHOST " + ENV.getProperty("OpenIdConnect.issoHost"));
        // temporært();
    }

// TODO Fjern, slår aldri til
    private void wsMedLTPAmåIgjennomServiceGateway() {
        String url = System.getenv("SERVICEGATEWAY_URL");
        if (url != null) {
            LOG.info("Setter service gateway url fra {}", url);
            System.setProperty("Oppgave_v3.url", url);
        }
    }

    private void temporært() {
        // FIXME : PFP-1176 Skriv om i OpenAmIssoHealthCheck og
        // AuthorizationRequestBuilder når Jboss dør
        // TODO fjern
        String url = System.getenv("OIDC_OPENAM_HOSTURL");
        if (url != null) {
            LOG.info("Setter oidc host url fra {}", url);
            System.setProperty("OpenIdConnect.issoHost", url);
        }
        // FIXME : PFP-1176 Skriv om i AuthorizationRequestBuilder og
        // IdTokenAndRefreshTokenProvider når Jboss dør
        // TODO fjern
        String agent = System.getenv("OIDC_OPENAM_AGENTNAME");
        if (agent != null) {
            LOG.info("Setter agent fra {}", agent);
            System.setProperty("OpenIdConnect.username", agent);
        }
        // FIXME : PFP-1176 Skriv om i IdTokenAndRefreshTokenProvider når Jboss dør
        String pw = System.getenv("OIDC_OPENAM_PASSWORD");
        if (pw != null) {
            LOG.info("Setter password");
            System.setProperty("OpenIdConnect.password", pw);
        }
        // FIXME : PFP-1176 Skriv om i BaseJmsKonfig når Jboss dør
        // TODO fjern, slår aldri til
        String cn = System.getenv("FPSAK_CHANNEL_NAME");
        if (cn != null) {
            LOG.info("Setter channel fra {}", cn);
            System.setProperty("mqGateway02.channel", cn);
        }
    }

    @Override
    protected void konfigurerJndi() throws Exception {
        // What?
        new EnvEntry("jdbc/defaultDS", dataSourceKonfig.getDefaultDatasource().getDatasource());
        konfigurerJms();
    }

    protected void konfigurerJms() {
        try {
            new JmsKonfig().konfigurer();
        } catch (Exception e) {
            throw new IllegalStateException("Kunne ikke konfigurere JMS", e);
        }
    }

    @Override
    protected void migrerDatabaser() throws IOException {
        for (DBConnProp dbConnProp : dataSourceKonfig.getDataSources()) {
            new DatabaseScript(dataSourceKonfig.getDefaultDatasource().getDatasource(), false,
                    dbConnProp.getMigrationScripts()).migrate();
        }
    }

    @Override
    protected WebAppContext createContext(AppKonfigurasjon appKonfigurasjon) throws IOException {
        WebAppContext webAppContext = super.createContext(appKonfigurasjon);
        webAppContext.setParentLoaderPriority(true);
        updateMetaData(webAppContext.getMetaData());
        return webAppContext;
    }

    private void updateMetaData(MetaData metaData) {
        // Find path to class-files while starting jetty from development environment.
        List<Class<?>> appClasses = getWebInfClasses();

        List<Resource> resources = appClasses.stream()
                .map(c -> Resource.newResource(c.getProtectionDomain().getCodeSource().getLocation()))
                .distinct()
                .collect(Collectors.toList());

        metaData.setWebInfClassesDirs(resources);
    }

    protected List<Class<?>> getWebInfClasses() {
        return Arrays.asList(ApplicationConfig.class, IssoApplication.class);
    }

    @Override
    protected ResourceCollection createResourceCollection() throws IOException {
        return new ResourceCollection(
                Resource.newClassPathResource("META-INF/resources/webjars/"),
                Resource.newClassPathResource("/web"));
    }

}
