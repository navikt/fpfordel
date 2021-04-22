package no.nav.foreldrepenger.fordel.web.app.tjenester;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
    private Map<AppServiceHandler, AtomicBoolean> serviceMap = new HashMap<>();

    ApplicationServiceStarter() {
        // CDI
    }

    @Inject
    public ApplicationServiceStarter(@Any Instance<AppServiceHandler> serviceHandlers) {
        var i = serviceHandlers.iterator();
        while (i.hasNext()) {
            serviceMap.put(i.next(), new AtomicBoolean());
        }
    }

    public void startServices() {
        DefaultExports.initialize();
        serviceMap.forEach((key, value) -> {
            if (value.compareAndSet(false, true)) {
                LOGGER.info("starter service: {}", key.getClass().getSimpleName());
                key.start();
            }
        });
    }

    public void stopServices() {
        LOGGER.info("Stopper {} services", serviceMap.size());
        var handlers = serviceMap.keySet().stream()
                .map(h -> CompletableFuture.runAsync(() -> {
                    LOGGER.info("Stopper service {}", h.getClass().getSimpleName());
                    h.stop();
                })).collect(Collectors.toList());

        CompletableFuture.allOf(handlers.toArray(new CompletableFuture[handlers.size()])).join();
    }
}
