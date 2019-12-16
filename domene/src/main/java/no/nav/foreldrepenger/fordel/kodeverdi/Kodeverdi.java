package no.nav.foreldrepenger.fordel.kodeverdi;

/** Kodeverk som er portet til java. */
public interface Kodeverdi {

    String getKode();

    String getKodeverk();

    default String getOffisiellKode() {
        throw new IllegalStateException("Utviklerfeil: Kaller getOffisiellKode for kodeverdi som ikke har implementert denne");
    }

    default String getTermNavn() {
        throw new IllegalStateException("Utviklerfeil: Kaller getTermNavn for kodeverdi som ikke har implementert denne");
    }
}
