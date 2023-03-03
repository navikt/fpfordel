package no.nav.foreldrepenger.journalføring;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.AktørIdDto;
import no.nav.foreldrepenger.mottak.klient.FagSakYtelseTypeDto;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.FagsakStatusDto;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.foreldrepenger.typer.JournalpostId;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;

/**
 * Denne validatoren sjekker om gitt journalpost er faglig konform med de valgene SBH har gjort i GUI, f.eks:
 * - At ytelseType fra dokument og valgt sak stemmer
 * - At ytelseType fra inntektsmelding og valgt sak
 * - At dokumentet ikke er en endringssøknad.
 * - At brukeren ikke har en aktiv sak allerede opprettet i FPSAK.
 */
public class ManuellOpprettSakValidator {

    private static final Logger LOG = LoggerFactory.getLogger(ManuellOpprettSakValidator.class);

    private final ArkivTjeneste arkivTjeneste;
    private final Fagsak fagsak;

    public ManuellOpprettSakValidator(ArkivTjeneste arkivTjeneste, Fagsak fagsak) {
        this.arkivTjeneste = arkivTjeneste;
        this.fagsak = fagsak;
    }

    private static FagSakYtelseTypeDto utledYtelseTypeFor(DokumentTypeId dokumentTypeId) {
        return switch (dokumentTypeId) {
            case SØKNAD_ENGANGSSTØNAD_ADOPSJON -> FagSakYtelseTypeDto.ENGANGSTØNAD;
            case SØKNAD_ENGANGSSTØNAD_FØDSEL -> FagSakYtelseTypeDto.ENGANGSTØNAD;
            case SØKNAD_FORELDREPENGER_ADOPSJON -> FagSakYtelseTypeDto.FORELDREPENGER;
            case SØKNAD_FORELDREPENGER_FØDSEL -> FagSakYtelseTypeDto.FORELDREPENGER;
            case SØKNAD_SVANGERSKAPSPENGER -> FagSakYtelseTypeDto.SVANGERSKAPSPENGER;
            default -> null;
        };
    }

    public void validerKonsistensMedSak(JournalpostId journalpostId, FagSakYtelseTypeDto oppgittFagSakYtelseTypeDto, AktørId aktørId) {
        requireNonNull(journalpostId, "Ugyldig input: JournalpostId kan ikke være null ved opprettelse av en sak.");
        requireNonNull(oppgittFagSakYtelseTypeDto, "Ugyldig input: YtelseType kan ikke være null ved opprettelse av en sak.");
        requireNonNull(aktørId, "Ugyldig input: AktørId kan ikke være null ved opprettelse av en sak.");

        var arkivJournalpost = arkivTjeneste.hentArkivJournalpost(journalpostId.getVerdi());
        var hovedDokumentType = arkivJournalpost.getHovedtype();

        FagSakYtelseTypeDto journalpostFagSakYtelseTypeDto = null;

        if (DokumentTypeId.erSøknadType(hovedDokumentType)) {
            journalpostFagSakYtelseTypeDto = utledYtelseTypeFor(hovedDokumentType);
            if (oppgittFagSakYtelseTypeDto.equals(journalpostFagSakYtelseTypeDto)) {
                return;
            }
        } else if (DokumentTypeId.INNTEKTSMELDING.equals(hovedDokumentType)) {
            var original = arkivJournalpost.getStrukturertPayload().toLowerCase();
            if (original.contains("ytelse>foreldrepenger<")) {
                if (harAktivSak(aktørId, oppgittFagSakYtelseTypeDto)) {
                    throw new TekniskException("FP-34238",
                        "Kan ikke journalføre FP inntektsmelding på en ny sak fordi det finnes en aktiv foreldrepenger sak allerede.");
                }
                journalpostFagSakYtelseTypeDto = FagSakYtelseTypeDto.FORELDREPENGER;
            } else if (original.contains("ytelse>svangerskapspenger<")) {
                journalpostFagSakYtelseTypeDto = FagSakYtelseTypeDto.SVANGERSKAPSPENGER;
            }
        }
        LOG.info("FPSAK vurdering ytelsedok {} vs ytelseoppgitt {}", journalpostFagSakYtelseTypeDto, oppgittFagSakYtelseTypeDto);
        if (!oppgittFagSakYtelseTypeDto.equals(journalpostFagSakYtelseTypeDto)) {
            throw new FunksjonellException("FP-785359", "Dokument og valgt ytelsetype i uoverenstemmelse",
                "Velg ytelsetype som samstemmer med dokument");
        }
    }

    private boolean harAktivSak(AktørId aktørId, FagSakYtelseTypeDto oppgittFagSakYtelseTypeDto) {
        return fagsak.hentBrukersSaker(new AktørIdDto(aktørId.getId()))
            .stream()
            .filter(it -> !it.status().equals(FagsakStatusDto.AVSLUTTET))
            .anyMatch(it -> it.fagSakYtelseTypeDto().equals(oppgittFagSakYtelseTypeDto));
    }

}
