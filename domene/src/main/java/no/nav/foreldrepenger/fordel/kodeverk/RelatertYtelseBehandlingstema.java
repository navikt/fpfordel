package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "RelatertYtelseBehandlingstema")
@DiscriminatorValue(RelatertYtelseBehandlingstema.DISCRIMINATOR)
public class RelatertYtelseBehandlingstema extends Kodeliste {
    public static final String DISCRIMINATOR = "RELATERT_YTELSE_BEH_TEMA";

    public static final RelatertYtelseBehandlingstema AAP = new RelatertYtelseBehandlingstema("AAP");
    public static final RelatertYtelseBehandlingstema FISK = new RelatertYtelseBehandlingstema("FISK");
    public static final RelatertYtelseBehandlingstema PERM = new RelatertYtelseBehandlingstema("PERM");
    public static final RelatertYtelseBehandlingstema LONN = new RelatertYtelseBehandlingstema("LONN");
    public static final RelatertYtelseBehandlingstema DAGO = new RelatertYtelseBehandlingstema("DAGO");
    public static final RelatertYtelseBehandlingstema BASI = new RelatertYtelseBehandlingstema("BASI");

    public static final RelatertYtelseBehandlingstema FORELDREPENGER_FODSEL_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema(
            "FØ");
    public static final RelatertYtelseBehandlingstema FORELDREPENGER_ADOPSJON_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema(
            "AP");
    public static final RelatertYtelseBehandlingstema FORELDREPENGER_FODSEL_UTLAND_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema(
            "FU");
    public static final RelatertYtelseBehandlingstema SVANGERSKAPSPENGER_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema(
            "SV");
    public static final RelatertYtelseBehandlingstema ENGANGSSTONAD_ADOPSJON_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema(
            "AE");
    public static final RelatertYtelseBehandlingstema ENGANGSSTONAD_FODSEL_BEHANDLINGSTEMA = new RelatertYtelseBehandlingstema(
            "FE");

    private static final List<RelatertYtelseBehandlingstema> FORELDREPENGER_BEHANDLINGSTEMAER = List.of(
            FORELDREPENGER_FODSEL_BEHANDLINGSTEMA,
            FORELDREPENGER_ADOPSJON_BEHANDLINGSTEMA,
            FORELDREPENGER_FODSEL_UTLAND_BEHANDLINGSTEMA);

    private static final List<RelatertYtelseBehandlingstema> ENGANGSSTONAD_BEHANDLINGSTEMAER = List.of(
            ENGANGSSTONAD_ADOPSJON_BEHANDLINGSTEMA,
            ENGANGSSTONAD_FODSEL_BEHANDLINGSTEMA);

    RelatertYtelseBehandlingstema() {
    }

    private RelatertYtelseBehandlingstema(String kode) {
        super(kode, DISCRIMINATOR);
    }

    public static boolean erGjelderEngangsstonad(String behandlingsTema) {
        return ENGANGSSTONAD_BEHANDLINGSTEMAER.stream()
                .anyMatch(tema -> tema.getKode().equals(behandlingsTema));
    }

    public static boolean erGjelderSvangerskapspenger(String behandlingsTema) {
        return SVANGERSKAPSPENGER_BEHANDLINGSTEMA.getKode().equals(behandlingsTema);
    }

    public static boolean erGjelderForeldrepenger(String behandlingsTema) {
        return FORELDREPENGER_BEHANDLINGSTEMAER.stream()
                .anyMatch(tema -> tema.getKode().equals(behandlingsTema));
    }
}
