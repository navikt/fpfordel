package no.nav.foreldrepenger.fordel;

import static no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder.getHolder;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.security.auth.Subject;

import org.glassfish.jersey.spi.ThreadPoolExecutorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;

public class MDCAndTokenValidationContextAwareThreadPoolExecutorProvider extends ThreadPoolExecutorProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MDCAndTokenValidationContextAwareThreadPoolExecutorProvider.class);

    public MDCAndTokenValidationContextAwareThreadPoolExecutorProvider(String name) {
        super(name);
        LOG.info("XXX Konstruert provider");
    }

    @Override
    protected ThreadPoolExecutor createExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        LOG.info("XXX Returnerer executor");
        return new MDCAwareThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue, threadFactory, handler);
    }

    static class MDCAwareThreadPoolExecutor extends ThreadPoolExecutor {
        MDCAwareThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                ThreadFactory threadFactory, RejectedExecutionHandler handler) {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        @Override
        public void execute(Runnable task) {
            LOG.info("XXX eksekverer");
            super.execute(decorate(task));
        }

        static Runnable decorate(Runnable task) {
            return new DecoratedRunnable(task, getSubjectHandler().getSubject(), getHolder().getTokenValidationContext(),
                    MDC.getCopyOfContextMap());
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
            LOG.info("XXX konstruert runnable");
        }

        @Override
        public void run() {
            var holder = JaxrsTokenValidationContextHolder.getHolder();
            setMDC(mdc);
            propagateSubjectIfSet();
            if (ctx.hasValidToken()) {
                propagateContext(holder);
            }
            try {
                LOG.info("XXX eksekverer runnable");
                task.run();
                LOG.info("XXX eksekvert runnable OK");

            } finally {
                LOG.info("XXX rydder opp i tråden");
                holder.setTokenValidationContext(null);
                MDC.clear();
            }
        }

        private void propagateContext(TokenValidationContextHolder holder) {
            LOG.info("XXX Propagating token from context onto thread");
            holder.setTokenValidationContext(ctx);
        }

        private void propagateSubjectIfSet() {
            try {
                if (subject != null) {
                    LOG.info("XXX Propagating token from subject handler onto thread");
                    ThreadLocalSubjectHandler.class.cast(getSubjectHandler()).setSubject(subject);
                } else {
                    LOG.info("XXX Intet subject å propagere");
                }
            } catch (Exception e) {
                LOG.warn("XXX Feil ved propagering av subject", e);

            }
        }

        private static void setMDC(Map<String, String> mdc) {
            MDC.clear();
            LOG.info("XXX setter MDC");
            if (mdc != null) {
                MDC.setContextMap(mdc);
            }
        }
    }
}
