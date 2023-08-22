package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import jakarta.validation.constraints.Digits;

public record DokumentIdDto(@Digits(integer = 18, fraction = 0) String dokumentId) {
}
