package no.nav.foreldrepenger.fordel.konfig;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.fordel.kodeverk.KodeverkTabell;
import no.nav.foreldrepenger.fordel.kodeverk.KonfigVerdiGruppe;
import no.nav.foreldrepenger.fordel.kodeverk.KonfigVerdiType;

/**
 * Unik kode som definerer gyldige konfigurerbare verdier.
 */
@Table(name = "KONFIG_VERDI_KODE")
@Entity(name = "KonfigVerdiKode")
public class KonfigVerdiKode extends KodeverkTabell {

    @ManyToOne
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "konfig_gruppe", referencedColumnName = "kode", nullable = false, insertable = false, updatable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KonfigVerdiGruppe.DISCRIMINATOR + "'"))})
    private KonfigVerdiGruppe konfigVerdiGruppe = KonfigVerdiGruppe.INGEN_GRUPPE;

    @ManyToOne
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "konfig_type", referencedColumnName = "kode", nullable = false, insertable = false, updatable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KonfigVerdiType.DISCRIMINATOR + "'"))})
    private KonfigVerdiType konfigVerdiType = KonfigVerdiType.STRING_TYPE;

    KonfigVerdiKode() {
        // for hibernate
    }

    KonfigVerdiKode(String kode) {
        super(kode);
    }

}
