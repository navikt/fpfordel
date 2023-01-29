package no.nav.foreldrepenger.journalføring;

import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class OppgaverTjeneste {
    private Oppgaver oppgaver;

    private ArkivTjeneste arkiv;

    public OppgaverTjeneste() {
        // CDI
    }

    @Inject
    public OppgaverTjeneste(Oppgaver oppgaver, ArkivTjeneste arkiv) {
        this.oppgaver = oppgaver;
        this.arkiv = arkiv;
    }

    public List<Oppgave> hentJournalføringsOppgaver() throws Exception {
        return oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), null);
    }

    public ArkivJournalpost hentJournalpostDetaljer(String journalpostId) {
        return arkiv.hentArkivJournalpost(journalpostId);
    }
}
