package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;

public record FilMetadata(String contentId,
                          DokumentTypeId dokumentTypeId) {

}
