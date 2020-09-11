package no.nav.foreldrepenger.mottak.extensions;

import static no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering.migrerUnittestSkjemaer;
import static no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering.settPlaceholdereOgJdniOppslag;

import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.sikkerhet.DummySubjectHandler;
import no.nav.vedtak.felles.testutilities.sikkerhet.SubjectHandlerUtils;
import no.nav.vedtak.util.env.Environment;

public class EntityManagerAwareFordelExtension extends EntityManagerAwareExtension {
    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerAwareFordelExtension.class);
    private static final Environment ENV = Environment.current();

    @Override
    protected void init() {
        SubjectHandlerUtils.useSubjectHandler(DummySubjectHandler.class);
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
        if (ENV.getProperty("maven.cmd.line.args") == null) {
            LOG.warn("Kjører migreringer");
            migrerUnittestSkjemaer();
        }
        settPlaceholdereOgJdniOppslag();
    }
}