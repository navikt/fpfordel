package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import no.nav.vedtak.felles.jpa.BaseEntitet;

/**
 * Superklasse-entitet for tabeller som fungerer som internt kodeverk/ oppslag/ referansedata.
 * Disse tabellene har en felles struktur som inneholder
 * <ul>
 * <li>kode</li>
 * <li>navn - for visning</li>
 * <li>beskrivelse - for dokumentasjon av koden</li>
 * </ul>
 * <p>
 * Bruk: Subklass denne med en en klasse som mapper til en spesifikk tabell.
 */
@MappedSuperclass
public abstract class KodeverkTabell extends BaseEntitet {

    @Id
    @Column(name = "kode", updatable = false, insertable = false, nullable = false)
    private String kode;

    @Column(name = "beskrivelse", updatable = false, insertable = false)
    private String beskrivelse;

    /**
     * Navn registrert i databasen.
     */
    @Column(name = "navn", updatable = false, insertable = false, nullable = false)
    private String navn;

    protected KodeverkTabell() {
        // Hibernate
    }

    protected KodeverkTabell(String kode) {
        Objects.requireNonNull(kode, "kode");
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

    public String getNavn() {
        return navn;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof KodeverkTabell)) {
            return false;
        }
        KodeverkTabell other = (KodeverkTabell) object;
        return Objects.equals(getKode(), other.getKode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(kode);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<kode=" + getKode() + ", navn=" + getNavn() + ">"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}