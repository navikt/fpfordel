package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

public class DokumentIdDto {

    @Digits(integer = 18, fraction = 0)
    private String dokumentId;

    public DokumentIdDto(String dokumentId) {
        this.dokumentId = dokumentId;
    }

    public DokumentIdDto() {  // For Jackson
    }

    public String getDokumentId() {
        return dokumentId;
    }
}
