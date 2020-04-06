package no.nav.foreldrepenger.mottak.journal;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;

public class ArkivJournalpost {

    private String journalpostId;
    private String brukerAktørId;
    private String kanal;
    private Tema tema;
    private BehandlingTema behandlingstema;
    private String journalfoerendeEnhet;
    private LocalDateTime datoOpprettet;
    private String eksternReferanseId;
    private DokumentTypeId hovedtype = DokumentTypeId.UDEFINERT;
    private Set<DokumentTypeId> alleTyper = new HashSet<>();
    private String dokumentInfoId;
    private String strukturertPayload;


    public ArkivJournalpost() {
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getBrukerAktørId() {
        return brukerAktørId;
    }

    public void setBrukerAktørId(String brukerAktørId) {
        this.brukerAktørId = brukerAktørId;
    }

    public String getKanal() {
        return kanal;
    }

    public void setKanal(String kanal) {
        this.kanal = kanal;
    }

    public Tema getTema() {
        return tema;
    }

    public void setTema(Tema tema) {
        this.tema = tema;
    }

    public BehandlingTema getBehandlingstema() {
        return behandlingstema;
    }

    public void setBehandlingstema(BehandlingTema behandlingstema) {
        this.behandlingstema = behandlingstema;
    }

    public String getJournalfoerendeEnhet() {
        return journalfoerendeEnhet;
    }

    public void setJournalfoerendeEnhet(String journalfoerendeEnhet) {
        this.journalfoerendeEnhet = journalfoerendeEnhet;
    }

    public LocalDateTime getDatoOpprettet() {
        return datoOpprettet;
    }

    public void setDatoOpprettet(LocalDateTime datoOpprettet) {
        this.datoOpprettet = datoOpprettet;
    }

    public String getEksternReferanseId() {
        return eksternReferanseId;
    }

    public void setEksternReferanseId(String eksternReferanseId) {
        this.eksternReferanseId = eksternReferanseId;
    }

    public DokumentTypeId getHovedtype() {
        return hovedtype;
    }

    public void setHovedtype(DokumentTypeId hovedtype) {
        this.hovedtype = hovedtype;
    }

    public Set<DokumentTypeId> getAlleTyper() {
        return alleTyper;
    }

    public void setAlleTyper(Set<DokumentTypeId> alleTyper) {
        this.alleTyper = alleTyper;
    }

    public String getDokumentInfoId() {
        return dokumentInfoId;
    }

    public void setDokumentInfoId(String dokumentInfoId) {
        this.dokumentInfoId = dokumentInfoId;
    }

    public String getStrukturertPayload() {
        return strukturertPayload;
    }

    public void setStrukturertPayload(String strukturertPayload) {
        this.strukturertPayload = strukturertPayload;
    }

    public boolean getInnholderStrukturertInformasjon() {
        return strukturertPayload != null && !strukturertPayload.isEmpty();
    }


    @Override
    public String toString() {
        return "ArkivJournalpost{" +
                "journalpostId='" + journalpostId + '\'' +
                ", kanal='" + kanal + '\'' +
                ", tema=" + tema +
                ", datoOpprettet=" + datoOpprettet +
                ", eksternReferanseId='" + eksternReferanseId + '\'' +
                ", hovedtype=" + hovedtype +
                '}';
    }
}
