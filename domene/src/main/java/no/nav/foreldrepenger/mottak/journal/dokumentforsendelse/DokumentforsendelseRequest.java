package no.nav.foreldrepenger.mottak.journal.dokumentforsendelse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;

public class DokumentforsendelseRequest {
    private Boolean forsøkEndeligJF;
    private String bruker;
    private String avsender;
    private String forsendelseId;
    private LocalDateTime forsendelseMottatt;
    private String tittel;
    private String saksnummer;
    private String retrying;
    private List<Dokument> hoveddokument;
    private List<Dokument> vedlegg;

    private DokumentforsendelseRequest() {
    }

    public Boolean getForsøkEndeligJF() {
        return forsøkEndeligJF;
    }

    public String getBruker() {
        return bruker;
    }

    public Optional<String> getAvsender() {
        return Optional.ofNullable(avsender);
    }

    public String getForsendelseId() {
        return forsendelseId;
    }

    public LocalDateTime getForsendelseMottatt() {
        return forsendelseMottatt;
    }

    public String getTittel() {
        return tittel;
    }

    public String getSaksnummer() {
        return saksnummer;
    }

    public boolean isRetrying() {
        return retrying != null;
    }

    public Optional<String> getRetrySuffix() {
        return Optional.ofNullable(retrying);
    }

    public List<Dokument> getHoveddokument() {
        return hoveddokument;
    }

    public List<Dokument> getVedlegg() {
        return vedlegg;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public static final Integer TITTEL_MAKS_LENGDE = 500;
        private Boolean forsøkEndeligJF;
        private String bruker;
        private String avsender;
        private String forsendelseId;
        private LocalDateTime forsendelseMottatt;
        private String saksnummer;
        private String tittel;
        private String retrying;
        private List<Dokument> hoveddokument;
        private List<Dokument> vedleggListe;

        public Builder() {
            this.hoveddokument = new ArrayList<>();
            this.vedleggListe = new ArrayList<>();
        }

        public Builder medForsøkEndeligJF(Boolean forsøkEndeligJF) {
            this.forsøkEndeligJF = forsøkEndeligJF;
            return this;
        }

        public Builder medBruker(String bruker) {
            this.bruker = bruker;
            return this;
        }

        public Builder medAvsender(String avsender) {
            this.avsender = avsender;
            return this;
        }

        public Builder medForsendelseId(String forsendelseId) {
            this.forsendelseId = forsendelseId;
            return this;
        }

        public Builder medForsendelseMottatt(LocalDateTime forsendelseMottatt) {
            this.forsendelseMottatt = forsendelseMottatt;
            return this;
        }

        public Builder medSaksnummer(String saksnummer) {
            this.saksnummer = saksnummer;
            return this;
        }

        public Builder medRetrySuffix(String suffix) {
            this.retrying = suffix;
            return this;
        }

        public Builder medHoveddokument(List<Dokument> hoveddokument) {
            this.hoveddokument = hoveddokument;
            return this;
        }

        public Builder medVedlegg(List<Dokument> vedleggListe) {
            this.vedleggListe = vedleggListe;
            this.setTittel(vedleggListe);
            return this;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(this.forsøkEndeligJF);
            Objects.requireNonNull(this.bruker);
            Objects.requireNonNull(this.forsendelseId);
            Objects.requireNonNull(this.forsendelseMottatt);
            if (this.hoveddokument.isEmpty() && this.vedleggListe.isEmpty()) {
                throw new IllegalStateException("må ha minst ett dokument (hoveddokument eller vedlegg)");
            }
            if (this.tittel == null && this.hoveddokument.isEmpty()) {
                throw new IllegalStateException("tittel ikke laget");
            }
        }

        public DokumentforsendelseRequest build() {
            verifyStateForBuild();
            DokumentforsendelseRequest request = new DokumentforsendelseRequest();
            request.forsøkEndeligJF = this.forsøkEndeligJF;
            request.bruker = this.bruker;
            request.avsender = this.avsender;
            request.forsendelseId = this.forsendelseId;
            request.forsendelseMottatt = this.forsendelseMottatt;
            request.saksnummer = this.saksnummer;
            request.retrying = this.retrying;
            if (hoveddokument.isEmpty()) {
                request.tittel = this.tittel;
            }
            request.hoveddokument = this.hoveddokument;
            request.vedlegg = this.vedleggListe;
            return request;
        }

        private void setTittel(List<Dokument> vedleggListe) {
            Map<DokumentTypeId, MutableInt> map = new HashMap<>();
            for (Dokument vedlegg : vedleggListe) {
                MutableInt count = map.get(vedlegg.getDokumentTypeId());
                if (count == null) {
                    map.put(vedlegg.getDokumentTypeId(), new MutableInt());
                } else {
                    count.increment();
                }
            }
            StringBuilder sb = new StringBuilder("Ettersendelse: ");
            sb.append(mapToString(map));
            // trunker tittelen dersom den er over 500 tegn
            if (sb.length() > TITTEL_MAKS_LENGDE) {
                sb.setLength(TITTEL_MAKS_LENGDE);
                sb.trimToSize();
                sb.replace(sb.length() - 3, sb.length(), "...");
            }
            this.tittel = sb.toString();
        }

        private String mapToString(Map<DokumentTypeId, MutableInt> map) {
            StringBuilder sb = new StringBuilder();
            Integer max = map.size();
            Integer i = 0;
            for (Map.Entry<DokumentTypeId, MutableInt> m : map.entrySet()) {
                sb.append(m.getKey().getKode());
                sb.append(" (");
                sb.append(m.getValue().get());
                sb.append("x)");
                if (++i < max) {
                    sb.append(", ");
                }
            }
            return sb.toString();
        }

        static class MutableInt {
            int value = 1;
            public void increment() {
                ++value;
            }

            public int get() {
                return value;
            }
        }
    }
}
