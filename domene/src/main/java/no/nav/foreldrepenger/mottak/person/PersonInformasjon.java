package no.nav.foreldrepenger.mottak.person;

import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;

public interface PersonInformasjon {

    Optional<String> hentAktørIdForPersonIdent(String id);

    Optional<String> hentPersonIdentForAktørId(String id);

    String hentNavn(BehandlingTema behandlingTema, String id);

}
