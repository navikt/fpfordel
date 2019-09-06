package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);

    private static String TEMPLATE_FILNAVN = "es-dev.properties";
    private static String JETTY_SCHEMAS_LOCAL = "jetty_web_server.json";

    private static String DEV_FILNAVN = "es.properties";
    private static String DEV_FILNAVN_LOCAL = "es-local.properties";
    private static String VTP_FILNAVN_LOCAL = "es-vtp.properties";


    private PropertiesUtils() {
    }

    static List<JettyDevDbKonfigurasjon> getDBConnectionProperties() throws IOException {
        ClassLoader classLoader = PropertiesUtils.class.getClassLoader();
        File file = new File(classLoader.getResource(JETTY_SCHEMAS_LOCAL).getFile());
        return JettyDevDbKonfigurasjon.fraFil(file);
    }

    static void lagPropertiesFilFraTemplate() throws IOException {
        File devFil = new File(DEV_FILNAVN);

        ClassLoader classLoader = PropertiesUtils.class.getClassLoader();
        File templateFil = new File(classLoader.getResource(TEMPLATE_FILNAVN).getFile());

        copyTemplateFile(templateFil, devFil, true);

        // create local file
        File localProps = new File(DEV_FILNAVN_LOCAL);
        if (!localProps.exists()) {
            boolean fileCreated = localProps.createNewFile();
            if (!fileCreated) {
                LOGGER.error("Kunne ikke opprette properties-fil");
            }
        }
    }

    private static void copyTemplateFile(File templateFil, File targetFil, boolean backup) throws IOException {
        if (!targetFil.exists()) {
            Files.copy(templateFil.toPath(), targetFil.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } else if ((targetFil.lastModified() < templateFil.lastModified())) {
            if (backup) {
                File backupDev = new File(targetFil.getAbsolutePath() + ".backup");
                Files.copy(targetFil.toPath(), backupDev.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            Files.copy(templateFil.toPath(), targetFil.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static void initProperties(boolean vtp) {
        File devFil = new File(DEV_FILNAVN);
        loadPropertyFile(devFil);
        loadPropertyFile(new File(DEV_FILNAVN_LOCAL));
        if (vtp) {
            loadPropertyFile(new File(VTP_FILNAVN_LOCAL));
        }
    }

    private static void loadPropertyFile(File devFil) {
        if (devFil.exists()) {
            Properties prop = new Properties();
            try (InputStream inputStream = new FileInputStream(devFil)) {
                prop.load(inputStream);
            } catch (IOException e) {
                LOGGER.error("Kunne ikke finne properties-fil", e);
            }
            System.getProperties().putAll(prop);
        }
    }

    static File lagLogbackConfig() throws IOException {
        File logbackConfig = new File("logback.xml");

        ClassLoader classLoader = PropertiesUtils.class.getClassLoader();
        File templateFil = new File(classLoader.getResource("logback-dev.xml").getFile());

        copyTemplateFile(templateFil, logbackConfig, false);

        return logbackConfig;

    }
}
