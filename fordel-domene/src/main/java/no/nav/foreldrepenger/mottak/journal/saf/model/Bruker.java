package no.nav.foreldrepenger.mottak.journal.saf.model;

public record Bruker(String id, BrukerIdType type) {

    public boolean erAktoerId() {
        return BrukerIdType.AKTOERID.equals(type);
    }
}
