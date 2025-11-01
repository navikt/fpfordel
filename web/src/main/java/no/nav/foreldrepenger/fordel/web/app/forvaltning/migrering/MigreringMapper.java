package no.nav.foreldrepenger.fordel.web.app.forvaltning.migrering;

import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;

public class MigreringMapper {

    public static MigreringOppgaveDto.OppgaveDto tilOppgaveDto(OppgaveEntitet oppgave) {
        return new MigreringOppgaveDto.OppgaveDto(oppgave.getJournalpostId(), oppgave.getOpprettetTidspunkt(),
            oppgave.getEnhet(), oppgave.getFrist(), oppgave.getBrukerId(), oppgave.getYtelseType(),
            oppgave.getBeskrivelse(), oppgave.getStatus(), oppgave.getReservertAv());
    }

    public static OppgaveEntitet fraOppgaveDto(MigreringOppgaveDto.OppgaveDto oppgaveDto) {
        var oppgave = OppgaveEntitet.builder()
            .medJournalpostId(oppgaveDto.journalpostId())
            .medEnhet(oppgaveDto.enhet())
            .medFrist(oppgaveDto.frist())
            .medBrukerId(oppgaveDto.brukerId())
            .medYtelseType(oppgaveDto.ytelseType())
            .medBeskrivelse(oppgaveDto.beskrivelse())
            .medReservertAv(oppgaveDto.reservertAv())
            .medStatus(oppgaveDto.status())
            .build();
        oppgave.setOpprettetTidspunkt(oppgaveDto.opprettetTidspunkt());
        return oppgave;
    }
}
