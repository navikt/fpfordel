package no.nav.foreldrepenger.mottak.queue;

import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.XMLForsendelsesinformasjon;

public interface MeldingsFordeler {

    void execute(XMLForsendelsesinformasjon forsendelsesinfo);
}
