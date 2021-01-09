package no.nav.foreldrepenger.mottak.klient;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;

public interface JournalpostSender {

    void send(JournalpostMottakDto journalpostMottakDto);

}
