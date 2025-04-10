package no.nav.foreldrepenger.fordel.web.server.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * AbacAttributtTyper som er i bruk i FPFORDEL.
 */
public enum AppAbacAttributtType implements AbacAttributtType {

    FORSENDELSE_UUID;

    public static final AbacAttributtType AKTØR_ID = StandardAbacAttributtType.AKTØR_ID;
    public static final AbacAttributtType FNR = StandardAbacAttributtType.FNR;
    public static final AbacAttributtType JOURNALPOST_ID = StandardAbacAttributtType.JOURNALPOST_ID;

    @Override
    public boolean getMaskerOutput() {
        return false;
    }

}
