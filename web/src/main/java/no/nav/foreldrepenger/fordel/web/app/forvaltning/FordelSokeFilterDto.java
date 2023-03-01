package no.nav.foreldrepenger.fordel.web.app.forvaltning;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE, fieldVisibility = Visibility.ANY)
@Schema
public class FordelSokeFilterDto implements AbacDto {

    @JsonProperty(value = "tekst")
    @NotNull
    @Valid
    @Size(min = 5, max = 200)
    @Pattern(regexp = "^[\\p{Alnum}_.=\\-]*$")
    private String tekst;

    @JsonProperty(value = "sisteKjoeretidspunktFraOgMed")
    @Valid
    private LocalDate opprettetFraOgMed = LocalDate.now().minusMonths(12);

    @JsonProperty(value = "sisteKjoeretidspunktTilOgMed")
    @Valid
    private LocalDate opprettetTilOgMed = LocalDate.now();

    public FordelSokeFilterDto() {
        // Jackson
    }

    @Schema(description = "Søketekst")
    public String getTekst() {
        return tekst;
    }

    public void setTekst(String tekst) {
        this.tekst = tekst;
    }

    @Schema(description = "Søker etter prosesstask med siste kjøring fra og med dette tidspunktet")
    public LocalDate getOpprettetFraOgMed() {
        return opprettetFraOgMed;
    }

    public void setOpprettetFraOgMed(LocalDate opprettetFraOgMed) {
        this.opprettetFraOgMed = opprettetFraOgMed;
    }

    @Schema(description = "Søker etter prosesstask med siste kjøring til og med dette tidspunktet")
    public LocalDate getOpprettetTilOgMed() {
        return opprettetTilOgMed;
    }

    public void setOpprettetTilOgMed(LocalDate opprettetTilOgMed) {
        this.opprettetTilOgMed = opprettetTilOgMed;
    }


    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }
}
