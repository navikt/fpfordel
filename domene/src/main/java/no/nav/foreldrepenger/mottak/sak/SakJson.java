package no.nav.foreldrepenger.mottak.sak;

import java.util.Objects;

public record SakJson(Long id, String tema, String applikasjon, String aktoerId, String fagsakNr) {

    @Override
    public int hashCode() {
        return Objects.hash(aktoerId, applikasjon, fagsakNr, id, tema);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (SakJson) o;
        return id == that.id && tema.equals(that.tema) && applikasjon.equals(that.applikasjon) && aktoerId.equals(that.aktoerId) && fagsakNr.equals(
            that.fagsakNr);

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [journalpostId=" + id + ", tema=" + tema + ", applikasjon=" + applikasjon + ", fagsakNr=" + fagsakNr + "]";
    }

}
