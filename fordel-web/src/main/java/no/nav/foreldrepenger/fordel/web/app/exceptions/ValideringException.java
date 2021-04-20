package no.nav.foreldrepenger.fordel.web.app.exceptions;

import java.util.Collection;

public class ValideringException extends RuntimeException {
    private final Collection<FeltFeilDto> feltFeil;

    public ValideringException(Collection<FeltFeilDto> feltfeil) {
        this.feltFeil = feltfeil;
    }

    public Collection<FeltFeilDto> getFeltFeil() {
        return feltFeil;
    }

}
