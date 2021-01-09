package no.nav.foreldrepenger.mottak.journal.saf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Bruker(String id, BrukerIdType type) {

    public boolean erAktoerId() {
        return BrukerIdType.AKTOERID.equals(type);
    }
}
