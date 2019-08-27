package no.nav.foreldrepenger.fordel.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Unik kode som definerer en gruppe kodeverider som er relatert.
 */
@Entity(name = "KonfigVerdiGruppe")
@DiscriminatorValue(KonfigVerdiGruppe.DISCRIMINATOR)
public class KonfigVerdiGruppe extends Kodeliste {

    public static final String DISCRIMINATOR = "KONFIG_VERDI_GRUPPE";
    /**
     * Default gruppe for kode verdier.
     */
    public static final KonfigVerdiGruppe INGEN_GRUPPE = new KonfigVerdiGruppe("INGEN");

    private KonfigVerdiGruppe() {
        // for hibernate
    }

    private KonfigVerdiGruppe(String kode) {
        super(kode, DISCRIMINATOR);
    }

    /**
     * Oppretter en type-safe, men 'tynn' klasse. Kan ikke lagres men kan brukes til oppslag slik at en unngår å sende
     * String rundt
     *
     * @param gruppeKode - kode for gruppen
     * @return en 'tynn' gruppe som inneholder bare koden.
     */
    public static KonfigVerdiGruppe forGruppe(String gruppeKode) {
        return new KonfigVerdiGruppe(gruppeKode);
    }

}
