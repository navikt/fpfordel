package no.nav.foreldrepenger.mottak.felles.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.vedtak.exception.TekniskException;

public class KafkaFeil {

    static TekniskException kanIkkeSerialisere(JsonProcessingException e) {
        return new TekniskException("FP-190496", "Kunne ikke serialisere til json");
    }
}
