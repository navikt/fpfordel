package no.nav.foreldrepenger.fordel.web.app.rest;

import java.util.Collections;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Parameter;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class SjekkJournalpostRequest implements AbacDto {

    @NotNull
    @Size(max = 18)
    @Pattern(regexp = "^[a-zA-ZæøåÆØÅ_\\-0-9]*$")
    @QueryParam("journalpostId")
    @Parameter(name = "journalpostId", description = "Journalpost ID", example = "false")
    private String journalpostId;

    @Size(max = 8)
    @Pattern(regexp = "^[a-zA-ZæøåÆØÅ_\\-0-9]*$")
    @QueryParam("oppgittBt")
    @Parameter(name = "oppgittBt", description = "Oppgitt behandlingstema", example = "false")
    private String oppgittBt;

    @Valid
    @Size(max = 8)
    @Pattern(regexp = "^[a-zA-ZæøåÆØÅ_\\-0-9]*$")
    @QueryParam("aktivesakerBt")
    @Parameter(name = "aktivesakerBt", description = "Behandlingstema for aktive saker ", example = "false")
    private List<String> aktivesakerBt;

    public SjekkJournalpostRequest() {
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getOppgittBt() {
        return oppgittBt;
    }

    public void setOppgittBt(String oppgittBt) {
        this.oppgittBt = oppgittBt;
    }

    public List<String> getAktivesakerBt() {
        return aktivesakerBt == null ? Collections.emptyList() : aktivesakerBt;
    }

    public void setAktivesakerBt(List<String> aktivesakerBt) {
        this.aktivesakerBt = aktivesakerBt;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }

}