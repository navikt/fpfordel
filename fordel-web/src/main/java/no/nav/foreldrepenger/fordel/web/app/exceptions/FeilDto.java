package no.nav.foreldrepenger.fordel.web.app.exceptions;

import java.util.Collection;
import java.util.List;

public record FeilDto(String feilmelding, FeilType type, Collection<FeltFeilDto> feltFeil) {
    public FeilDto(String feilmelding, FeilType type) {
        this(feilmelding, type, List.of());
    }

    public FeilDto(String feilmelding, Collection<FeltFeilDto> feltFeil) {
        this(feilmelding, null, feltFeil);
    }

    public FeilDto(String feilmelding) {
        this(feilmelding, List.of());
    }
}
