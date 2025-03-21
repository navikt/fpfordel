package no.nav.foreldrepenger.mottak.task.sikkerhetsnett;

public record SikkerhetsnettResponse(String journalpostId,
                                     String mottaksKanal,
                                     String behandlingstema,
                                     String journalforendeEnhet) {

}
