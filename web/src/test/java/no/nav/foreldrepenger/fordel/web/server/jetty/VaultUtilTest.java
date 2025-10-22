package no.nav.foreldrepenger.fordel.web.server.jetty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class VaultUtilTest {

    protected static final String TEST_RESOURCES_PATH = Path.of("src/test/resources").toString();

    @BeforeEach
    void setUp() {
        System.setProperty(VaultUtil.VAULT_MOUNT_PATH, TEST_RESOURCES_PATH);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(VaultUtil.VAULT_MOUNT_PATH);
    }

    @Test
    void testFileRead() {
        var verdi = VaultUtil.lesFilVerdi("/vault/secret", "user");
        assertThat(verdi).isEqualTo("srvTest");
    }

    @Test
    void testExceptionWhenFileNotFound() {
        var mappeNavn = "secret";
        var filNavn = "user";

        var message = assertThrows(IllegalStateException.class, () -> VaultUtil.lesFilVerdi(mappeNavn, filNavn));
        assertThat(message.getMessage()).startsWith("Mangler vault konfig for %s".formatted(Path.of(TEST_RESOURCES_PATH, mappeNavn, filNavn)));
    }
}
