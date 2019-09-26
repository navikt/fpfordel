package no.nav.foreldrepenger.fordel.konfig;

import java.time.LocalDate;

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
            @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + KonfigVerdiGruppe.DISCRIMINATOR + "'"))})
    private KonfigVerdiGruppe konfigVerdiGruppe = KonfigVerdiGruppe.INGEN_GRUPPE;

    /**
     * Foreløpig skrudd av mulghet for oppdatering.
     */
    @Column(name = "konfig_verdi", insertable = false, updatable = false)
    private String verdi;


    @Column(name = "fom")
    private LocalDate fomDato;

    @Column(name = "tom")
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

}
