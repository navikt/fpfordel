package no.nav.foreldrepenger.mottak.extensions;


import static no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering.migrer;
import static no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering.settJdniOppslag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.dbstoette.DBTestUtil;
import no.nav.vedtak.felles.testutilities.db.EntityManagerAwareExtension;
import no.nav.vedtak.util.env.Environment;

public class FPfordelEntityManagerAwareExtension extends EntityManagerAwareExtension {
    private static final Logger LOG = LoggerFactory.getLogger(FPfordelEntityManagerAwareExtension.class);
    private static final boolean isNotRunningUnderMaven = Environment.current().getProperty("maven.cmd.line.args") == null;

    static {
        if (!DBTestUtil.kjøresAvMaven()) {
            LOG.info("Kjører IKKE under maven");
            // prøver alltid migrering hvis endring, ellers funker det dårlig i IDE.
            migrer();
        }
        settJdniOppslag();
    }

}
