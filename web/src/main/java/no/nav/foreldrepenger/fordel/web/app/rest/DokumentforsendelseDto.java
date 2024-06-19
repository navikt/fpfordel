package no.nav.foreldrepenger.fordel.web.app.rest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DokumentforsendelseDto {
    @NotNull
    private UUID forsendelsesId;
    @NotNull
    @Size(min = 1)
    private List<FilMetadataDto> filer;

    private String saksnummer;

    private LocalDateTime forsendelseMottatt;

    private String brukerId;

    public DokumentforsendelseDto(UUID forsendelsesId, List<FilMetadataDto> filer) {
        this.forsendelsesId = forsendelsesId;
        this.filer = filer;
    }

    DokumentforsendelseDto() { // NOSONAR ... is marked "javax.validation.constraints.NotNull" but is not
        // initialized in this constructor.
        // For Jackson
    }

    public UUID getForsendelsesId() {
        return forsendelsesId;
    }

    public List<FilMetadataDto> getFiler() {
        return filer;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public LocalDateTime getForsendelseMottatt() {
        return forsendelseMottatt;
    }

    public DokumentforsendelseDto setForsendelseMottatt(LocalDateTime forsendelseMottatt) {
        this.forsendelseMottatt = forsendelseMottatt;
        return this;
    }

    public String getBrukerId() {
        return brukerId;
    }

    public DokumentforsendelseDto setBrukerId(String brukerId) {
        this.brukerId = brukerId;
        return this;
    }
}

class FilMetadataDto {
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]{4}$")
    @JsonProperty("Content-ID")
    private String contentId;
    @NotNull
    @Size(min = 7, max = 7)
    @Pattern(regexp = "^I[0-9]{6}$")
    private String dokumentTypeId;

    public FilMetadataDto(String contentId, String dokumentTypeId) {
        this.contentId = contentId;
        this.dokumentTypeId = dokumentTypeId;
    }

    FilMetadataDto() { // NOSONAR ... is marked "javax.validation.constraints.NotNull" but is not
        // initialized in this constructor.
        // For Jackson
    }

    public String getContentId() {
        return contentId;
    }

    public String getDokumentTypeId() {
        return dokumentTypeId;
    }
}
