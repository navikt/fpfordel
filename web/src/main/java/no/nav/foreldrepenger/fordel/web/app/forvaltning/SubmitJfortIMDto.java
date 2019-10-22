package no.nav.foreldrepenger.fordel.web.app.forvaltning;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.sikkerhet.abac.AppAbacAttributtType;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskIdDto;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

@ApiModel(value = "Input til prosesstask før submit VL")
public class SubmitJfortIMDto implements AbacDto {

    @ApiModelProperty(value = "taskID")
    @NotNull
    @Valid
    private ProsessTaskIdDto prosessTaskIdDto;
    
    @ApiModelProperty(value = "saksnummer")
    @NotNull
    @Valid
    private SaksnummerDto saksnummerDto;
    
    @ApiModelProperty(value = "journalpostID")
    @NotNull
    @Valid
    private JournalpostIdDto journalpostIdDto;


    public SubmitJfortIMDto() { // NOSONAR Input-dto, ingen behov for initialisering
    	// for Jackson
    }

    public SubmitJfortIMDto(ProsessTaskIdDto prosessTaskIdDto, SaksnummerDto saksnummerDto, JournalpostIdDto journalpostIdDto) {
        this.prosessTaskIdDto = prosessTaskIdDto;
        this.saksnummerDto = saksnummerDto;
        this.journalpostIdDto = journalpostIdDto;
    }

    public ProsessTaskIdDto getProsessTaskIdDto() {
        return prosessTaskIdDto;
    }

    public void setProsessTaskIdDto(ProsessTaskIdDto prosessTaskIdDto) {
        this.prosessTaskIdDto = prosessTaskIdDto;
    }

    public SaksnummerDto getSaksnummerDto() {
        return saksnummerDto;
    }

    public void setSaksnummerDto(SaksnummerDto saksnummerDto) {
        this.saksnummerDto = saksnummerDto;
    }

    public JournalpostIdDto getJournalpostIdDto() {
        return journalpostIdDto;
    }

    public void setJournalpostIdDto(JournalpostIdDto journalpostIdDto) {
        this.journalpostIdDto = journalpostIdDto;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett()
                .leggTil(AppAbacAttributtType.SAKSNUMMER, saksnummerDto.getSaksnummer())
                .leggTil(AppAbacAttributtType.JOURNALPOST_ID, journalpostIdDto.getJournalpostId());

    }

}