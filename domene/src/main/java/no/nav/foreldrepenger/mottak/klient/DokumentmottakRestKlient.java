package no.nav.foreldrepenger.mottak.klient;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;

public interface DokumentmottakRestKlient {
    void send(JournalpostMottakDto journalpostMottakDto);
}
