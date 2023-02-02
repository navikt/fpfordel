package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OppgaverRestTjenesteTest {

    @Mock
    private PersonInformasjon pdl;
    @Mock
    private Oppgaver oppgaver;
    @Mock
    private Fagsak fagsak;
    @Mock
    private ArkivTjeneste arkiv;

    @Test
    void skal_levere_en_tom_liste_om_ingen_oppgaver_funnet() throws Exception {
        var restTjeneste = new OppgaverRestTjeneste(oppgaver, pdl, arkiv, fagsak);

        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

        assertThat(oppgaveDtos).isNotNull();
        assertThat(oppgaveDtos.size()).isEqualTo(0);
    }

    @Test
    void skal_levere_en_liste_med_oppgaver() throws Exception {
        var restTjeneste = new OppgaverRestTjeneste(oppgaver, pdl, arkiv, fagsak);

        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var aktørId = "aktørId";
        var beskrivelse = "beskrivelse";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, aktørId, now, expectedJournalpostId, beskrivelse, BehandlingTema.FORELDREPENGER_ADOPSJON));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), null)).thenReturn(journalføringOppgaver);

        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

        assertThat(oppgaveDtos).isNotNull();
        assertThat(oppgaveDtos.size()).isEqualTo(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.journalpostId()).isEqualTo(expectedJournalpostId);
        assertThat(oppgave.id()).isEqualTo(expectedId);
        assertThat(oppgave.frist()).isEqualTo(now);
        assertThat(oppgave.aktørId()).isEqualTo(aktørId);
        assertThat(oppgave.fødselsnummer()).isEqualTo(null);
        assertThat(oppgave.beskrivelse()).isEqualTo(beskrivelse);
        assertThat(oppgave.opprettetDato()).isEqualTo(now);
        assertThat(oppgave.prioritet()).isEqualTo(OppgaverRestTjeneste.OppgavePrioritet.NORM);
        assertThat(oppgave.ytelseType()).isEqualTo(BehandlingTema.FORELDREPENGER.getTermNavn());
        assertThat(oppgave.enhetId()).isEqualTo("enhet");
        assertThat(oppgave.journalpostHarMangler()).isEqualTo(false);
    }

    @Test
    void skal_levere_fnr_om_finnes() throws Exception {
        var restTjeneste = new OppgaverRestTjeneste(oppgaver, pdl, arkiv, fagsak);

        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var aktørId = "aktørId";
        var beskrivelse = "beskrivelse";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, aktørId, now, expectedJournalpostId, beskrivelse, BehandlingTema.FORELDREPENGER_FØDSEL));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), null)).thenReturn(journalføringOppgaver);
        var fnr = "12344345678";
        when(pdl.hentPersonIdentForAktørId(aktørId)).thenReturn(Optional.of(fnr));
        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

        assertThat(oppgaveDtos).isNotNull();
        assertThat(oppgaveDtos.size()).isEqualTo(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.aktørId()).isEqualTo(aktørId);
        assertThat(oppgave.fødselsnummer()).isEqualTo(fnr);
        assertThat(oppgave.ytelseType()).isEqualTo(BehandlingTema.FORELDREPENGER.getTermNavn());
        assertThat(oppgave.journalpostHarMangler()).isEqualTo(false);
    }

    @Test
    void skal_ha_mangel_om_aktørId_mangler() throws Exception {
        var restTjeneste = new OppgaverRestTjeneste(oppgaver, pdl, arkiv, fagsak);

        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, null, now, expectedJournalpostId, beskrivelse, BehandlingTema.ENGANGSSTØNAD_FØDSEL));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), null)).thenReturn(journalføringOppgaver);
        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

        assertThat(oppgaveDtos).isNotNull();
        assertThat(oppgaveDtos.size()).isEqualTo(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.aktørId()).isEqualTo(null);
        assertThat(oppgave.fødselsnummer()).isEqualTo(null);
        assertThat(oppgave.journalpostHarMangler()).isEqualTo(true);
    }

    @Test
    void skal_ha_ytelse_type_ukjent_om_det_ikke_lar_seg_utlede_fra_behandlingstema() throws Exception {
        var restTjeneste = new OppgaverRestTjeneste(oppgaver, pdl, arkiv, fagsak);

        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, null, now, expectedJournalpostId, beskrivelse, BehandlingTema.OMS));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), null)).thenReturn(journalføringOppgaver);
        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

        assertThat(oppgaveDtos).isNotNull();
        assertThat(oppgaveDtos.size()).isEqualTo(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.ytelseType()).isEqualTo("Ukjent");
    }

    private static Oppgave opprettOppgave(long expectedId, String aktørId, LocalDate now, String expectedJournalpostId, String beskrivelse, BehandlingTema behandlingTema) {
        return new Oppgave(
                expectedId,
                expectedJournalpostId,
                "FPSAK",
                null,
                aktørId,
                "tema",
                behandlingTema.getOffisiellKode(),
                Oppgavetype.JOURNALFØRING.getKode(),
                "behType",
                1,
                "enhet",
                now,
                now,
                Prioritet.NORM,
                Oppgavestatus.AAPNET,
                beskrivelse);
    }
}