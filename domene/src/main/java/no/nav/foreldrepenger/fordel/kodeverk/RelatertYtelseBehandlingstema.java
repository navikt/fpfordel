package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.Arrays;
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "RelatertYtelseBehandlingstema")
@DiscriminatorValue(RelatertYtelseBehandlingstema.DISCRIMINATOR)
public class RelatertYtelseBehandlingstema extends Kodeliste {
    public static final String DISCRIMINATOR = "RELATERT_YTELSE_BEH_TEMA"; //$NON-NLS-1$

    public static final RelatertYtelseBehandlingstema AAP = new RelatertYtelseBehandlingstema("AAP"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema FISK = new RelatertYtelseBehandlingstema("FISK"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema PERM = new RelatertYtelseBehandlingstema("PERM"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema LONN = new RelatertYtelseBehandlingstema("LONN"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema DAGO = new RelatertYtelseBehandlingstema("DAGO"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema BASI = new RelatertYtelseBehandlingstema("BASI"); //$NON-NLS-1$

    public static final RelatertYtelseBehandlingstema FORELDREPENGER_FODSEL_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema("FØ"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema FORELDREPENGER_ADOPSJON_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema("AP"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema FORELDREPENGER_FODSEL_UTLAND_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema("FU"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema SVANGERSKAPSPENGER_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema("SV"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema ENGANGSSTONAD_ADOPSJON_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema("AE"); //$NON-NLS-1$
    public static final RelatertYtelseBehandlingstema ENGANGSSTONAD_FODSEL_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema("FE"); //$NON-NLS-1$

    private static final List<RelatertYtelseBehandlingstema> FORELDREPENGER_BEHANDLINGSTEMAER = Arrays.asList(
        FORELDREPENGER_FODSEL_BEHANDLINGSTEMA, FORELDREPENGER_ADOPSJON_BEHANDLINGSTEMA, FORELDREPENGER_FODSEL_UTLAND_BEHANDLINGSTEMA);

    private static final List<RelatertYtelseBehandlingstema> ENGANGSSTONAD_BEHANDLINGSTEMAER = Arrays.asList(ENGANGSSTONAD_ADOPSJON_BEHANDLINGSTEMA,
        ENGANGSSTONAD_FODSEL_BEHANDLINGSTEMA);

    RelatertYtelseBehandlingstema() {
        // Hibernate trenger den
    }

    private RelatertYtelseBehandlingstema(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static boolean erGjelderEngangsstonad(String behandlingsTema) {
        return ENGANGSSTONAD_BEHANDLINGSTEMAER.stream().anyMatch(relatertYtelseBehandlingstema ->
            relatertYtelseBehandlingstema.getKode().equals(behandlingsTema));
    }

    public static boolean erGjelderSvangerskapspenger(String behandlingsTema) {
        return SVANGERSKAPSPENGER_BEHANDLINGSTEMA.getKode().equals(behandlingsTema);
    }

    public static boolean erGjelderForeldrepenger(String behandlingsTema) {
        return FORELDREPENGER_BEHANDLINGSTEMAER.stream().anyMatch(relatertYtelseBehandlingstema ->
            relatertYtelseBehandlingstema.getKode().equals(behandlingsTema));
    }
}
