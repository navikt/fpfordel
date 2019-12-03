package no.nav.foreldrepenger.fordel.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "BehandlingTema")
@DiscriminatorValue(BehandlingTema.DISCRIMINATOR)
public class BehandlingTema extends Kodeliste {

    public static final String DISCRIMINATOR = "BEHANDLING_TEMA";

    /**
     * Konstanter for å skrive ned kodeverdi. For å hente ut andre data konfigurert,
     * må disse leses fra databasen (eks. for å hente offisiell kode for et Nav
     * kodeverk).
     */
    public static final BehandlingTema ENGANGSSTØNAD = new BehandlingTema("ENGST"); //$NON-NLS-1$
    public static final BehandlingTema ENGANGSSTØNAD_FØDSEL = new BehandlingTema("ENGST_FODS"); //$NON-NLS-1$
    public static final BehandlingTema ENGANGSSTØNAD_ADOPSJON = new BehandlingTema("ENGST_ADOP"); //$NON-NLS-1$

    public static final BehandlingTema FORELDREPENGER = new BehandlingTema("FORP"); //$NON-NLS-1$
    public static final BehandlingTema FORELDREPENGER_FØDSEL = new BehandlingTema("FORP_FODS"); //$NON-NLS-1$
    public static final BehandlingTema FORELDREPENGER_ADOPSJON = new BehandlingTema("FORP_ADOP"); //$NON-NLS-1$

    public static final BehandlingTema SVANGERSKAPSPENGER = new BehandlingTema("SVP");

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    public static final BehandlingTema UDEFINERT = new BehandlingTema("-");

    BehandlingTema() {
    }

    private BehandlingTema(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public boolean gjelderEngangsstønad() {
        return this.equals(ENGANGSSTØNAD_ADOPSJON) ||
                this.equals(ENGANGSSTØNAD_FØDSEL) ||
                this.equals(ENGANGSSTØNAD);
    }

    public boolean gjelderForeldrepenger() {
        return this.equals(FORELDREPENGER) ||
                this.equals(FORELDREPENGER_FØDSEL) ||
                this.equals(FORELDREPENGER_ADOPSJON);
    }

    public boolean ikkeSpesifikkHendelse() {
        return this.equals(FORELDREPENGER)
                || this.equals(ENGANGSSTØNAD)
                || this.equals(UDEFINERT);
    }

    public boolean gjelderSvangerskapspenger() {
        return this.equals(SVANGERSKAPSPENGER);
    }
}
