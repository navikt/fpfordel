package no.nav.foreldrepenger.mottak.domene;

import java.util.UUID;

import no.nav.vedtak.exception.TekniskException;

public class MeldingKonverteringFeil {
    private MeldingKonverteringFeil() {

    }

    static TekniskException ukjentSkjemaType(String skjemaType) {
        return new TekniskException("FP-947143", String.format("Ukjent meldingstype %s", skjemaType));
    }

    public static TekniskException ukjentFormatPåSøknad(UUID forsendelseId) {
        return new TekniskException("FP-874812",
                String.format("Ukjent format på søknad eller mangler nødvendig element (Forsendelse med ID: %s)", forsendelseId));
    }

    public static TekniskException finnerIkkeAktørId(String classe) {
        return new TekniskException("FP-513732", String.format("Finner ikke aktørID for bruker på %s", classe));

    }
}
