package no.nav.foreldrepenger.mottak.infotrygd;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class InfotrygdUgyldigInputException extends IntegrasjonException {
    public InfotrygdUgyldigInputException(Feil feil) {
        super(feil);
    }
}
