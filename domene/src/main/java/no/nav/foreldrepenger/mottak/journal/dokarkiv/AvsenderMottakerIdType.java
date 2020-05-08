package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum AvsenderMottakerIdType {
    @JsonEnumDefaultValue
    UKJENT,
    FNR,
    ORGNR
}
