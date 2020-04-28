package no.nav.foreldrepenger.mottak.domene.dokument;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;

public class ConstraintException extends TekniskException {

    public ConstraintException(Feil feil) {
        super(feil);
    }

}
