package no.nav.foreldrepenger.mottak.infotrygd;

import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseTema.ENSLIG_FORSORGER_TEMA;
import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseTema.FORELDREPENGER_TEMA;
import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseTema.SYKEPENGER_TEMA;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseBehandlingstema;

public class InfotrygdSak {
    private final String tema;
    private final String behandlingsTema;
    private final String sakId;
    private final LocalDate iverksatt;
    private final LocalDate registrert;

    public InfotrygdSak(String sakId, String tema, String behandlingsTema, LocalDate iverksatt, LocalDate registrert) { // NOSONAR
        this.sakId = sakId;
        this.tema = tema;
        this.behandlingsTema = behandlingsTema;
        this.iverksatt = iverksatt;
        this.registrert = registrert;
    }

    public String getSakId() {
        return sakId;
    }

    public String getTema() {
        return tema;
    }

    public String getBehandlingsTema() {
        return behandlingsTema;
    }

    public Optional<LocalDate> getIverksatt() {
        return Optional.ofNullable(iverksatt);
    }

    public LocalDate getRegistrert() {
        return registrert;
    }

    public boolean gjelderEnsligForsorger() {
        return ENSLIG_FORSORGER_TEMA.getKode().equals(tema);
    }

    public boolean gjelderForeldrepenger() {
        return FORELDREPENGER_TEMA.getKode().equals(tema)
                && RelatertYtelseBehandlingstema.erGjelderForeldrepenger(behandlingsTema);
    }

    public boolean gjelderSvangerskapspenger() {
        return FORELDREPENGER_TEMA.getKode().equals(tema)
                && RelatertYtelseBehandlingstema.erGjelderSvangerskapspenger(behandlingsTema);
    }

    public boolean gjelderSykepenger() {
        return SYKEPENGER_TEMA.getKode().equals(tema);
    }

    public boolean gjelderEngangsstonad() {
        return FORELDREPENGER_TEMA.getKode().equals(tema)
                && RelatertYtelseBehandlingstema.erGjelderEngangsstonad(behandlingsTema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(behandlingsTema, iverksatt, registrert, sakId, tema);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InfotrygdSak)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        InfotrygdSak that = (InfotrygdSak) obj;

        return Objects.equals(that.behandlingsTema, this.behandlingsTema) &&
                Objects.equals(that.iverksatt, this.iverksatt) &&
                // Objects.equals(that.registrert, this.registrert) &&
                // Objects.equals(that.sakId, this.sakId) &&
                Objects.equals(that.tema, this.tema);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[tema=" + tema + ", behandlingsTema=" + behandlingsTema + ", sakId="
                + sakId + ", iverksatt=" + iverksatt + ", registrert=" + registrert + "]";
    }

}
