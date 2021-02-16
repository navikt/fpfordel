package no.nav.foreldrepenger.mottak.felles;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;

public class MottakMeldingFeil {

    private MottakMeldingFeil() {

    }

    public static TekniskException prosesstaskPreconditionManglerProperty(String taskname, String property, Long taskId) {
        return new TekniskException("FP-941984",
                String.format("Prosessering av preconditions for %s mangler %s. TaskId: %s", taskname, property, taskId));
    }

    public static TekniskException prosesstaskPostconditionManglerProperty(String taskname, String property, Long taskId) {
        return new TekniskException("FP-638068",
                String.format("Prosessering av postconditions for %s mangler %s. TaskId: %s", taskname, property, taskId));
    }

    public static TekniskException fantIkkePersonidentForAktørId(String taskname, Long taskId) {
        return new TekniskException("FP-254631",
                String.format("Fant ikke personident for aktørId i task %s.  TaskId: %s", taskname, taskId));
    }

    public static TekniskException ulikBehandlingstemaKodeITynnMeldingOgSøknadsdokument(String behandlingstemaKodeTynnmelding,
            String behandlingstemaKodeSøknadsdokument) {
        return new TekniskException("FP-404782",
                String.format("Ulik behandlingstemakode i tynnmelding (%s) og søknadsdokument (%s)", behandlingstemaKodeTynnmelding,
                        behandlingstemaKodeSøknadsdokument));

    }

    public static FunksjonellException ulikSaksnummerITynnmeldingOgSøknadsdokument(String saksnummerTynnmelding, String saksnummerSøknadsdokument) {
        return new FunksjonellException("FP-401245",
                String.format("Ulike saksnummer i melding/VL (%s) og endringssøknad (%s).", saksnummerTynnmelding,
                        saksnummerSøknadsdokument),
                null);
    }

    public static TekniskException ulikAktørIdITynnMeldingOgSøknadsdokument(String tynn, String søknad) {
        return new TekniskException("FP-502574",
                String.format("Ulik aktørId i tynnmelding (%s) og søknadsdokument (%s)", tynn, søknad));
    }

    public static TekniskException ukjentBehandlingstema(String behandlingTema) {
        return new TekniskException("FP-785833",
                String.format("Ukjent behandlingstema {%s}", behandlingTema));

    }

    public static TekniskException manglerYtelsePåInntektsmelding() {
        return new TekniskException("FP-429673", "Mangler Ytelse på Innteksmelding");

    }
}
