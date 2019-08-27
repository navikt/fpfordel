package no.nav.foreldrepenger.fordel.web.app.forvaltning;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskIdDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

@ApiModel(value = "Input til prosesstask før restart")
public class RetryTaskKanalrefDto implements AbacDto {

    @ApiModelProperty(value = "taskID")
    @NotNull
    @Valid
    private ProsessTaskIdDto prosessTaskIdDto;
    @ApiModelProperty(value = "suffix")
    @NotNull
    @Size(max = 8)
    @Pattern(regexp = "^[a-zA-ZæøåÆØÅ_\\-0-9]*$")
    private String retrySuffix;


    public RetryTaskKanalrefDto() { // NOSONAR Input-dto, ingen behov for initialisering
    }

    public RetryTaskKanalrefDto(ProsessTaskIdDto prosessTaskIdDto, String retrySuffix) {
        this.prosessTaskIdDto = prosessTaskIdDto;
        this.retrySuffix = retrySuffix;
    }

    public ProsessTaskIdDto getProsessTaskIdDto() {
        return prosessTaskIdDto;
    }

    public void setProsessTaskIdDto(ProsessTaskIdDto prosessTaskIdDto) {
        this.prosessTaskIdDto = prosessTaskIdDto;
    }

    public String getRetrySuffix() {
        return retrySuffix;
    }

    public void setRetrySuffix(String retrySuffix) {
        this.retrySuffix = retrySuffix;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }

}