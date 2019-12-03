package no.nav.foreldrepenger.fordel.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "VariantFormat")
@DiscriminatorValue(VariantFormat.DISCRIMINATOR)
public class VariantFormat extends Kodeliste {

    public static final String DISCRIMINATOR = "VARIANT_FORMAT";

    /**
     * Konstanter for å skrive ned kodeverdi. For å hente ut andre data konfigurert,
     * må disse leses fra databasen (eks. for å hente offisiell kode for et Nav
     * kodeverk).
     */
    public static final VariantFormat PRODUKSJON = new VariantFormat("PROD");
    public static final VariantFormat ARKIV = new VariantFormat("ARKIV");
    public static final VariantFormat SKANNING_META = new VariantFormat("SKANM");
    public static final VariantFormat BREVBESTILLING = new VariantFormat("BREVB");
    public static final VariantFormat ORIGINAL = new VariantFormat("ORIG");
    public static final VariantFormat FULLVERSJON = new VariantFormat("FULL");
    public static final VariantFormat SLADDET = new VariantFormat("SLADD");
    public static final VariantFormat PRODUKSJON_DLF = new VariantFormat("PRDLF");

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    public static final VariantFormat UDEFINERT = new VariantFormat("-");

    VariantFormat() {
    }

    private VariantFormat(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
