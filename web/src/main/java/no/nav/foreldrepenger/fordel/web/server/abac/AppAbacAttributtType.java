package no.nav.foreldrepenger.fordel.web.server.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * AbacAttributtTyper som er i bruk i FPFORDEL.
 */
public enum AppAbacAttributtType implements AbacAttributtType {

    FORSENDELSE_UUID("forsendelseUUID");

    public static final AbacAttributtType AKTØR_ID = StandardAbacAttributtType.AKTØR_ID;

    private final boolean maskerOutput;
    private final String sporingsloggEksternKode;
    private final boolean valider;

    AppAbacAttributtType(String sporingsloggEksternKode) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = false;
        valider = false;
    }

    @Override
    public boolean getMaskerOutput() {
        return maskerOutput;
    }

    public String getSporingsloggKode() {
        return sporingsloggEksternKode;
    }

    public boolean getValider() {
        return valider;
    }
}
