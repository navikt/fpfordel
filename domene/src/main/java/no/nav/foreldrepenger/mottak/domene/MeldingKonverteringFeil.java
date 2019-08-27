package no.nav.foreldrepenger.mottak.domene;

import static no.nav.vedtak.feil.LogLevel.WARN;

import java.util.UUID;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface MeldingKonverteringFeil extends DeklarerteFeil {

    MeldingKonverteringFeil FACTORY = FeilFactory.create(MeldingKonverteringFeil.class);

    @TekniskFeil(feilkode = "FP-947143", feilmelding = "Ukjent meldingstype %s", logLevel = WARN)
    Feil ukjentSkjemaType(String skjemaType);

    @TekniskFeil(feilkode = "FP-874812", feilmelding = "Ukjent format på søknad eller mangler nødvendig element (Forsendelse med ID: %s)", logLevel = WARN)
    Feil ukjentFormatPåSøknad(UUID forsendelseId);

    @TekniskFeil(feilkode = "FP-513732", feilmelding = "Finner ikke aktørID for bruker på %s", logLevel = WARN)
    Feil finnerIkkeAktørId(String classe);
}
