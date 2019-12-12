package no.nav.foreldrepenger.fordel.dbstoette;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.lokal.dbstoette.DBConnectionProperties;
import no.nav.vedtak.felles.lokal.dbstoette.DatabaseStøtte;

/**
 * Initielt skjemaoppsett + migrering av unittest-skjemaer
 */
public final class Databaseskjemainitialisering {

    private static final Logger log = LoggerFactory.getLogger(Databaseskjemainitialisering.class);
    private static final String TMP_DIR = "java.io.tmpdir";
    private static final String SEMAPHORE_FIL_PREFIX = "no.nav.vedtak.felles.behandlingsprosess";
    private static final String SEMAPHORE_FIL_SUFFIX = "no.nav.vedtak.felles.behandlingsprosess";

    private static final Pattern placeholderPattern = Pattern.compile("\\$\\{(.*)\\}");

    public static void main(String[] args) {
        System.setProperty("user.timezone", ZoneId.of("Europe/Oslo").getId());
        migrerUnittestSkjemaer();
    }

    public static void settOppSkjemaer() {
        try {
            settSchemaPlaceholder(DatasourceConfiguration.UNIT_TEST.getRaw());
            DatabaseStøtte.kjørMigreringFor(DatasourceConfiguration.DBA.get());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void migrerUnittestSkjemaer() {

        settOppSkjemaer();

        try {
            DatabaseStøtte.kjørMigreringFor(DatasourceConfiguration.UNIT_TEST.get());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (!isJenkins()) {
            slettAlleSemaphorer();
            try {
                Files.createTempFile(SEMAPHORE_FIL_PREFIX, SEMAPHORE_FIL_SUFFIX);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            log.info("Kjører på jenkins");
        }
    }

    public static void settPlaceholdereOgJdniOppslag() {
        try {
            Databaseskjemainitialisering.settSchemaPlaceholder(DatasourceConfiguration.UNIT_TEST.getRaw());
            DatabaseStøtte.settOppJndiForDefaultDataSource(DatasourceConfiguration.UNIT_TEST.get());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void kjørMigreringHvisNødvendig() {
        if (!Databaseskjemainitialisering.isJenkins() && !Databaseskjemainitialisering.erMigreringKjørt()) {
            log.info("Kjører migrering på nytt");
            Databaseskjemainitialisering.migrerUnittestSkjemaer();
        } else {
            if (!Databaseskjemainitialisering.isJenkins()) {
                log.info("Migrering var kjørt nylig (under 5 min siden), så skipper den.");
            }
        }
    }

    public static void settSchemaPlaceholder(List<DBConnectionProperties> connectionProperties)
            throws FileNotFoundException {
        for (DBConnectionProperties dbcp : connectionProperties) {
            Matcher matcher = placeholderPattern.matcher(dbcp.getSchema());
            if (matcher.matches()) {
                String placeholder = matcher.group(1);
                if (System.getProperty(placeholder) == null) {
                    System.setProperty(placeholder, dbcp.getDefaultSchema());
                }
            }
        }
    }

    public static boolean erMigreringKjørt() {
        File[] list = getSemaphorer();
        if (list.length == 0) {
            log.info("Migrering er ikke kjørt");
            return false;
        }

        try {
            BasicFileAttributes attr = Files.readAttributes(list[0].toPath(), BasicFileAttributes.class);
            if (attr.creationTime().toInstant().isBefore(Instant.now().minusSeconds(300))) {
                log.info("Migrering ble kjørt for mer enn 5 minutter siden");
                Files.deleteIfExists(list[0].toPath());
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list.length != 0;
    }

    public static boolean isJenkins() {
        return System.getenv().containsKey("BUILD_NUMBER") && System.getenv().containsKey("BRANCH_NAME");
    }

    private static void slettAlleSemaphorer() {
        File[] list = getSemaphorer();
        Stream.of(list).forEach(e -> {
            try {
                Files.deleteIfExists(e.toPath());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
    }

    private static File[] getSemaphorer() {
        File tmpDir = new File(System.getProperty(TMP_DIR));
        return tmpDir.listFiles((dir, name) -> {
            return name.startsWith(SEMAPHORE_FIL_PREFIX) && name.endsWith(SEMAPHORE_FIL_SUFFIX);
        });
    }
}
