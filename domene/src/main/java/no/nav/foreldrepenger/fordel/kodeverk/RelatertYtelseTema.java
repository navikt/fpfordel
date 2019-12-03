package no.nav.foreldrepenger.fordel.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "RelatertYtelseTema")
@DiscriminatorValue(RelatertYtelseTema.DISCRIMINATOR)
public class RelatertYtelseTema extends Kodeliste {

    public static final String DISCRIMINATOR = "RELATERT_YTELSE_TEMA";

    public static final RelatertYtelseTema AA = new RelatertYtelseTema("AA");
    public static final RelatertYtelseTema DAGP = new RelatertYtelseTema("DAGP");

    public static final RelatertYtelseTema FORELDREPENGER_TEMA = new RelatertYtelseTema("FA");
    public static final RelatertYtelseTema ENSLIG_FORSORGER_TEMA = new RelatertYtelseTema("EF");
    public static final RelatertYtelseTema SYKEPENGER_TEMA = new RelatertYtelseTema("SP");

    RelatertYtelseTema() {
    }

    private RelatertYtelseTema(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
