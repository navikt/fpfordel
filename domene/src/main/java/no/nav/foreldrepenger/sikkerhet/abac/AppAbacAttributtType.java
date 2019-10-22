package no.nav.foreldrepenger.sikkerhet.abac;

import no.nav.vedtak.sikkerhet.abac.AbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.StandardAbacAttributtType;

/**
 * AbacAttributtTyper som er i bruk i FPFORDEL.
 */
public enum AppAbacAttributtType implements AbacAttributtType {

    FORSENDELSE_UUID("forsendelseUUID"),

    ;

    public static AbacAttributtType AKTØR_ID = StandardAbacAttributtType.AKTØR_ID;

    public static AbacAttributtType FNR = StandardAbacAttributtType.FNR;

    public static AbacAttributtType JOURNALPOST_ID = StandardAbacAttributtType.JOURNALPOST_ID;

    public static AbacAttributtType SAKSNUMMER = StandardAbacAttributtType.SAKSNUMMER;

    private final boolean maskerOutput;
    private final String sporingsloggEksternKode;
    private final boolean valider;

    AppAbacAttributtType() {
        sporingsloggEksternKode = null;
        maskerOutput = false;
        valider = false;
    }

    AppAbacAttributtType(String sporingsloggEksternKode) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = false;
        valider = false;
    }

    AppAbacAttributtType(String sporingsloggEksternKode, boolean maskerOutput) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = maskerOutput;
        valider = false;
    }

    AppAbacAttributtType(String sporingsloggEksternKode, boolean maskerOutput, boolean valider) {
        this.sporingsloggEksternKode = sporingsloggEksternKode;
        this.maskerOutput = maskerOutput;
        this.valider = valider;
    }

    @Override
    public boolean getMaskerOutput() {
        return maskerOutput;
    }
    
    @Override
    public String getSporingsloggKode() {
        return sporingsloggEksternKode;
    }

    @Override
    public boolean getValider() {
        return valider;
    }
}
