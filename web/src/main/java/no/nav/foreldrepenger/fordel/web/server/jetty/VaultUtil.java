package no.nav.foreldrepenger.fordel.web.server.jetty;

import no.nav.foreldrepenger.konfig.Environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class VaultUtil {
    private static final Logger LOG = LoggerFactory.getLogger(VaultUtil.class);
    private static final Environment ENV = Environment.current();

    protected static final String VAULT_MOUNT_PATH = "VAULT_MOUNT_PATH";
    protected static final String SECRETS_MOUNT_PATH = ENV.getProperty(VAULT_MOUNT_PATH, "/var/run/secrets/nais.io");

    private VaultUtil() {
    }

    public static String lesFilVerdi(String mappeNavn, String filNavn) {
        var path = Path.of(SECRETS_MOUNT_PATH, mappeNavn, filNavn);
        try {
            return Files.readString(path).trim();
        } catch (IOException e) {
            LOG.error("Feil ved henting av secret fra {}", path, e);
            throw new IllegalStateException("Mangler vault konfig for %s".formatted(path));
        }
    }
}
