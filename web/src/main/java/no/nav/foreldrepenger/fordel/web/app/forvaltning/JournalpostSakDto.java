package no.nav.foreldrepenger.fordel.web.app.forvaltning;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

//@ApiModel(value = "Input til prosesstask før submit VL")
public record JournalpostSakDto(@Schema @NotNull @Valid SaksnummerDto saksnummerDto,
                                @Schema @NotNull @Valid JournalpostIdDto journalpostIdDto) implements AbacDto {

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();

    }

}
