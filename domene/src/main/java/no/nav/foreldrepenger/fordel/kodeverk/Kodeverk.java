package no.nav.foreldrepenger.fordel.kodeverk;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;

import no.nav.vedtak.felles.jpa.converters.BooleanToStringConverter;

/**
 * Representerer et kodeverk brukt i applikasjonen. Hvert kodeverk har en eier (default er 'VL' dvs. det er internt
 * kodeverk)
 * <p>
 * Dersom noen av synk flaggene er satt skal dette kodeverket synkroniseres automatisk med kodeverk eier (fungerer bare
 * hvis eier støtter dette og vi har implementert det).
 */
@Entity(name = "Kodeverk")
@Table(name = "KODEVERK")
public class Kodeverk extends KodeverkTabell {

    @Column(name = "kodeverk_eier", nullable = false)
    private String kodeverkEier;

    @Column(name = "kodeverk_eier_ref")
    private String kodeverkEierReferanse;

    @Column(name = "kodeverk_eier_ver")
    private String kodeverkEierVersjon;

    @Column(name = "kodeverk_eier_navn")
    private String kodeverkEierNavn;

    /**
     * Hvorvidt nye koder skal legges til ved synk.
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "kodeverk_synk_nye")
    private Boolean synkNyeKoderFraKodeverEier;

    /**
     * Hvorvidt endringer i gyldig dato (fom, tom) og navn skal oppdateres ved synk.
     */
    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "kodeverk_synk_eksisterende")
    private Boolean synkEksisterendeKoderFraKodeverkEier;

    @Convert(converter = BooleanToStringConverter.class)
    @Column(name = "sammensatt")
    private Boolean sammensatt;

    public Kodeverk() {
        // proxy for hibernate
    }

    /**
     * For å kunne skape instanser hvor man ikke leser opp fra databasen
     * slik som ved enhetstesting.
     */
    public Kodeverk(String kode, String kodeverkEier, String kodeverkEierVersjon, String kodeverkEierNavn,
                    boolean synkNyeKoderFraKodeverEier, boolean synkEksisterendeKoderFraKodeverkEier, boolean sammensatt) {
        super(kode);
        this.kodeverkEier = kodeverkEier;
        this.kodeverkEierVersjon = kodeverkEierVersjon;
        this.kodeverkEierNavn = kodeverkEierNavn;
        this.synkNyeKoderFraKodeverEier = synkNyeKoderFraKodeverEier;
        this.synkEksisterendeKoderFraKodeverkEier = synkEksisterendeKoderFraKodeverkEier;
        this.sammensatt = sammensatt;
    }

    public String getKodeverkEier() {
        return kodeverkEier;
    }

    public String getKodeverkEierNavn() {
        return kodeverkEierNavn;
    }

    public String getKodeverkEierReferanse() {
        return kodeverkEierReferanse;
    }

    public String getKodeverkEierVersjon() {
        return kodeverkEierVersjon;
    }

    public Boolean getSynkEksisterendeKoderFraKodeverkEier() {
        return synkEksisterendeKoderFraKodeverkEier;
    }

    public Boolean getSynkNyeKoderFraKodeverEier() {
        return synkNyeKoderFraKodeverEier;
    }

    public Boolean getSammensatt() {
        return sammensatt;
    }
}
