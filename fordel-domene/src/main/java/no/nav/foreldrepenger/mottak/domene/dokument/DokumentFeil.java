package no.nav.foreldrepenger.mottak.domene.dokument;

import java.util.UUID;

import no.nav.vedtak.exception.TekniskException;

public class DokumentFeil {
    private DokumentFeil() {

    }

    public static TekniskException fantIkkeForsendelse(UUID forsendelseId) {
        return new TekniskException("FP-295614", String.format("Ukjent forsendelseId %s", forsendelseId));
    }

    public static TekniskException fantIkkeUnikResultat() {
        return new TekniskException("FP-302156", "Spørringen returnerte mer enn eksakt ett resultat");

    }

}
