package no.nav.foreldrepenger.fordel.dbstoette;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class UnittestRepositoryRule extends RepositoryRule {
    private static final Logger LOG = LoggerFactory.getLogger(UnittestRepositoryRule.class);

    static {
        if (System.getenv("MAVEN_CMD_LINE_ARGS") == null) {
            LOG.warn("Kjører migreringer");
            Databaseskjemainitialisering.migrerUnittestSkjemaer();
        }
        Databaseskjemainitialisering.settPlaceholdereOgJdniOppslag();
    }

    @Override
    protected void init() {
    }

}
