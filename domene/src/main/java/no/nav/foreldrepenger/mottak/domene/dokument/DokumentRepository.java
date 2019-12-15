package no.nav.foreldrepenger.mottak.domene.dokument;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;

public interface DokumentRepository {
    void lagre(Dokument dokument);

    void lagre(DokumentMetadata dokumentMetadata);

    Optional<Dokument> hentUnikDokument(UUID forsendelseId, boolean hovedDokument, ArkivFilType contentType);

    List<Dokument> hentDokumenter(UUID forsendelsesId);

    DokumentMetadata hentEksaktDokumentMetadata(UUID forsendelseId);

    Optional<DokumentMetadata> hentUnikDokumentMetadata(UUID forsendelseId);

    void slettForsendelse(UUID forsendelseId);

    void oppdaterForseldelseMedArkivId(UUID forsendelseId, String arkivId, ForsendelseStatus status);

    void oppdaterForsendelseMetadata(UUID forsendelseId, String arkivId, String saksnummer, ForsendelseStatus status);
}
