package no.nav.foreldrepenger.mottak.extensions;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
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

public class RepositoryExtension extends
        PersistenceUnitInitializer
        implements InvocationInterceptor, TestInstancePostProcessor, BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    @Override
    public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
            ExtensionContext extensionContext) throws Throwable {

        LOG.info("Intercepting");
        WeldContext.getInstance().doWithScope(() -> {
            EntityTransaction trans = null;
            try {
                trans = startTransaction();
                return invocation.proceed();
            } catch (Throwable e) {
                if (trans != null) {
                    trans.rollback();
                }
            } finally {
                getEntityManager().clear();
            }
            return null;
        });
    }

    private static final Logger LOG = LoggerFactory.getLogger(RepositoryExtension.class);

    private EntityTransaction startTransaction() {
        EntityTransaction transaction = getEntityManager().getTransaction();

        transaction.begin();
        return transaction;
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        LOG.info("After each");

    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        SubjectHandlerUtils.useSubjectHandler(DummySubjectHandler.class);
        LOG.info("Before each");

    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        LOG.info("After all");

    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        LOG.info("Before all");
    }

    @Override
    public EntityManager getEntityManager() {
        return WeldContext.getInstance().doWithScope(super::getEntityManager);
    }

    @Override
    protected void init() {
        LOG.info("Init ");
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        testInstance.getClass()
                .getMethod("setEntityManager", EntityManager.class)
                .invoke(testInstance, getEntityManager());

    }

}