package no.nav.foreldrepenger.mottak.tjeneste;


import java.time.LocalDate;

public class KonfigVerdiTjeneste {

    private static final LocalDate konfigVerdiStartdatoForeldrepenger = LocalDate.of(2019,1,1);

    public static LocalDate getKonfigVerdiStartdatoForeldrepenger() {
        return konfigVerdiStartdatoForeldrepenger;
    }

}