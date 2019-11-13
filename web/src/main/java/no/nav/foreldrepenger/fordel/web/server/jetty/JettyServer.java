package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.WebAppContext;

import no.nav.foreldrepenger.fordel.web.app.konfig.ApplicationConfig;
import no.nav.foreldrepenger.fordel.web.app.util.ClusterDetector;
import no.nav.foreldrepenger.fordel.web.server.jetty.DataSourceKonfig.DBConnProp;
import no.nav.vedtak.isso.IssoApplication;

public class JettyServer extends AbstractJettyServer {

    private DataSourceKonfig dataSourceKonfig;

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
        if (!ClusterDetector.isProd()) {
            System.setProperty("log.level.no.nav.foreldrepenger.fordel", "TRACE");
        }
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
        loadBalancerFqdnTilLoadBalancerUrl();
        wsMedLTPAmåIgjennomServiceGateway();
        temporært();
    }

    private void loadBalancerFqdnTilLoadBalancerUrl() {
        if (System.getenv("LOADBALANCER_FQDN") != null) {
            String loadbalancerFqdn = System.getenv("LOADBALANCER_FQDN");
            String protocol = (loadbalancerFqdn.startsWith("localhost")) ? "http" : "https";
            System.setProperty("loadbalancer.url", protocol + "://" + loadbalancerFqdn);
        }
    }

    private void wsMedLTPAmåIgjennomServiceGateway() {
        if (System.getenv("SERVICEGATEWAY_URL") != null) {
            System.setProperty("Oppgave_v3.url", System.getenv("SERVICEGATEWAY_URL"));
        }
    }

    private void temporært() {
        // FIXME : PFP-1176 Skriv om i OpenAmIssoHealthCheck og
        // AuthorizationRequestBuilder når Jboss dør
        if (System.getenv("OIDC_OPENAM_HOSTURL") != null) {
            System.setProperty("OpenIdConnect.issoHost", System.getenv("OIDC_OPENAM_HOSTURL"));
        }
        // FIXME : PFP-1176 Skriv om i AuthorizationRequestBuilder og
        // IdTokenAndRefreshTokenProvider når Jboss dør
        if (System.getenv("OIDC_OPENAM_AGENTNAME") != null) {
            System.setProperty("OpenIdConnect.username", System.getenv("OIDC_OPENAM_AGENTNAME"));
        }
        // FIXME : PFP-1176 Skriv om i IdTokenAndRefreshTokenProvider når Jboss dør
        if (System.getenv("OIDC_OPENAM_PASSWORD") != null) {
            System.setProperty("OpenIdConnect.password", System.getenv("OIDC_OPENAM_PASSWORD"));
        }
        // FIXME : PFP-1176 Skriv om i BaseJmsKonfig når Jboss dør
        if (System.getenv("FPSAK_CHANNEL_NAME") != null) {
            System.setProperty("mqGateway02.channel", System.getenv("FPSAK_CHANNEL_NAME"));
        }
    }

    @Override
    protected void konfigurerJndi() throws Exception {
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
            new DatabaseScript(dbConnProp.getDatasource(), false, dbConnProp.getMigrationScripts()).migrate();
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
                Resource.newClassPathResource("/web"));
    }

}
