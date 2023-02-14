package no.nav.foreldrepenger.mottak.journal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.Journalpost;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Tilleggsopplysning;

public class ArkivJournalpost {

    private Journalpost original;
    private String journalpostId;
    private String brukerAktørId;
    private String avsenderIdent;
    private String avsenderNavn;
    private String kanal;
    private Journalstatus tilstand;
    private Journalposttype journalposttype;
    private Tema tema;
    private BehandlingTema behandlingstema;
    private BehandlingTema utledetBehandlingstema;
    private String saksnummer;
    private String journalfoerendeEnhet;
    private LocalDateTime datoOpprettet;
    private String eksternReferanseId;
    private DokumentTypeId hovedtype = DokumentTypeId.UDEFINERT;
    private List<Tilleggsopplysning> tilleggsopplysninger = new ArrayList<>();
    private Set<DokumentTypeId> alleTyper = new HashSet<>();
    private String dokumentInfoId;
    private String strukturertPayload;
    private String beskrivelse;

    public ArkivJournalpost() {
    }

    public Journalpost getOriginalJournalpost() {
        return original;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public Optional<String> getTittel() {
        return Optional.ofNullable(original)
                .map(Journalpost::tittel);
    }

    public Optional<String> getSaksnummer() {
        return Optional.ofNullable(saksnummer);
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

    public String getAvsenderIdent() {
        return avsenderIdent;
    }

    public String getAvsenderNavn() {
        return avsenderNavn;
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

    public BehandlingTema getUtledetBehandlingstema() {
        return utledetBehandlingstema;
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

    public List<Tilleggsopplysning> getTilleggsopplysninger() {
        return tilleggsopplysninger;
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
        return (strukturertPayload != null) && !strukturertPayload.isEmpty();
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public boolean harBrevkodeCrm() {
        return Optional.ofNullable(original)
            .map(Journalpost::dokumenter).orElse(List.of()).stream()
            .map(DokumentInfo::brevkode)
            .filter(Objects::nonNull)
            .anyMatch(bk -> bk.startsWith("CRM"));
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

        public Builder medJournalpost(Journalpost journalpost) {
            ajp.original = journalpost;
            return this;
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

        public Builder medAvsender(String id, String navn) {
            ajp.avsenderIdent = id;
            ajp.avsenderNavn = navn;
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

        public Builder medUtledetBehandlingstema(BehandlingTema behandlingstema) {
            ajp.utledetBehandlingstema = behandlingstema;
            return this;
        }

        public Builder medJournalfoerendeEnhet(String journalfoerendeEnhet) {
            ajp.journalfoerendeEnhet = journalfoerendeEnhet;
            return this;
        }

        public Builder medSaksnummer(String saksnummer) {
            ajp.saksnummer = saksnummer;
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

        public Builder medTilleggsopplysninger(List<Tilleggsopplysning> tilleggsopplysninger) {
            if (tilleggsopplysninger != null)
                ajp.tilleggsopplysninger.addAll(tilleggsopplysninger);
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

        public Builder medBeskrivelse(String beskrivelse) {
            ajp.beskrivelse = beskrivelse;
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
