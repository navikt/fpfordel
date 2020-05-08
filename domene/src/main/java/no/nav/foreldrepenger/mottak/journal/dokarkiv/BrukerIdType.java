package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum BrukerIdType {
    @JsonEnumDefaultValue
    UKJENT,
    AKTOERID,
    FNR,
    ORGNR
}
