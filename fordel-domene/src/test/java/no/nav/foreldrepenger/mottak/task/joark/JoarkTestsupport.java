package no.nav.foreldrepenger.mottak.task.joark;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

class JoarkTestsupport {

    static final String ARKIV_ID = "kiv2";
    static final String DOKUMENT_ID = "456";
    static final String BRUKER_FNR = "99999999899";
    static final String AKTØR_ID = "9000000000009";
    static final String RANDOM_REF = "eksternReferanse";
    static final List<String> brukerListe = Collections.singletonList(AKTØR_ID);

    final ProsessTaskData taskData;

    JoarkTestsupport() {
        taskData = ProsessTaskData.forProsessTaskHandler(HentDataFraJoarkTask.class);
        taskData.setSekvens("1");
    }

    ArkivJournalpost.Builder lagArkivJournalpost(List<String> brukerListe,
            DokumentTypeId dokumentTypeId) {
        return ArkivJournalpost.getBuilder()
                .medEksternReferanseId(RANDOM_REF)
                .medJournalpostId(ARKIV_ID)
                .medTilstand(Journalstatus.MOTTATT)
                .medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER)
                .medHovedtype(dokumentTypeId)
                .medDokumentInfoId(DOKUMENT_ID)
                .medDatoOpprettet(LocalDateTime.now())
                .medBrukerAktørId(brukerListe.isEmpty() ? null : brukerListe.get(0));
    }

    ArkivJournalpost lagArkivJournalpostUstrukturert(List<String> brukerListe) {
        return lagArkivJournalpost(brukerListe, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).build();
    }

    ArkivJournalpost lagJArkivJournalpostUstrukturert() {
        return lagArkivJournalpost(brukerListe, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).build();
    }

    ArkivJournalpost lagJArkivJournalpostKlage() {
        return lagArkivJournalpost(brukerListe, DokumentTypeId.KLAGE_DOKUMENT).build();
    }

    ArkivJournalpost lagJArkivJournalpostKlageMedSaksnummer(String saksnummer) {
        return lagArkivJournalpost(brukerListe, DokumentTypeId.KLAGE_DOKUMENT).medSaksnummer(saksnummer).build();
    }

    ArkivJournalpost lagJArkivJournalpostUstrukturert(DokumentTypeId dokumentTypeId) {
        return lagArkivJournalpost(brukerListe, dokumentTypeId).build();
    }

    ArkivJournalpost lagArkivJournalpostStrukturert(DokumentTypeId dokumentTypeId, String filename) {
        try {
            if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
                return lagArkivJournalpost(Collections.emptyList(), dokumentTypeId).medStrukturertPayload(this.readFile(filename)).build();
            }
            return lagArkivJournalpost(brukerListe, dokumentTypeId).medStrukturertPayload(this.readFile(filename)).build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Manglende fil");
        }

    }

    private String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
