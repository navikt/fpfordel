package no.nav.foreldrepenger.fordel.web.server.jetty;

import static javax.servlet.DispatcherType.REQUEST;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.Security;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.security.auth.message.config.AuthConfigFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.geronimo.components.jaspi.AuthConfigFactoryImpl;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.jaspi.JaspiAuthenticatorFactory;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.MetaData;
import org.eclipse.jetty.webapp.WebAppConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.slf4j.MDC;

import no.nav.security.token.support.core.configuration.IssuerProperties;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import no.nav.security.token.support.jaxrs.servlet.JaxrsJwtTokenValidationFilter;
import no.nav.vedtak.util.env.Environment;

abstract class AbstractJettyServer {

    /**
     * @see AbstractNetworkConnector#getHost()
     * @see org.eclipse.jetty.server.ServerConnector#openAcceptChannel()
     */
    // TODO : Trenger vi egentlig å sette denne? Spec ser ut til å si at det er eq
    // med null, settes den default til null eller binder den mot et interface?

    protected static final String SERVER_HOST = "0.0.0.0";
    public static final String ACR_LEVEL4 = "acr=Level4";
    public static final String TOKENX = "tokenx";
    public static final String IDPORTEN = "idporten";

    private static final Environment ENV = Environment.current();

    /**
     * Legges først slik at alltid resetter context før prosesserer nye requests.
     * Kjøres først så ikke risikerer andre har satt Request#setHandled(true).
     */
    static final class ResetLogContextHandler extends AbstractHandler {
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
            MDC.clear();
        }
    }

    protected static final Configuration[] CONFIGURATIONS = new Configuration[] {
            new WebInfConfiguration(),
            new WebXmlConfiguration(),
            new WebAppConfiguration(),
            new AnnotationConfiguration(),
            new EnvConfiguration(),
            new PlusConfiguration(),
    };
    private final JettyWebKonfigurasjon webKonfigurasjon;

    protected AbstractJettyServer(JettyWebKonfigurasjon webKonfigurasjon) {
        this.webKonfigurasjon = webKonfigurasjon;
    }

    protected void bootStrap() throws Exception {
        konfigurer();
        migrerDatabaser();
        start(webKonfigurasjon);
    }

    protected void konfigurer() throws Exception {
        konfigurerMiljø();
        konfigurerSikkerhet();
        konfigurerJndi();
    }

    protected abstract void konfigurerMiljø() throws Exception; // NOSONAR

    protected void konfigurerSikkerhet() {
        Security.setProperty(AuthConfigFactory.DEFAULT_FACTORY_SECURITY_PROPERTY, AuthConfigFactoryImpl.class.getCanonicalName());

        var jaspiConf = new File(System.getProperty("conf", "./conf") + "/jaspi-conf.xml");
        if (!jaspiConf.exists()) {
            throw new IllegalStateException("Missing required file: " + jaspiConf.getAbsolutePath());
        }
        System.setProperty("org.apache.geronimo.jaspic.configurationFile", jaspiConf.getAbsolutePath());
    }

    protected abstract void konfigurerJndi() throws Exception; // NOSONAR

    protected abstract void migrerDatabaser() throws IOException;

    protected void start(JettyWebKonfigurasjon jettyWebKonfigurasjon) throws Exception {
        Server server = new Server(jettyWebKonfigurasjon.getServerPort());
        server.setConnectors(createConnectors(jettyWebKonfigurasjon, server).toArray(new Connector[] {}));
        var handlers = new HandlerList(new ResetLogContextHandler(), createContext(jettyWebKonfigurasjon));
        server.setHandler(handlers);
        server.start();
        server.join();
    }

    protected List<Connector> createConnectors(JettyWebKonfigurasjon jettyWebKonfigurasjon, Server server) {
        List<Connector> connectors = new ArrayList<>();
        var httpConnector = new ServerConnector(server, new HttpConnectionFactory(createHttpConfiguration()));
        httpConnector.setPort(jettyWebKonfigurasjon.getServerPort());
        httpConnector.setHost(SERVER_HOST);
        connectors.add(httpConnector);

        return connectors;
    }

    protected WebAppContext createContext(JettyWebKonfigurasjon webKonfigurasjon) throws IOException {
        var ctx = new WebAppContext();
        ctx.setParentLoaderPriority(true);

        // må hoppe litt bukk for å hente web.xml fra classpath i stedet for fra
        // filsystem.
        String descriptor;
        try (var resource = Resource.newClassPathResource("/WEB-INF/web.xml")) {
            descriptor = resource.getURI().toURL().toExternalForm();
        }
        ctx.setDescriptor(descriptor);
        ctx.setBaseResource(createResourceCollection());
        ctx.setContextPath(webKonfigurasjon.getContextPath());
        ctx.setConfigurations(CONFIGURATIONS);
        ctx.setAttribute("org.eclipse.jetty.server.webapp.WebInfIncludeJarPattern", "^.*resteasy-.*.jar$|^.*felles-.*.jar$");
        ctx.setSecurityHandler(createSecurityHandler());
        addTokenValidationFilter(ctx);
        ctx.setParentLoaderPriority(true);
        updateMetaData(ctx.getMetaData());
        return ctx;
    }

    private void addTokenValidationFilter(WebAppContext ctx) {
        ctx.addFilter(new FilterHolder(new JaxrsJwtTokenValidationFilter(config())),
                "/fpfordel/api/*",
                EnumSet.of(REQUEST));
    }

    private static MultiIssuerConfiguration config() {
        return new MultiIssuerConfiguration(
                Map.of(TOKENX, issuerProperties("token.x.well.known.url", "token.x.client.id"),
                        IDPORTEN, issuerProperties("loginservice.idporten.discovery.url", "loginservice.idporten.audience")));
    }

    private static IssuerProperties issuerProperties(String wellKnownUrl, String clientId) {
        return new IssuerProperties(ENV.getRequiredProperty(wellKnownUrl, URL.class), List.of(ENV.getRequiredProperty(clientId)));
    }

    private ResourceCollection createResourceCollection() {
        return new ResourceCollection(
                Resource.newClassPathResource("META-INF/resources/webjars/"),
                Resource.newClassPathResource("/web"));
    }

    private void updateMetaData(MetaData metaData) {
        // Find path to class-files while starting jetty from development environment.
        var resources = getWebInfClasses().stream()
                .map(c -> Resource.newResource(c.getProtectionDomain().getCodeSource().getLocation()))
                .distinct()
                .collect(Collectors.toList());

        // metaData.setWebInfClassesDirs(resources);
        metaData.setWebInfClassesResources(resources);
    }

    protected abstract List<Class<?>> getWebInfClasses();

    protected HttpConfiguration createHttpConfiguration() {
        // Create HTTP Config
        HttpConfiguration httpConfig = new HttpConfiguration();

        // Add support for X-Forwarded headers
        httpConfig.addCustomizer(new org.eclipse.jetty.server.ForwardedRequestCustomizer());

        return httpConfig;

    }

    private static SecurityHandler createSecurityHandler() {
        ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();
        securityHandler.setAuthenticatorFactory(new JaspiAuthenticatorFactory());

        JAASLoginService loginService = new JAASLoginService();
        loginService.setName("jetty-login");
        loginService.setLoginModuleName("jetty-login");
        loginService.setIdentityService(new DefaultIdentityService());
        securityHandler.setLoginService(loginService);

        return securityHandler;
    }
}
