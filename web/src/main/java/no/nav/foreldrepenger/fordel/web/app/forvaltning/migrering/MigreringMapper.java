package no.nav.foreldrepenger.fordel.web.app.forvaltning.migrering;

import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import no.nav.foreldrepenger.mottak.domene.dokument.Journalpost;

public class MigreringMapper {

    public static MigreringOppgaveDto.OppgaveDto tilOppgaveDto(OppgaveEntitet oppgave) {
        return new MigreringOppgaveDto.OppgaveDto(oppgave.getJournalpostId(), oppgave.getEnhet(), oppgave.getFrist(), oppgave.getBrukerId(),
            oppgave.getYtelseType(), oppgave.getBeskrivelse(), oppgave.getReservertAv());
    }

    public static OppgaveEntitet fraOppgaveDto(MigreringOppgaveDto.OppgaveDto oppgaveDto) {
        return OppgaveEntitet.builder()
            .medJournalpostId(oppgaveDto.journalpostId())
            .medEnhet(oppgaveDto.enhet())
            .medFrist(oppgaveDto.frist())
            .medBrukerId(oppgaveDto.brukerId())
            .medYtelseType(oppgaveDto.ytelseType())
            .medBeskrivelse(oppgaveDto.beskrivelse())
            .medReservertAv(oppgaveDto.reservertAv())
            .medStatus(Status.AAPNET)
            .build();
    }

    public static MigreringJournalpostDto.JournalpostDto tilJournalpostDto(Journalpost journalpost) {
        return new MigreringJournalpostDto.JournalpostDto(journalpost.getJournalpostId(),
            journalpost.getTilstand(),
            journalpost.getKanal(),
            journalpost.getReferanse());
    }

    public static Journalpost fraJournalpostDto(MigreringJournalpostDto.JournalpostDto journalpost) {
        return new Journalpost(journalpost.journalpostId(), journalpost.tilstand(), journalpost.kanal(), journalpost.referanse(), "FORDEL");
    }


}
