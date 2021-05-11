package no.nav.foreldrepenger.fordel.web.app.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public record DokumentforsendelseDto(@NotNull UUID forsendelsesId, String saksnummer, String brukerId, LocalDateTime forsendelseMottatt,
        @NotNull @Size(min = 1) List<FilMetadataDto> filer) {
}

record FilMetadataDto(@NotNull @Pattern(regexp = "^[a-zA-Z0-9]{4}$") String contentId,
        @NotNull @Size(min = 7, max = 7) @Pattern(regexp = "^I[0-9]{6}$") String dokumentTypeId) {

}