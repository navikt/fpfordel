package no.nav.foreldrepenger.fordel.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "MottakKanal")
@DiscriminatorValue(MottakKanal.DISCRIMINATOR)
public class MottakKanal extends Kodeliste {

    public static final String DISCRIMINATOR = "MOTTAK_KANAL";

    /**
     * Konstanter for å skrive ned kodeverdi. For å hente ut andre data konfigurert, må disse leses fra databasen (eks.
     * for å hente offisiell kode for et Nav kodeverk).
     */
    public static final MottakKanal ALTINN = new MottakKanal("ALTINN");
    public static final MottakKanal EIA = new MottakKanal("EIA");
    public static final MottakKanal EKSTERNT_OPPSLAG = new MottakKanal("EKST_OPPS");
    public static final MottakKanal NAV_NO = new MottakKanal("NAV_NO");
    public static final MottakKanal SKANNING_NETS = new MottakKanal("SKAN_NETS");
    public static final MottakKanal SKANNING_PENSJON = new MottakKanal("SKAN_PEN");

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden gjør samme nytten.
     */
    public static final MottakKanal UDEFINERT = new MottakKanal("-");

    MottakKanal() {
        // Hibernate trenger den
    }

    private MottakKanal(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
