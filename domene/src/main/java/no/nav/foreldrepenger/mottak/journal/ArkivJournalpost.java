package no.nav.foreldrepenger.mottak.journal;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;

public class ArkivJournalpost {

    private String journalpostId;
    private String brukerAktørId;
    private String kanal;
    private Journalstatus tilstand;
    private Journalposttype journalposttype;
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

    public Journalstatus getTilstand() {
        return tilstand;
    }

    public Journalposttype getJournalposttype() {
        return journalposttype;
    }

    public Optional<String> getBrukerAktørId() {
        return Optional.ofNullable(brukerAktørId);
    }

    public String getKanal() {
        return kanal;
    }

    public Tema getTema() {
        return tema;
    }

    public BehandlingTema getBehandlingstema() {
        return behandlingstema;
    }

    public Optional<String> getJournalfoerendeEnhet() {
        return Optional.ofNullable(journalfoerendeEnhet);
    }

    public LocalDateTime getDatoOpprettet() {
        return datoOpprettet;
    }

    public String getEksternReferanseId() {
        return eksternReferanseId;
    }

    public DokumentTypeId getHovedtype() {
        return hovedtype;
    }

    public Set<DokumentTypeId> getAlleTyper() {
        return alleTyper;
    }

    public Optional<String> getDokumentInfoId() {
        return Optional.ofNullable(dokumentInfoId);
    }

    public String getStrukturertPayload() {
        return strukturertPayload;
    }

    public boolean getInnholderStrukturertInformasjon() {
        return strukturertPayload != null && !strukturertPayload.isEmpty();
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private ArkivJournalpost ajp;

        Builder() {
            ajp = new ArkivJournalpost();
            ajp.alleTyper = new HashSet<>();
            ajp.behandlingstema = BehandlingTema.UDEFINERT;
        }

        public Builder medJournalpostId(String journalpostId) {
            ajp.journalpostId = journalpostId;
            return this;
        }

        public Builder medJournalposttype(Journalposttype type) {
            ajp.journalposttype = type;
            return this;
        }

        public Builder medTilstand(Journalstatus tilstand) {
            ajp.tilstand = tilstand;
            return this;
        }

        public Builder medBrukerAktørId(String brukerAktørId) {
            ajp.brukerAktørId = brukerAktørId;
            return this;
        }

        public Builder medKanal(String kanal) {
            ajp.kanal = kanal;
            return this;
        }

        public Builder medTema(Tema tema) {
            ajp.tema = tema;
            return this;
        }

        public Builder medBehandlingstema(BehandlingTema behandlingstema) {
            ajp.behandlingstema = behandlingstema;
            return this;
        }

        public Builder medJournalfoerendeEnhet(String journalfoerendeEnhet) {
            ajp.journalfoerendeEnhet = journalfoerendeEnhet;
            return this;
        }

        public Builder medDatoOpprettet(LocalDateTime datoOpprettet) {
            ajp.datoOpprettet = datoOpprettet;
            return this;
        }

        public Builder medEksternReferanseId(String eksternReferanseId) {
            ajp.eksternReferanseId = eksternReferanseId;
            return this;
        }

        public Builder medHovedtype(DokumentTypeId hovedtype) {
            ajp.hovedtype = hovedtype;
            return this;
        }

        public Builder medAlleTyper(Set<DokumentTypeId> alleTyper) {
            ajp.alleTyper = alleTyper;
            return this;
        }

        public Builder medDokumentInfoId(String dokumentInfoId) {
            ajp.dokumentInfoId = dokumentInfoId;
            return this;
        }

        public Builder medStrukturertPayload(String strukturertPayload) {
            ajp.strukturertPayload = strukturertPayload;
            return this;
        }

        public ArkivJournalpost build() {
            Objects.requireNonNull(ajp.journalpostId, "journalpostId");
            Objects.requireNonNull(ajp.hovedtype, "dokumentTypeId");
            Objects.requireNonNull(ajp.tema, "tema");
            Objects.requireNonNull(ajp.tilstand, "tilstand");
            return ajp;
        }
    }

    @Override
    public String toString() {
        return "ArkivJournalpost{" +
                "journalpostId='" + journalpostId + '\'' +
                ", kanal='" + kanal + '\'' +
                ", tilstand=" + tilstand +
                ", journalposttype=" + journalposttype +
                ", tema=" + tema +
                ", behandlingstema=" + behandlingstema +
                ", datoOpprettet=" + datoOpprettet +
                ", eksternReferanseId='" + eksternReferanseId + '\'' +
                ", hovedtype=" + hovedtype +
                '}';
    }
}
