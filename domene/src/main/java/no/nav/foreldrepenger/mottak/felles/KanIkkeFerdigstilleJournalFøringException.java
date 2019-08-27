package no.nav.foreldrepenger.mottak.felles;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;

public class KanIkkeFerdigstilleJournalFøringException extends IntegrasjonException {
    public KanIkkeFerdigstilleJournalFøringException(Feil feil) {
        super(feil);
    }
}
