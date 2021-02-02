package no.nav.foreldrepenger.mottak.journal.saf.model;

import java.util.Optional;

public record AvsenderMottaker(String id,
        String type,
        String navn) {

    public Optional<String> getIdHvisFNR() {
        return "FNR".equalsIgnoreCase(type) ? Optional.ofNullable(id) : Optional.empty();
    }

}
