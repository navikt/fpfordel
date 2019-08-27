package no.nav.foreldrepenger.fordel.web.app.exceptions;

import java.io.Serializable;
import java.util.Collection;

public class FeilDto implements Serializable {

    private String feilmelding;
    private Collection<FeltFeilDto> feltFeil;
    private FeilType type;

    public FeilDto(String feilmelding) {
        this.feilmelding = feilmelding;
    }

    public FeilDto(String feilmelding, Collection<FeltFeilDto> feltFeil) {
        this.feilmelding = feilmelding;
        this.feltFeil = feltFeil;
    }

    public FeilDto(FeilType type, String feilmelding) {
        this.type = type;
        this.feilmelding = feilmelding;
    }

    public String getFeilmelding() {
        return feilmelding;
    }

    public Collection<FeltFeilDto> getFeltFeil() {
        return feltFeil;
    }

    public FeilType getType() { return type; }
}
