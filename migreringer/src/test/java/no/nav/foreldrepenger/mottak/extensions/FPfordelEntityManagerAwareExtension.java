package no.nav.foreldrepenger.mottak.extensions;

import static no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering.migrer;
import static no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering.settJdniOppslag;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;
import no.nav.vedtak.util.env.Environment;

public class FPfordelEntityManagerAwareExtension extends EntityManagerAwareExtension {
    private static final Logger LOG = LoggerFactory.getLogger(FPfordelEntityManagerAwareExtension.class);

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
        if (!(Environment.current().getProperty("maven.cmd.line.args") != null)) {
            LOG.info("Kjører IKKE under maven");
            // prøver alltid migrering hvis endring, ellers funker det dårlig i IDE.
            migrer();
        }
        settJdniOppslag();
    }

}
