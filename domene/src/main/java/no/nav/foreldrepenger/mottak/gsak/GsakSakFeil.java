package no.nav.foreldrepenger.mottak.gsak;

import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakUgyldigInput;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface GsakSakFeil extends DeklarerteFeil {

    GsakSakFeil FACTORY = FeilFactory.create(GsakSakFeil.class);

    @TekniskFeil(feilkode = "FP-974567", feilmelding = "for mange saker funnet.", logLevel = LogLevel.ERROR)
    Feil forMangeSakerFunnet(FinnSakForMangeForekomster e);

    @TekniskFeil(feilkode = "FP-350721", feilmelding = "ugyldig input.", logLevel = LogLevel.ERROR)
    Feil ugyldigInput(FinnSakUgyldigInput e);

}
