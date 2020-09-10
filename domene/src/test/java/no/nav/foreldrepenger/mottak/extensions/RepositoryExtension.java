package no.nav.foreldrepenger.mottak.extensions;

import static no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering.migrerUnittestSkjemaer;
import static no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering.settPlaceholdereOgJdniOppslag;

import java.lang.reflect.Method;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.testutilities.cdi.WeldContext;
import no.nav.vedtak.felles.testutilities.db.PersistenceUnitInitializer;
import no.nav.vedtak.felles.testutilities.sikkerhet.DummySubjectHandler;
import no.nav.vedtak.felles.testutilities.sikkerhet.SubjectHandlerUtils;
import no.nav.vedtak.util.env.Environment;

public class RepositoryExtension extends PersistenceUnitInitializer implements InvocationInterceptor, TestInstancePostProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryExtension.class);
    private static final Environment ENV = Environment.current();

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        WeldContext.getInstance().doWithScope(() -> {
            EntityTransaction trans = null;
            try {
                trans = startTransaction();
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                if (trans != null) {
                    trans.rollback();
                }
                getEntityManager().clear();
            }
            return null;
        });

    }

    private EntityTransaction startTransaction() {
        EntityTransaction transaction = getEntityManager().getTransaction();
        transaction.begin();
        return transaction;
    }

    @Override
    public EntityManager getEntityManager() {
        return WeldContext.getInstance().doWithScope(super::getEntityManager);
    }

    @Override
    protected void init() {
        LOG.info("Init ");
        SubjectHandlerUtils.useSubjectHandler(DummySubjectHandler.class);
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
        if (ENV.getProperty("maven.cmd.line.args") == null) {
            LOG.warn("Kjører migreringer");
            migrerUnittestSkjemaer();
        }
        settPlaceholdereOgJdniOppslag();
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        testInstance.getClass()
                .getMethod("setEntityManager", EntityManager.class)
                .invoke(testInstance, getEntityManager());

    }

}