package no.nav.foreldrepenger.mottak.tjeneste;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class KonfigVerdiTjeneste {

    private LocalDate konfigVerdiStartdatoForeldrepenger;

    @Inject
    public KonfigVerdiTjeneste(@KonfigVerdi(value = "foreldrepenger.startdato") String konfigVerdiStartdatoForeldrepenger) {
        this.konfigVerdiStartdatoForeldrepenger = LocalDate.parse(konfigVerdiStartdatoForeldrepenger, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public KonfigVerdiTjeneste() {
        //NOSONAR: for CDI
    }

    public LocalDate getKonfigVerdiStartdatoForeldrepenger() {
        return konfigVerdiStartdatoForeldrepenger;
    }

}