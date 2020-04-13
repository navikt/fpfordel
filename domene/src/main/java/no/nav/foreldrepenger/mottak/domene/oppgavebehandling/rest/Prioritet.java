package no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Prioritet {
    HOY,
    @JsonEnumDefaultValue
    NORM,
    LAV
}
