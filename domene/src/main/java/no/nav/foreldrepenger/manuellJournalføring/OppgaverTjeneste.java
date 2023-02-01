package no.nav.foreldrepenger.manuellJournalføring;

import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.List;

@ApplicationScoped
public class OppgaverTjeneste {
    private Oppgaver oppgaver;

    public OppgaverTjeneste() {
        // CDI
    }

    @Inject
    public OppgaverTjeneste(Oppgaver oppgaver) {
        this.oppgaver = oppgaver;
    }

    public List<Oppgave> hentJournalføringsOppgaver() throws Exception {
        return oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), null);
    }
}
