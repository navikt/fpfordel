package no.nav.foreldrepenger.mottak.behandlendeenhet;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface EnhetsTjenesteFeil extends DeklarerteFeil {

    EnhetsTjenesteFeil FACTORY = FeilFactory.create(EnhetsTjenesteFeil.class);

    @TekniskFeil(feilkode = "FP-070668", feilmelding = "Person ikke funnet ved hentGeografiskTilknytning eller relasjoner", logLevel = LogLevel.ERROR)
    Feil enhetsTjenestePersonIkkeFunnet(Exception e);

    @ManglerTilgangFeil(feilkode = "FP-509290", feilmelding = "Mangler tilgang til å utføre hentGeografiskTilknytning eller hentrelasjoner", logLevel = LogLevel.ERROR)
    Feil enhetsTjenesteSikkerhetsbegrensing(Exception e);

}
