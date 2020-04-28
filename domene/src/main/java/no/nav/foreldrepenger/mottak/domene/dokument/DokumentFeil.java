package no.nav.foreldrepenger.mottak.domene.dokument;

import java.util.UUID;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface DokumentFeil extends DeklarerteFeil {
    DokumentFeil FACTORY = FeilFactory.create(DokumentFeil.class);

    @TekniskFeil(feilkode = "FP-295614", feilmelding = "Ukjent forsendelseId %s", logLevel = LogLevel.WARN)
    Feil fantIkkeForsendelse(UUID forsendelseId);

    @TekniskFeil(feilkode = "FP-302156", feilmelding = "Spørringen returnerte mer enn eksakt ett resultat", logLevel = LogLevel.WARN)
    Feil fantIkkeUnikResultat();

    @TekniskFeil(exceptionClass = ConstraintException.class, feilkode = "FP-324315", feilmelding = "Duplikat forsendelseId {%s} finnes i databasen", logLevel = LogLevel.WARN)
    Feil constraintForsendelseId(UUID forsendelseId);
}
