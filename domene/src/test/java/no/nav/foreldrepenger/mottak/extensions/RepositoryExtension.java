package no.nav.foreldrepenger.mottak.extensions;

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

import no.nav.foreldrepenger.fordel.dbstoette.Databaseskjemainitialisering;
import no.nav.vedtak.felles.testutilities.cdi.WeldContext;
import no.nav.vedtak.felles.testutilities.db.PersistenceUnitInitializer;
import no.nav.vedtak.felles.testutilities.sikkerhet.DummySubjectHandler;
import no.nav.vedtak.felles.testutilities.sikkerhet.SubjectHandlerUtils;

public class RepositoryExtension extends PersistenceUnitInitializer implements InvocationInterceptor, TestInstancePostProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryExtension.class);

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        LOG.info("Intercepting");
        WeldContext.getInstance().doWithScope(() -> {
            EntityTransaction trans = null;
            try {
                trans = startTransaction();
                invocation.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                if (trans != null) {
                    LOG.info("Ruller tilbake transaction");
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
        if (System.getenv("MAVEN_CMD_LINE_ARGS") == null) {
            LOG.warn("Kjører migreringer");
            Databaseskjemainitialisering.migrerUnittestSkjemaer();
        }
        Databaseskjemainitialisering.settPlaceholdereOgJdniOppslag();
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        testInstance.getClass()
                .getMethod("setEntityManager", EntityManager.class)
                .invoke(testInstance, getEntityManager());

    }

}