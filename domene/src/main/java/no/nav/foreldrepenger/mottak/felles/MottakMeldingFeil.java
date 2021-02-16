package no.nav.foreldrepenger.mottak.felles;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.INFO;
import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface MottakMeldingFeil extends DeklarerteFeil {

    public static final String ENDRINGSSØKNAD_AVVIK_SAKSNUMMER = "FP-401245";

    MottakMeldingFeil FACTORY = FeilFactory.create(MottakMeldingFeil.class);

    @TekniskFeil(feilkode = "FP-941984", feilmelding = "Prosessering av preconditions for %s mangler %s. TaskId: %s", logLevel = WARN)
    Feil prosesstaskPreconditionManglerProperty(String taskname, String property, Long taskId);

    @TekniskFeil(feilkode = "FP-638068", feilmelding = "Prosessering av postconditions for %s mangler %s. TaskId: %s", logLevel = WARN)
    Feil prosesstaskPostconditionManglerProperty(String taskname, String property, Long taskId);

    @IntegrasjonFeil(feilkode = "FP-254631", feilmelding = "Fant ikke personident for aktørId i task %s.  TaskId: %s", logLevel = WARN)
    Feil fantIkkePersonidentForAktørId(String taskname, Long taskId);

    @TekniskFeil(feilkode = "FP-404782", feilmelding = "Ulik behandlingstemakode i tynnmelding (%s) og søknadsdokument (%s)", logLevel = ERROR)
    Feil ulikBehandlingstemaKodeITynnMeldingOgSøknadsdokument(String behandlingstemaKodeTynnmelding, String behandlingstemaKodeSøknadsdokument);

    @FunksjonellFeil(feilkode = ENDRINGSSØKNAD_AVVIK_SAKSNUMMER, feilmelding = "Ulike saksnummer i melding/VL (%s) og endringssøknad (%s).", løsningsforslag = "Dokumentet skal journalføres mot infotrygd", logLevel = LogLevel.INFO)
    Feil ulikSaksnummerITynnmeldingOgSøknadsdokument(String saksnummerTynnmelding, String saksnummerSøknadsdokument);

    @TekniskFeil(feilkode = "FP-502574", feilmelding = "Ulik aktørId i tynnmelding og søknadsdokument", logLevel = WARN)
    Feil ulikAktørIdITynnMeldingOgSøknadsdokument();

    @TekniskFeil(feilkode = "FP-785833", feilmelding = "Feil journaltilstand. Forventet tilstand: endelig, fikk Midlertidig", logLevel = INFO)
    Feil feilJournalTilstandForventetTilstandEndelig();

    @TekniskFeil(feilkode = "FP-286143", feilmelding = "Ukjent behandlingstema {%s}", logLevel = WARN)
    Feil ukjentBehandlingstema(String behandlingTema);

    @TekniskFeil(feilkode = "FP-429673", feilmelding = "Mangler Ytelse på Innteksmelding", logLevel = WARN)
    Feil manglerYtelsePåInntektsmelding();
}
