package no.nav.foreldrepenger.fordel.web.app.exceptions;

import java.util.Collection;

public class Valideringsfeil extends RuntimeException {
    private final Collection<FeltFeilDto> feltFeil;

    public Valideringsfeil(Collection<FeltFeilDto> feltfeil) {
        this.feltFeil = feltfeil;
    }

    public Collection<FeltFeilDto> getFeltFeil() {
        return feltFeil;
    }

}
