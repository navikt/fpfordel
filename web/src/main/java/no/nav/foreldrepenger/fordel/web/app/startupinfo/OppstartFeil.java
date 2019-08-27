package no.nav.foreldrepenger.fordel.web.app.startupinfo;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.INFO;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface OppstartFeil extends DeklarerteFeil {

    OppstartFeil FACTORY = FeilFactory.create(OppstartFeil.class);

    @TekniskFeil(feilkode = "FP-753407", feilmelding = "Uventet exception ved oppstart", logLevel = ERROR)
    Feil uventetExceptionVedOppstart(Exception e);

    @TekniskFeil(feilkode = "FP-753409", feilmelding = "Selftest %s: %s. Endpoint: %s. Responstid: %s. Feilmelding: %s.", logLevel = INFO)
    Feil selftestStatus(String status, String description, String endpoint, String responstid, String feilmelding);
}
