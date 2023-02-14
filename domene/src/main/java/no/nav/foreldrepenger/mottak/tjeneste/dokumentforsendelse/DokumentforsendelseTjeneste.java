package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DokumentforsendelseTjeneste {
    Duration POLL_INTERVALL = Duration.ofSeconds(1);

    void lagreForsendelseValider(DokumentMetadata metadata, List<Dokument> dokumenter);

    ForsendelseStatusDto finnStatusinformasjon(UUID forsendelseId);

    Optional<ForsendelseStatusDto> finnStatusinformasjonHvisEksisterer(UUID forsendelseId);
}
