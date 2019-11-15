package no.nav.foreldrepenger.mottak.infotrygd.rest;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "sakId", "type", "status", "resultat", "iverksatt", "vedtatt" })
public class Sak {

    private final LocalDate iverksatt;
    private final SakResultat resultat;
    private final Saksnummer sakId;
    private final SakStatus status;
    private final SakType type;
    private final LocalDate vedtatt;

    @JsonCreator
    public Sak(
            @JsonProperty("iverksatt") LocalDate iverksatt,
            @JsonProperty("resultat") SakResultat resultat,
            @JsonProperty("sakId") Saksnummer sakId,
            @JsonProperty("status") SakStatus status,
            @JsonProperty("type") SakType type,
            @JsonProperty("vedtatt") LocalDate vedtatt) {
        this.iverksatt = iverksatt;
        this.resultat = resultat;
        this.sakId = sakId;
        this.status = status;
        this.type = type;
        this.vedtatt = vedtatt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iverksatt, resultat, sakId, status, type, vedtatt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Sak)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Sak that = (Sak) obj;
        return Objects.equals(that.iverksatt, this.iverksatt) &&
                Objects.equals(that.resultat, this.resultat) &&
                Objects.equals(that.sakId, this.sakId) &&
                Objects.equals(that.status, this.status) &&
                Objects.equals(that.type, this.type) &&
                Objects.equals(that.vedtatt, this.vedtatt);
    }

    public LocalDate getIverksatt() {
        return iverksatt;
    }

    public SakResultat getResultat() {
        return resultat;
    }

    public Saksnummer getSakId() {
        return sakId;
    }

    public SakStatus getStatus() {
        return status;
    }

    public SakType getType() {
        return type;
    }

    public LocalDate getVedtatt() {
        return vedtatt;
    }

    @JsonIgnore
    public String getSaksnummer() {
        return getSakId().getBlokk() + nrFra(getSakId().getNr());
    }

    private static String nrFra(int nr) {
        return nr < 10 ? "0" + nr : String.valueOf(nr);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[iverksatt=" + iverksatt + ", resultat=" + resultat + ", saksnummer="
                + getSaksnummer() + ", status=" + status + ", type=" + type + ", vedtatt=" + vedtatt + "]";
    }

}
