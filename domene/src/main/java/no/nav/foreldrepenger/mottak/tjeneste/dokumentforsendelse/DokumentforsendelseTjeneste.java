package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;

public interface DokumentforsendelseTjeneste {
    Duration POLL_INTERVALL = Duration.ofSeconds(1);

    void nyDokumentforsendelse(DokumentMetadata metadata);

    void lagreDokument(Dokument dokument);

    void validerDokumentforsendelse(UUID forsendelsesId);

    ForsendelseStatusDto finnStatusinformasjon(UUID forsendelseId);

    Optional<ForsendelseStatusDto> finnStatusinformasjonHvisEksisterer(UUID forsendelseId);
}
