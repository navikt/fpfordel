package no.nav.foreldrepenger.mottak.felles.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface KafkaFeil extends DeklarerteFeil {
    KafkaFeil FEILFACTORY = FeilFactory.create(KafkaFeil.class);

    @TekniskFeil(feilkode = "FP-190496", feilmelding = "Kunne ikke serialisere til json.", logLevel = LogLevel.WARN)
    Feil kanIkkeSerialisere(JsonProcessingException e);
}
