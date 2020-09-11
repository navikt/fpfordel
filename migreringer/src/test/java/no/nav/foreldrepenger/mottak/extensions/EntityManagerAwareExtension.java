package no.nav.foreldrepenger.mottak.extensions;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import no.nav.vedtak.felles.testutilities.cdi.WeldContext;
import no.nav.vedtak.felles.testutilities.db.PersistenceUnitInitializer;

public class EntityManagerAwareExtension extends PersistenceUnitInitializer implements InvocationInterceptor, TestInstancePostProcessor {

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
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        testInstance.getClass()
                .getMethod("setEntityManager", EntityManager.class)
                .invoke(testInstance, getEntityManager());
    }
}