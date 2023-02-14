package no.nav.foreldrepenger.fordel.web.app.tjenester;

import static java.util.concurrent.CompletableFuture.runAsync;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.apptjeneste.AppServiceHandler;

@ApplicationScoped
public class ApplicationServiceStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceStarter.class);
    private List<AppServiceHandler> handlers;

    ApplicationServiceStarter() {
    }

    @Inject
    public ApplicationServiceStarter(@Any Instance<AppServiceHandler> handlers) {
        this(handlers.stream().toList());
    }

    ApplicationServiceStarter(AppServiceHandler handler) {
        this(List.of(handler));
    }

    ApplicationServiceStarter(List<AppServiceHandler> handlers) {
        this.handlers = handlers;
    }

    public void startServices() {
        LOGGER.info("Starter {} services", handlers.size());
        CompletableFuture.allOf(handlers.stream().map(h -> {
            return runAsync(h::start);
        }).toArray(CompletableFuture[]::new)).join();
        LOGGER.info("Startet  {} services", handlers.size());
    }

    public void stopServices() {
        LOGGER.info("Stopper {} services", handlers.size());
        CompletableFuture.allOf(handlers.stream().map(h -> runAsync(h::stop)).toArray(CompletableFuture[]::new)).join();
        LOGGER.info("Stoppet {} services", handlers.size());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [handlers=" + handlers + "]";
    }

}
