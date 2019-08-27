package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.Comparator;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "Fagsystem")
@DiscriminatorValue(Fagsystem.DISCRIMINATOR)
public class Fagsystem extends Kodeliste {

    public static final String DISCRIMINATOR = "FAGSYSTEM";
    public static final Comparator<Fagsystem> NULLSAFE_COMPARATOR = Comparator.nullsFirst(Fagsystem::compareTo);

    /**
     * Konstanter for å skrive ned kodeverdi. For å hente ut andre data konfigurert, må disse leses fra databasen (eks.
     * for å hente offisiell kode for et Nav kodeverk).
     */
    public static final Fagsystem FPSAK = new Fagsystem("FPSAK", "FS36");
    public static final Fagsystem TPS = new Fagsystem("TPS", "FS03");
    public static final Fagsystem JOARK = new Fagsystem("JOARK", "AS36");
    public static final Fagsystem INFOTRYGD = new Fagsystem("INFOTRYGD", "IT01");
    public static final Fagsystem ARENA = new Fagsystem("ARENA", "AO01");
    public static final Fagsystem INNTEKT = new Fagsystem("INNTEKT", "FS28");
    public static final Fagsystem MEDL = new Fagsystem("MEDL", "FS18");
    public static final Fagsystem GOSYS = new Fagsystem("GOSYS", "FS22");

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden gjør samme nytten.
     */
    public static final Fagsystem UDEFINERT = new Fagsystem("-");

    Fagsystem() {
        // Hibernate trenger den
    }

    private Fagsystem(String kode) {
        super(kode, DISCRIMINATOR);
    }

    private Fagsystem(String kode, String offisiellKode) {
        super(kode, DISCRIMINATOR, offisiellKode, null, null, null);
    }


}
