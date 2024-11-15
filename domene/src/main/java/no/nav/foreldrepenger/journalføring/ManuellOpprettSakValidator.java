package no.nav.foreldrepenger.journalføring;

import static java.util.Objects.requireNonNull;
import static no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType.ES;
import static no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType.FP;
import static no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType.SVP;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;

import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;

import no.nav.foreldrepenger.journalføring.utils.YtelseTypeUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.FagsakYtelseTypeDto;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.vedtak.exception.FunksjonellException;

import java.util.Optional;

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

    public ManuellOpprettSakValidator(ArkivTjeneste arkivTjeneste) {
        this.arkivTjeneste = arkivTjeneste;
    }

    private static FagsakYtelseTypeDto utledYtelseTypeFor(DokumentTypeId dokumentTypeId) {
        return switch (dokumentTypeId) {
            case SØKNAD_ENGANGSSTØNAD_ADOPSJON, SØKNAD_ENGANGSSTØNAD_FØDSEL -> FagsakYtelseTypeDto.ENGANGSTØNAD;
            case SØKNAD_FORELDREPENGER_ADOPSJON, SØKNAD_FORELDREPENGER_FØDSEL -> FagsakYtelseTypeDto.FORELDREPENGER;
            case SØKNAD_SVANGERSKAPSPENGER -> FagsakYtelseTypeDto.SVANGERSKAPSPENGER;
            default -> null;
        };
    }

    public void validerKonsistensMedSak(JournalpostId journalpostId, FagsakYtelseTypeDto oppgittFagsakYtelseTypeDto, AktørId aktørId,
                                        DokumentTypeId nyDokumentTypeId) {
        requireNonNull(journalpostId, "Ugyldig input: JournalpostId kan ikke være null ved opprettelse av en sak.");
        requireNonNull(oppgittFagsakYtelseTypeDto, "Ugyldig input: YtelseType kan ikke være null ved opprettelse av en sak.");
        requireNonNull(aktørId, "Ugyldig input: AktørId kan ikke være null ved opprettelse av en sak.");

        var arkivJournalpost = arkivTjeneste.hentArkivJournalpost(journalpostId.getVerdi());
        validerKonsistensMedSakJP(arkivJournalpost, oppgittFagsakYtelseTypeDto, aktørId, nyDokumentTypeId);
    }

    public void validerKonsistensMedSakJP(ArkivJournalpost arkivJournalpost, FagsakYtelseTypeDto oppgittFagsakYtelseTypeDto, AktørId aktørId,
                                          DokumentTypeId nyDokumentTypeId) {
        requireNonNull(arkivJournalpost, "Ugyldig input: Journalpost kan ikke være null ved opprettelse av en sak.");
        requireNonNull(oppgittFagsakYtelseTypeDto, "Ugyldig input: YtelseType kan ikke være null ved opprettelse av en sak.");
        requireNonNull(aktørId, "Ugyldig input: AktørId kan ikke være null ved opprettelse av en sak.");

        var hovedDokumentType = arkivJournalpost.getHovedtype();
        if (nyDokumentTypeId != null && !DokumentTypeId.UDEFINERT.equals(nyDokumentTypeId)) {
            hovedDokumentType = nyDokumentTypeId;
        }

        FagsakYtelseTypeDto journalpostFagsakYtelseTypeDto = null;

        if (DokumentTypeId.erSøknadType(hovedDokumentType)) {
            journalpostFagsakYtelseTypeDto = utledYtelseTypeFor(hovedDokumentType);
        } else if (DokumentTypeId.INNTEKTSMELDING.equals(hovedDokumentType)) {
            var original = arkivJournalpost.getStrukturertPayload().toLowerCase();
            if (original.contains("ytelse>foreldrepenger<")) {
                journalpostFagsakYtelseTypeDto = FagsakYtelseTypeDto.FORELDREPENGER;
            } else if (original.contains("ytelse>svangerskapspenger<")) {
                journalpostFagsakYtelseTypeDto = FagsakYtelseTypeDto.SVANGERSKAPSPENGER;
            }
        } else if (arkivJournalpost.getTilstand().erEndelig()) { // her prøver man å flytte en journalpost som allerede er journalført ferdig til en annen sak
            var behandlingstema = arkivJournalpost.getBehandlingstema();
            LOG.info("Utleder behandlingTema: {}, behandlingTema: {}", arkivJournalpost.getUtledetBehandlingstema(), behandlingstema);
            journalpostFagsakYtelseTypeDto = Optional.ofNullable(behandlingstema.utledYtelseType()).orElseGet(() -> arkivJournalpost.getUtledetBehandlingstema().utledYtelseType());
            LOG.info("Prøver å journalføre en allerede journalført post {} knyttet til ytelsetype {}", arkivJournalpost.getJournalpostId(), journalpostFagsakYtelseTypeDto);
        }

        LOG.info("FPSAK vurdering ytelsedok {} vs ytelseoppgitt {}", journalpostFagsakYtelseTypeDto, oppgittFagsakYtelseTypeDto);
        if (!oppgittFagsakYtelseTypeDto.equals(journalpostFagsakYtelseTypeDto)) {
            throw new FunksjonellException("FP-785359", "Dokument og valgt ytelsetype i uoverenstemmelse",
                "Velg ytelsetype som samstemmer med dokument");
        }
    }

}
