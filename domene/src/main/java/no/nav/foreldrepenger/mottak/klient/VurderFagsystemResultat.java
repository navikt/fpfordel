package no.nav.foreldrepenger.mottak.klient;

import java.time.LocalDateTime;
import java.util.Optional;

import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;

public class VurderFagsystemResultat {

    private boolean behandlesIVedtaksløsningen;
    private boolean sjekkMotInfotrygd;
    private boolean manuellVurdering;
    private boolean prøvIgjen;
    private LocalDateTime prøvIgjenTidspunkt;
    private String saksnummer;

    VurderFagsystemResultat(BehandlendeFagsystemDto data) {
        if (data == null) return;
        this.behandlesIVedtaksløsningen = data.isBehandlesIVedtaksløsningen();
        this.sjekkMotInfotrygd = data.isSjekkMotInfotrygd();
        this.manuellVurdering = data.isManuellVurdering();
        this.prøvIgjen = data.isPrøvIgjen();
        data.getPrøvIgjenTidspunkt().ifPresent(this::setPrøvIgjenTidspunkt);
        data.getSaksnummer().ifPresent(this::setSaksnummer);
    }

    public VurderFagsystemResultat() {
    }

    public boolean isBehandlesIVedtaksløsningen() {
        return behandlesIVedtaksløsningen;
    }

    public void setBehandlesIVedtaksløsningen(boolean behandlesIVedtaksløsningen) {
        this.behandlesIVedtaksløsningen = behandlesIVedtaksløsningen;
    }

    public boolean isSjekkMotInfotrygd() {
        return sjekkMotInfotrygd;
    }

    public void setSjekkMotInfotrygd(boolean sjekkMotInfotrygd) {
        this.sjekkMotInfotrygd = sjekkMotInfotrygd;
    }

    public boolean isManuellVurdering() {
        return manuellVurdering;
    }

    public void setManuellVurdering(boolean manuellVurdering) {
        this.manuellVurdering = manuellVurdering;
    }

    public boolean isPrøvIgjen() {
        return prøvIgjen;
    }

    public void setPrøvIgjen(boolean prøvIgjen) {
        this.prøvIgjen = prøvIgjen;
    }

    public Optional<LocalDateTime> getPrøvIgjenTidspunkt() {
        return Optional.ofNullable(prøvIgjenTidspunkt);
    }

    public void setPrøvIgjenTidspunkt(LocalDateTime prøvIgjenTidspunkt) {
        this.prøvIgjenTidspunkt = prøvIgjenTidspunkt;
    }

    public Optional<String> getSaksnummer() {
        return Optional.ofNullable(saksnummer);
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

}
