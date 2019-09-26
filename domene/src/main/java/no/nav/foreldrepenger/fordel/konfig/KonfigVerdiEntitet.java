package no.nav.foreldrepenger.fordel.konfig;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.fordel.BaseEntitet;
import no.nav.foreldrepenger.fordel.kodeverk.KonfigVerdiGruppe;

@Table(name = "KONFIG_VERDI")
@Entity(name = "KonfigVerdi")
public class KonfigVerdiEntitet extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_KONFIG_VERDI")
    private Long id;

    /**
     * Foreløpig skrudd av mulghet for oppdatering.
     */
    @ManyToOne()
    @JoinColumn(name = "konfig_kode", insertable = false, updatable = false)
    private KonfigVerdiKode konfigVerdiKode;

    @ManyToOne(optional = false)
    @JoinColumnsOrFormulas({
            @JoinColumnOrFormula(column = @JoinColumn(name = "konfig_gruppe", referencedColumnName = "kode", insertable = false, updatable = false)),
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KonfigVerdiGruppe.DISCRIMINATOR + "'")) })
    private KonfigVerdiGruppe konfigVerdiGruppe = KonfigVerdiGruppe.INGEN_GRUPPE;

    /**
     * Foreløpig skrudd av mulghet for oppdatering.
     */
    @Column(name = "konfig_verdi", insertable = false, updatable = false)
    private String verdi;

    @Column(name = "gyldig_fom")
    private LocalDate fomDato;

    @Column(name = "gyldig_tom")
    private LocalDate tomDato;

    private KonfigVerdiEntitet() {
        // for hibernate
    }

    public String getVerdi() {
        return verdi;
    }

    public String getKode() {
        return konfigVerdiKode.getKode();
    }

    public String getKodeGruppe() {
        return konfigVerdiGruppe.getKode();
    }

    Long getId() {
        return id;
    }

    public KonfigVerdiGruppe getKonfigVerdiGruppe() {
        return konfigVerdiGruppe;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "<kode=" + getKode() + ", gruppe=" + getKodeGruppe() + " [" + fomDato + ", " + tomDato + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || !(obj instanceof KonfigVerdiEntitet)) {
            return false;
        }
        var other = (KonfigVerdiEntitet) obj;
        return Objects.equals(this.getKode(), other.getKode())
            && Objects.equals(this.getKodeGruppe(), other.getKodeGruppe());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getKode(), getKodeGruppe());
    }
}
