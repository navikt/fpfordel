package no.nav.foreldrepenger.fordel.web.server.jetty;

import no.nav.foreldrepenger.konfig.Environment;

public class JettyDevServer extends JettyServer {

    private static final Environment ENV = Environment.current();

    private JettyDevServer(int serverPort) {
        super(serverPort);
    }

    public static void main(String[] args) throws Exception {
        initTrustStoreAndKeyStore();
        jettyServer(args).bootStrap();
    }

    protected static JettyDevServer jettyServer(String[] args) {
        if (args.length > 0) {
            return new JettyDevServer(Integer.parseUnsignedInt(args[0]));
        }
        return new JettyDevServer(ENV.getProperty("server.port", Integer.class, 8080));
    }

    private static void initTrustStoreAndKeyStore() {
        var keystoreRelativPath = ENV.getProperty("keystore.relativ.path");
        var truststoreRelativPath = ENV.getProperty("truststore.relativ.path");
        var keystoreTruststorePassword = ENV.getProperty("vtp.ssl.passord");
        var absolutePathHome = ENV.getProperty("user.home", ".");
        System.setProperty("javax.net.ssl.trustStore", absolutePathHome + truststoreRelativPath);
        System.setProperty("javax.net.ssl.keyStore", absolutePathHome + keystoreRelativPath);
        System.setProperty("javax.net.ssl.trustStorePassword", keystoreTruststorePassword);
        System.setProperty("javax.net.ssl.keyStorePassword", keystoreTruststorePassword);
        System.setProperty("javax.net.ssl.password", keystoreTruststorePassword);
        // KAFKA spesifikke properties
        System.setProperty("KAFKA_TRUSTSTORE_PATH", absolutePathHome + truststoreRelativPath);
        System.setProperty("KAFKA_KEYSTORE_PATH", absolutePathHome + keystoreRelativPath);
        System.setProperty("KAFKA_CREDSTORE_PASSWORD", keystoreTruststorePassword);
    }
}
