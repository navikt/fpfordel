package no.nav.foreldrepenger.mottak.journal.saf.model;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum AvsenderMottakerIdType {
    @JsonEnumDefaultValue
    UKJENT,
    FNR,
    ORGNR
}
