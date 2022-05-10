package no.nav.foreldrepenger.mottak.person;

import java.util.Optional;

public interface PersonInformasjon {

    Optional<String> hentAktørIdForPersonIdent(String id);

    Optional<String> hentPersonIdentForAktørId(String id);

    String hentNavn(String id);

    String hentGeografiskTilknytning(String aktørId);

    boolean harStrengDiskresjonskode(String aktørId);

}
