package no.nav.foreldrepenger.mottak.infotrygd;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class InfotrygdPersonIkkeFunnetException extends IntegrasjonException {
    public InfotrygdPersonIkkeFunnetException(Feil feil) {
        super(feil);
    }
}
