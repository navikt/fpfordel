package no.nav.foreldrepenger.fordel.web.app.exceptions;

public record FeltFeilDto(String navn, String melding, String metainformasjon) {

    public FeltFeilDto(String navn, String melding) {
        this(navn, melding, null);
    }
}
