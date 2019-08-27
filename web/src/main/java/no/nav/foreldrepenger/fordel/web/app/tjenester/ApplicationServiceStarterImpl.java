package no.nav.foreldrepenger.fordel.web.app.tjenester;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.apptjeneste.AppServiceHandler;

@ApplicationScoped
public class ApplicationServiceStarterImpl implements ApplicationServiceStarter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServiceStarterImpl.class);
    private Map<AppServiceHandler, AtomicBoolean> serviceMap = new HashMap<>();

    ApplicationServiceStarterImpl() {
        // CDI
    }

    @Inject
    public ApplicationServiceStarterImpl(@Any Instance<AppServiceHandler> serviceHandlers) {
        serviceHandlers.forEach(handler -> serviceMap.put(handler, new AtomicBoolean()));
    }

    @Override
    public void startServices() {
        serviceMap.forEach((key, value) -> {
            if (value.compareAndSet(false, true)) {
                LOGGER.info("starter service: {}", key.getClass().getSimpleName());
                key.start();
            }
        });
    }

    @Override
    public void stopServices() {
        List<Thread> threadList = new ArrayList<>();
        serviceMap.forEach((key, value) -> {
            if (value.compareAndSet(true, false)) {
                LOGGER.info("stopper service: {}", key.getClass().getSimpleName());
                Thread t = new Thread(key::stop);
                t.start();
                threadList.add(t);
            }
        });
        while (!threadList.isEmpty()) {
            Thread t = threadList.get(0);
            try {
                t.join(31000);
                threadList.remove(t);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage());
                t.interrupt();
            }
        }
    }

}
