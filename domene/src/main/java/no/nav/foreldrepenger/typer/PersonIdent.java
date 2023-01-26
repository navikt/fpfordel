package no.nav.foreldrepenger.typer;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Denne mapper p.t Norsk person ident (fødselsnummer, inkl F-nr, D-nr eller FDAT)
 * <ul>
 * <li>F-nr: http://lovdata.no/forskrift/2007-11-09-1268/%C2%A72-2 (F-nr)</li>
 *
 * <li>D-nr: http://lovdata.no/forskrift/2007-11-09-1268/%C2%A72-5 (D-nr), samt hvem som kan utstede
 * (http://lovdata.no/forskrift/2007-11-09-1268/%C2%A72-6)</li>
 *
 * <li>FDAT: Personer uten FNR. Disse har fødselsdato + 00000 (normalt) eller fødselsdato + 00001 (dødfødt).
 * </ul>
 */
public class PersonIdent implements Comparable<PersonIdent> {

    private static final String VALID_REGEXP = "^\\d{11}$";

    private static final Pattern VALID = Pattern.compile(VALID_REGEXP, Pattern.CASE_INSENSITIVE);

    private static final int[] CHECKSUM_EN_VECTOR = new int[]{3, 7, 6, 1, 8, 9, 4, 5, 2};
    private static final int[] CHECKSUM_TO_VECTOR = new int[]{5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    private static final int FNR_LENGDE = 11;

    private static final int PERSONNR_LENGDE = 5;

    @JsonValue
    private final String ident;

    public PersonIdent(String ident) {
        Objects.requireNonNull(ident, "ident kan ikke være null");
        this.ident = ident;
    }

    /**
     * @return true hvis angitt str er et fødselsnummer (F-Nr eller D-Nr). False hvis ikke, eller er FDAT nummer.
     */
    public static boolean erGyldigFnr(final String str) {
        if (str == null) {
            return false;
        }
        var s = str.trim();
        return s.length() == FNR_LENGDE && validerFnrStruktur(s);
    }

    private static int sum(String foedselsnummer, int... faktors) {
        var sum = 0;
        for (int i = 0, l = faktors.length; i < l; ++i) {
            sum += Character.digit(foedselsnummer.charAt(i), 10) * faktors[i];
        }
        return sum;
    }

    private static boolean validerFnrStruktur(String foedselsnummer) {
        if (foedselsnummer.length() != FNR_LENGDE || !VALID.matcher(foedselsnummer).matches()) {
            return false;
        }
        var checksumEn = FNR_LENGDE - (sum(foedselsnummer, CHECKSUM_EN_VECTOR) % FNR_LENGDE);
        if (checksumEn == FNR_LENGDE) {
            checksumEn = 0;
        }
        var checksumTo = FNR_LENGDE - (sum(foedselsnummer, CHECKSUM_TO_VECTOR) % FNR_LENGDE);
        if (checksumTo == FNR_LENGDE) {
            checksumTo = 0;
        }
        return checksumEn == Character.digit(foedselsnummer.charAt(FNR_LENGDE - 2), 10)
            && checksumTo == Character.digit(foedselsnummer.charAt(FNR_LENGDE - 1), 10);
    }

    public static PersonIdent fra(String ident) {
        return ident == null ? null : new PersonIdent(ident);
    }

    @Override
    public int compareTo(PersonIdent o) {
        return this.ident.compareTo(o.ident);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !this.getClass().equals(obj.getClass())) {
            return false;
        }
        var other = (PersonIdent) obj;
        return Objects.equals(ident, other.ident);
    }

    public String getIdent() {
        return ident;
    }

    public boolean erDnr() {
        var n = Character.digit(ident.charAt(0), 10);
        return n > 3 && n <= 7;
    }

    public boolean erNPID() {
        if (ident.length() != FNR_LENGDE) return false;
        var n = Character.digit(ident.charAt(2), 10);
        return n > 1 && n <= 3;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ident);
    }

    @Override
    public String toString() {
        return PersonIdent.class.getSimpleName() + "<ident=" + maskerIdent() + ">";
    }

    private String maskerIdent() {
        var length = ident.length();
        if (length <= 5) {
            return "*".repeat(length);
        }
        return ident.substring(0, 4) + "*".repeat(length - 5) + ident.substring(length - 1);
    }
}
