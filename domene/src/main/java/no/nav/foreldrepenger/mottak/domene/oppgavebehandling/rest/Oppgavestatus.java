package no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Oppgavestatus {
    @JsonEnumDefaultValue
    OPPRETTET,
    AAPNET,
    UNDER_BEHANDLING,
    FERDIGSTILT,
    FEILREGISTRERT;
}