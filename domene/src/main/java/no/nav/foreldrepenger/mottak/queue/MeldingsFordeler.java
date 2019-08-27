package no.nav.foreldrepenger.mottak.queue;

import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.Forsendelsesinformasjon;

public interface MeldingsFordeler {

    void execute(Forsendelsesinformasjon forsendelsesinfo);
}
