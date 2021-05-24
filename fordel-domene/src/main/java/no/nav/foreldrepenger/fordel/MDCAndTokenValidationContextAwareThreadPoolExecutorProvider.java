package no.nav.foreldrepenger.fordel;

import static no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder.getHolder;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;
import static org.slf4j.MDC.getCopyOfContextMap;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.glassfish.jersey.client.ClientAsyncExecutor;
import org.glassfish.jersey.spi.ThreadPoolExecutorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;

@ClientAsyncExecutor
public class MDCAndTokenValidationContextAwareThreadPoolExecutorProvider extends ThreadPoolExecutorProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MDCAndTokenValidationContextAwareThreadPoolExecutorProvider.class);

    public MDCAndTokenValidationContextAwareThreadPoolExecutorProvider() {
        this(MDCAndTokenValidationContextAwareThreadPoolExecutorProvider.class.getSimpleName());
    }

    public MDCAndTokenValidationContextAwareThreadPoolExecutorProvider(String name) {
        super(name);
    }

    @Override
    protected ThreadPoolExecutor createExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        return new MDCAwareThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue, threadFactory, handler);
    }

    static class MDCAwareThreadPoolExecutor extends ThreadPoolExecutor {
        MDCAwareThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        @Override
        public void execute(Runnable task) {
            super.execute(decorate(task));
        }

        static Runnable decorate(Runnable task) {
            return new DecoratedRunnable(task, getSubjectHandler().getSubject(), getHolder().getTokenValidationContext(), getCopyOfContextMap());
        }
    }

    static class DecoratedRunnable implements Runnable {

        private static final Logger LOG = LoggerFactory.getLogger(DecoratedRunnable.class);
        private final Runnable task;
        private final TokenValidationContext ctx;
        private final Map<String, String> mdc;
        private final Subject subject;

        DecoratedRunnable(Runnable task, Subject subject, TokenValidationContext ctx, Map<String, String> mdc) {
            this.task = task;
            this.subject = subject;
            this.ctx = ctx;
            this.mdc = mdc;
        }

        @Override
        public void run() {
            propagate();
            try {
                task.run();
            } finally {
                cleanup();
            }
        }

        private void propagate() {
            propagateMDCIfSet();
            propagateSubjectIfSet();
            propagateContextIfSet();
        }

        private void propagateContextIfSet() {
            if (ctx != null && ctx.hasValidToken()) {
                LOG.trace("Propagerer context");
                JaxrsTokenValidationContextHolder.getHolder().setTokenValidationContext(ctx);
            } else {
                LOG.trace("Ingen context å propagere");
            }
        }

        private void propagateSubjectIfSet() {
            try {
                if (subject != null) {
                    LOG.trace("Propagerer subject fra subject handler");
                    ThreadLocalSubjectHandler.class.cast(getSubjectHandler()).setSubject(subject);
                } else {
                    LOG.trace("Intet subject å propagere");
                }
            } catch (Exception e) {
                LOG.warn("Feil ved propagering av subject", e);

            }
        }

        private void propagateMDCIfSet() {
            MDC.clear();
            if (mdc != null) {
                LOG.trace("Propagerer {} verdier fra MDC", mdc.size());
                MDC.setContextMap(mdc);
            } else {
                LOG.trace("Ingen MDC å propagere");
            }
        }
    }

    private static void cleanup() {
        LOG.trace("Rydder opp i tråden");
        JaxrsTokenValidationContextHolder.getHolder().setTokenValidationContext(null);
        MDC.clear();
        ThreadLocalSubjectHandler.class.cast(getSubjectHandler()).setSubject(null);
        LOG.trace("Ryddet opp i tråden OK");
    }
}
