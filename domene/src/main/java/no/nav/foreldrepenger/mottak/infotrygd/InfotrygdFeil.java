package no.nav.foreldrepenger.mottak.infotrygd;

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface InfotrygdFeil extends DeklarerteFeil {

    InfotrygdFeil FACTORY = FeilFactory.create(InfotrygdFeil.class);

    @TekniskFeil(feilkode = "FP-250919", feilmelding = "%s ikke tilgjengelig (sikkerhetsbegrensning)", logLevel = LogLevel.WARN)
    Feil tjenesteUtilgjengeligSikkerhetsbegrensning(String tjeneste, Exception exceptionMessage);

    @IntegrasjonFeil(feilkode = "FP-614379", feilmelding = "Funksjonell feil i grensesnitt mot %s", logLevel = LogLevel.WARN, exceptionClass = InfotrygdUgyldigInputException.class)
    Feil ugyldigInput(String tjeneste, FinnSakListeUgyldigInput årsak);

    @IntegrasjonFeil(feilkode = "FP-180123", feilmelding = "Funksjonell feil i grensesnitt mot %s", logLevel = LogLevel.WARN, exceptionClass = InfotrygdPersonIkkeFunnetException.class)
    Feil personIkkeFunnet(String tjeneste, FinnSakListePersonIkkeFunnet årsak);

    @TekniskFeil(feilkode = "FP-180124", feilmelding = "Tjeneste %s ikke tilgjengelig (nedetid)", logLevel = LogLevel.INFO)
    Feil nedetid(String tjeneste, IntegrasjonException årsak);

}
