package no.nav.foreldrepenger.fordel.web.app.rest;

import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;

import io.swagger.v3.oas.annotations.Parameter;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class SjekkJournalpostRequest implements AbacDto {

    @NotNull
    @QueryParam("journalpostId")
    @Parameter(name = "journalpostId", description = "Om oppgaven er tildelt en ressurs eller ikke", example = "false")
    private String journalpostId;

    @QueryParam("oppgittBt")
    @Parameter(name = "oppgittBt", description = "Om oppgaven er tildelt en ressurs eller ikke", example = "false")
    private String oppgitt;

    @QueryParam("aktivesakerBt")
    @Parameter(name = "aktivesakerBt", description = "Om oppgaven er tildelt en ressurs eller ikke", example = "false")
    private List<String> aktivesaker;

    public SjekkJournalpostRequest() {
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getOppgitt() {
        return oppgitt;
    }

    public void setOppgitt(String oppgitt) {
        this.oppgitt = oppgitt;
    }

    public List<String> getAktivesaker() {
        return aktivesaker == null ? Collections.emptyList() : aktivesaker;
    }

    public void setAktivesaker(List<String> aktivesaker) {
        this.aktivesaker = aktivesaker;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett();
    }

}