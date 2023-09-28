package no.nav.foreldrepenger.journalføring.oppgave.domene;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.domene.BrukerId;
import no.nav.foreldrepenger.domene.YtelseType;

class OppgaveTest {

    protected static final String TEST_BESKRIVELSE = "Test beskrivelse";

    @Test
    void getSetTest() {
        var builder = OppgaveEntitet.builder();

        var now = LocalDate.now();
        var brukerId = new BrukerId("0123456789123");
        var journalpostId = "123";
        var enhet = "1234";
        var reservertAv = "SAKSBEH";

        builder.medJournalpostId(journalpostId);
        builder.medBrukerId(brukerId);
        builder.medEnhet(enhet);
        builder.medFrist(now.plusDays(1));
        builder.medStatus(Status.AAPNET);
        builder.medBeskrivelse(TEST_BESKRIVELSE);
        builder.medYtelseType(YtelseType.FP);
        builder.medReservertAv(reservertAv);

        var oppgave = builder.build();

        assertThat(oppgave.getJournalpostId()).isEqualTo(journalpostId);
        assertThat(oppgave.getBrukerId()).isEqualTo(brukerId);
        assertThat(oppgave.getEnhet()).isEqualTo(enhet);
        assertThat(oppgave.getFrist()).isEqualTo(now.plusDays(1));
        assertThat(oppgave.getStatus()).isEqualTo(Status.AAPNET);
        assertThat(oppgave.getBeskrivelse()).isEqualTo(TEST_BESKRIVELSE);
        assertThat(oppgave.getYtelseType()).isEqualTo(YtelseType.FP);
        assertThat(oppgave.getReservertAv()).isEqualTo(reservertAv);
    }
}
