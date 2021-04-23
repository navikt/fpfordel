package no.nav.foreldrepenger.fordel.web.app.tjenester;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.hotspot.DefaultExports;
import no.nav.vedtak.apptjeneste.AppServiceHandler;

@ApplicationScoped
public class ApplicationServiceStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceStarter.class);
    private List<AppServiceHandler> handlers;

    ApplicationServiceStarter() {
        // CDI
    }

    @Inject
    public ApplicationServiceStarter(@Any Instance<AppServiceHandler> handlers) {
        this(handlers.stream().collect(toList()));
    }

    ApplicationServiceStarter(AppServiceHandler handler) {
        this(List.of(handler));
    }

    ApplicationServiceStarter(List<AppServiceHandler> handlers) {
        this.handlers = handlers;
    }

    public void startServices() {
        DefaultExports.initialize();
        handlers.forEach(h -> {
            LOGGER.info("Starter service: {}", h.getClass().getSimpleName());
            h.start();
        });
    }

    public void stopServices() {
        LOGGER.info("Stopper {} services", handlers.size());
        var appHandlers = handlers.stream()
                .map(h -> CompletableFuture.runAsync(() -> {
                    LOGGER.info("Stopper service {}", h.getClass().getSimpleName());
                    h.stop();
                })).collect(toList());

        CompletableFuture.allOf(appHandlers.toArray(new CompletableFuture[appHandlers.size()])).join();
        LOGGER.info("Stoppet {} services", handlers.size());

    }
}
