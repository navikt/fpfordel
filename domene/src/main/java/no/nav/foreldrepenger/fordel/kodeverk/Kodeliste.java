package no.nav.foreldrepenger.fordel.kodeverk;


import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.vedtak.felles.jpa.BaseEntitet;

/**
 * Et innslag i en liste av koder tilgjengelig for et Kodeverk.
 * Koder kan legges til og oppdateres, men tracker ikke endringer over tid (kun av om de er tilgjengelig).
 * <p>
 * Koder skal ikke gjenbrukes, i tråd med anbefalinger fra Kodeverkforvaltningen.Derfor vil kun en
 * gyldighetsperiode vedlikeholdes per kode.
 */
@MappedSuperclass
@Table(name = "KODELISTE")
@DiscriminatorColumn(name = "kodeverk")
public abstract class Kodeliste extends BaseEntitet implements Comparable<Kodeliste> {

    public static final Comparator<Kodeliste> NULLSAFE_KODELISTE_COMPARATOR = Comparator.nullsFirst(Kodeliste::compareTo);

    @Id
    @Column(name = "kodeverk", nullable = false)
    private String kodeverk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kodeverk", referencedColumnName = "kode", insertable = false, updatable = false)
    private Kodeverk kodeverkEntitet;

    @Id
    @Column(name = "kode", nullable = false)
    private String kode;

    /**
     * Kode bestemt av kodeeier. Kan avvike fra intern kodebruk
     */
    @Column(name = "offisiell_kode")
    private String offisiellKode;

    @Column(name = "beskrivelse", updatable = false, insertable = false)
    private String beskrivelse;

    @Column(name = "navn", updatable = false, insertable = false)
    private String navn;

    /**
     * ISO-639-1-alpha-2 lower-case språk.
     */
    @Column(name = "sprak", nullable = false)
    private String språk = "nb";

    /**
     * Når koden gjelder fra og med.
     */
    @Column(name = "gyldig_fom", nullable = false)
    private LocalDate gyldigFraOgMed = LocalDate.of(2000, 01, 01); // NOSONAR

    /**
     * Når koden gjelder til og med.
     */
    @Column(name = "gyldig_tom", nullable = false)
    private LocalDate gyldigTilOgMed = LocalDate.of(9999, 12, 31); // NOSONAR

    /**
     * Denne skal kun inneholde JSON data. Struktur på Json er opp til konkret subklasse å tolke (bruk {@link #getJsonField(String)}
     */
    @Column(name = "ekstra_data")
    private String ekstraData;

    /**
     * Skal ikke leses fra databasen, kun slås opp.
     */
    @Transient
    private String displayNavn;

    protected Kodeliste() {
        // proxy for hibernate
    }

    public Kodeliste(String kode, String kodeverk) {
        Objects.requireNonNull(kode, "kode"); //$NON-NLS-1$
        Objects.requireNonNull(kodeverk, "kodeverk"); //$NON-NLS-1$
        this.kode = kode;
        this.kodeverk = kodeverk;
    }

    public Kodeliste(String kode, String kodeverk, String offisiellKode, String navn, LocalDate fom, LocalDate tom) {
        this(kode, kodeverk);
        this.offisiellKode = offisiellKode;
        this.navn = navn;
        this.gyldigFraOgMed = fom;
        this.gyldigTilOgMed = tom;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public String getKode() {
        return kode;
    }

    public String getOffisiellKode() {
        return offisiellKode;
    }

    public boolean erLikOffisiellKode(String annenOffisiellKode) {
        if (offisiellKode == null) {
            throw new IllegalArgumentException("Har ikke offisiellkode for, Kodeverk=" + getKodeverk() + ", kode=" + getKode()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return offisiellKode.equals(annenOffisiellKode);
    }

    /**
     * Returnerer språk i standard ISO 639-1-alpha2 lower case.
     */
    public String getSpråk() {
        return språk;
    }

    public String getNavn() {
        return navn;
    }

    public LocalDate getGyldigFraOgMed() {
        return gyldigFraOgMed;
    }

    public LocalDate getGyldigTilOgMed() {
        return gyldigTilOgMed;
    }

    protected String getEkstraData() {
        return ekstraData;
    }

    protected String getJsonField(String key) {
        if (getEkstraData() == null) {
            return null;
        }
        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode jsonNode = om.readTree(getEkstraData()).get(key);
            return jsonNode == null ? null : jsonNode.asText();
        } catch (IOException e) {
            throw new IllegalStateException("Ugyldig format (forventet JSON) for kodeverk=" + getKodeverk() + ", kode=" + getKode() //$NON-NLS-1$ //$NON-NLS-2$
                    + ", jsonKey=" + key + ": " + getEkstraData(), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Kodeliste)) {
            return false;
        }
        Kodeliste other = (Kodeliste) obj;
        return Objects.equals(getKode(), other.getKode())
                && Objects.equals(getKodeverk(), other.getKodeverk());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKode(), getKodeverk());
    }

    @Override
    public int compareTo(Kodeliste that) {
        return that.getKode().compareTo(this.getKode());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "<" //$NON-NLS-1$
                + "kode=" + getKode() //$NON-NLS-1$
                + ", kodeverk=" + getKodeverk() //$NON-NLS-1$
                + ", språk=" + språk //$NON-NLS-1$
                + ", offisiellKode=" + offisiellKode //$NON-NLS-1$
                + ", gyldigFom=" + gyldigFraOgMed //$NON-NLS-1$
                + ", gyldigTom=" + gyldigTilOgMed //$NON-NLS-1$
                + ">"; //$NON-NLS-1$
    }

    public String getKodeverk() {
        if (kodeverk == null) {
            DiscriminatorValue dc = getClass().getDeclaredAnnotation(DiscriminatorValue.class);
            if (dc != null) {
                kodeverk = dc.value();
            }
        }
        return kodeverk;
    }

    public static List<String> kodeVerdier(Kodeliste... entries) {
        return kodeVerdier(Arrays.asList(entries));
    }

    public static List<String> kodeVerdier(Collection<? extends Kodeliste> entries) {
        return entries.stream().map(k -> k.getKode()).collect(Collectors.toList());
    }
}
