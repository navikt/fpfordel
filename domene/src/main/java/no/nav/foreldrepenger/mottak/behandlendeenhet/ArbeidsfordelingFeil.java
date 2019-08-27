package no.nav.foreldrepenger.mottak.behandlendeenhet;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.util.List;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnAlleBehandlendeEnheterListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface ArbeidsfordelingFeil extends DeklarerteFeil {

    ArbeidsfordelingFeil FACTORY = FeilFactory.create(ArbeidsfordelingFeil.class);

    @TekniskFeil(feilkode = "FP-224143", feilmelding = "Ugyldig input til finnFagsakInfomasjon behandlende enhet", logLevel = LogLevel.ERROR)
    Feil finnBehandlendeEnhetListeUgyldigInput(FinnBehandlendeEnhetListeUgyldigInput e);

    @TekniskFeil(feilkode = "FP-669566", feilmelding = "Finner ikke behandlende enhet for geografisk tilknytning '%s', diskresjonskode '%s', behandlingstema '%s'", logLevel = WARN)
    Feil finnerIkkeBehandlendeEnhet(String geografiskTilknytning, String diskresjonskode, BehandlingTema behandlingTema);

    @TekniskFeil(feilkode = "FP-104703", feilmelding = "Forventet en, men fikk flere alternative behandlende enheter for geografisk tilknytning '%s', diskresjonskode '%s', behandlingstema  '%s': '%s'. Valgte '%s'", logLevel = WARN)
    Feil fikkFlereBehandlendeEnheter(String geografiskTilknytning, String diskresjonskode, BehandlingTema behandlingTema, List<String> enheter, String valgtEnhet);

    @TekniskFeil(feilkode = "FP-678703", feilmelding = "Finner ikke alle behandlende enheter for behandlingstema '%s'", logLevel = WARN)
    Feil finnerIkkeAlleBehandlendeEnheter(BehandlingTema behandlingTema);

    @TekniskFeil(feilkode = "FP-324042", feilmelding = "Ugyldig input til finn alle behandlende enheter", logLevel = LogLevel.ERROR)
    Feil finnAlleBehandlendeEnheterListeUgyldigInput(FinnAlleBehandlendeEnheterListeUgyldigInput e);

}
