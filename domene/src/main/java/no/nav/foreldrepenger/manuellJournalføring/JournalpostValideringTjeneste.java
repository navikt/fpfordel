package no.nav.foreldrepenger.manuellJournalføring;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.YtelseType;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.foreldrepenger.typer.JournalpostId;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JournalpostValideringTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(JournalpostValideringTjeneste.class);

    private ArkivTjeneste arkivTjeneste;
    private Fagsak fagsak;

    public JournalpostValideringTjeneste(ArkivTjeneste arkivTjeneste, Fagsak fagsak) {
        this.arkivTjeneste = arkivTjeneste;
        this.fagsak = fagsak;
    }

    public void validerKonsistensMedSak(JournalpostId journalpostId, String behandlingTema, AktørId aktørId) {
        var oppgittBehandlingTema = utledOgValiderOppgittBehandlingTema(behandlingTema);
        var journalpost = arkivTjeneste.hentJournalpostForSak(journalpostId.getVerdi());
        var hoveddokument = journalpost.map(ArkivJournalpost::getHovedDokument).orElseThrow();

        var journalpostYtelseType = YtelseType.UDEFINERT;
        var oppgittYtelseType = oppgittBehandlingTema.utledYtelseType();

        if (DokumentTypeId.erSøknadType(hoveddokument.getDokumentType())) {
            journalpostYtelseType = utledYtelseTypeFor(hoveddokument.getDokumentType());
            if (oppgittYtelseType.equals(journalpostYtelseType)) {
                return;
            }
        } else if (DokumentTypeId.INNTEKTSMELDING.equals(hoveddokument.getDokumentType())) {
            var original = arkivTjeneste.hentStrukturertDokument(journalpostId.getVerdi(), hoveddokument.getDokumentId()).toLowerCase();
            if (original.contains("ytelse>foreldrepenger<")) {
                if (fagsak.harAktivSak(aktørId, oppgittYtelseType)) {
                    throw new TekniskException("FP-34238", "Kan ikke journalføre FP inntektsmelding på en ny sak fordi det finnes en aktiv foreldrepenger sak allerede.");
                }
                journalpostYtelseType = YtelseType.FORELDREPENGER;
            } else if (original.contains("ytelse>svangerskapspenger<")) {
                journalpostYtelseType = YtelseType.SVANGERSKAPSPENGER;
            }
        }
        LOG.info("FPSAK vurdering ytelsedok {} vs ytelseoppgitt {}", journalpostYtelseType, oppgittYtelseType);
        if (!oppgittYtelseType.equals(journalpostYtelseType)) {
            throw new FunksjonellException("FP-785359", "Dokument og valgt ytelsetype i uoverenstemmelse",
                    "Velg ytelsetype som samstemmer med dokument");
        }
        // TODO: Vil aldri komme hit siden oppgittYtelseType valideres for UNDEFINED
        if (YtelseType.UDEFINERT.equals(journalpostYtelseType)) {
            throw new FunksjonellException("FP-785360", "Kan ikke opprette sak basert på oppgitt dokument",
                    "Journalføre dokument på annen sak");
        }
    }

    private BehandlingTema utledOgValiderOppgittBehandlingTema(String behandlingstemaOffisiellKode) {
        var behandlingTema = BehandlingTema.fraOffisiellKode(behandlingstemaOffisiellKode);
        if (BehandlingTema.UDEFINERT.equals(behandlingTema)) {
            var feilMelding = lagUgyldigInputMelding(behandlingstemaOffisiellKode);
            throw new TekniskException("FP-34236", feilMelding);
        }
        if (BehandlingTema.UDEFINERT.equals(BehandlingTema.forYtelseUtenFamilieHendelse(behandlingTema))) {
            var feilMelding = lagUgyldigInputMelding(behandlingstemaOffisiellKode);
            throw new TekniskException("FP-34237", feilMelding);
        }
        return behandlingTema;
    }

    private static String lagUgyldigInputMelding(String verdi) {
        return String.format("Ugyldig input: Behandlingstema med verdi: %s er ugyldig input.", verdi);
    }

    private static YtelseType utledYtelseTypeFor(DokumentTypeId dokumentTypeId) {
        return switch (dokumentTypeId) {
            case SØKNAD_ENGANGSSTØNAD_ADOPSJON -> YtelseType.ENGANGSTØNAD;
            case SØKNAD_ENGANGSSTØNAD_FØDSEL -> YtelseType.ENGANGSTØNAD;
            case SØKNAD_FORELDREPENGER_ADOPSJON -> YtelseType.FORELDREPENGER;
            case SØKNAD_FORELDREPENGER_FØDSEL -> YtelseType.FORELDREPENGER;
            case SØKNAD_SVANGERSKAPSPENGER ->  YtelseType.SVANGERSKAPSPENGER;
            default -> YtelseType.UDEFINERT;
        };
    }

}
