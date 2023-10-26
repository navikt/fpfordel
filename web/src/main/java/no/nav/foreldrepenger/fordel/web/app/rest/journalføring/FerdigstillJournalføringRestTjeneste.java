package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static no.nav.foreldrepenger.fordel.web.app.rest.journalføring.ManuellJournalføringMapper.mapYtelseTypeFraDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.fordel.web.server.abac.AppAbacAttributtType;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.klient.FagsakYtelseTypeDto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

/**
 * Enkelt REST tjeneste for å oppdatere og ferdigstille journalføring på dokumenter som kunne ikke
 * journalføres automatisk på fpsak saker. Brukes for å klargjøre og sende over saken til videre behandling i VL.
 * Gir mulighet å opprette saken i fpsak og så journalføre dokumentet på den nye saken.
 */
@Path("/sak")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
@Transactional
public class FerdigstillJournalføringRestTjeneste {
    private FerdigstillJournalføringTjeneste journalføringTjeneste;
    private static final Logger LOG = LoggerFactory.getLogger(FerdigstillJournalføringRestTjeneste.class);
    private static final String EXCEPTION_KODE = "FP-32354";


    protected FerdigstillJournalføringRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public FerdigstillJournalføringRestTjeneste(FerdigstillJournalføringTjeneste journalføringTjeneste) {
        this.journalføringTjeneste = journalføringTjeneste;
    }

    @POST
    @Path("/ferdigstill")
    @Operation(description = "For å ferdigstille journalføring. Det opprettes en ny fagsak om saksnummer ikke sendes.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "200", description = "Journalføring ferdigstillt"), @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    public SaksnummerDto oppdaterOgFerdigstillJournalfoering(@Parameter(description = "Trenger journalpostId, saksnummer og enhet til ferdigstille en journalføring. "
            + "Om saksnummer ikke foreligger må ytelse type og aktørId oppgis for å opprette en ny sak.") @NotNull @Valid
            @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) FerdigstillJournalføringRestTjeneste.FerdigstillRequest request) {

        validerJournalpostId(request.journalpostId());
        validerEnhetId(request.enhetId());
        var journalpostId = JournalpostId.fra(request.journalpostId());

        // sikre at finnes før oppretting av sak
        var journalpost = journalføringTjeneste.hentJournalpost(request.journalpostId());
        if (journalpost == null) {
            throw new TekniskException(EXCEPTION_KODE, "Finner ikke journalpost.");
        }

        LOG.info("FPFORDEL RESTJOURNALFØRING: Starter ferdigstilling av journalpostRequets {}", request);

        //Endring av titler
        List<FerdigstillJournalføringTjeneste.DokumenterMedNyTittel> dokumenter = new ArrayList<>();
        String nyJournalpostTittel = null;
        DokumentTypeId nyDokumentTypeId = null;
        if (request.oppdaterTitlerDto != null) {
            if (!request.oppdaterTitlerDto.dokumenter().isEmpty()) {
                dokumenter = mapTilDokumenter(request.oppdaterTitlerDto.dokumenter());
            }
            nyJournalpostTittel = request.oppdaterTitlerDto().journalpostTittel() != null ? request.oppdaterTitlerDto().journalpostTittel() : null;
            if (nyJournalpostTittel != null) {
                nyDokumentTypeId = DokumentTypeId.fraTermNavn(nyJournalpostTittel);
            }
            LOG.info("FPFORDEL RESTJOURNALFØRING:Ny journalposttittel er: {}, ny dokumentTypeId: {}", nyJournalpostTittel, nyDokumentTypeId);
        }

        var saksnummer = request.saksnummer() != null ? request.saksnummer() : null;

        if (saksnummer == null) {
            if (request.opprettSak() == null || (!SakstypeDto.GENERELL.equals(request.opprettSak().sakstype()) && request.opprettSak().ytelseType() == null)) {
                throw new TekniskException(EXCEPTION_KODE, "OpprettSakDto kan ikke være null ved opprettelse av en sak eller mangler ytelsestype.");
            }
            if (SakstypeDto.GENERELL.equals(request.opprettSak().sakstype())) {
                journalføringTjeneste.oppdaterJournalpostOgFerdigstillGenerellSak(request.enhetId, journalpost, request.opprettSak().aktørId(),
                    nyJournalpostTittel, dokumenter, nyDokumentTypeId);
                return new SaksnummerDto("000000000");
            }
            saksnummer = journalføringTjeneste.opprettSak(journalpost, mapOpprettSak(request.opprettSak()), nyDokumentTypeId);
        }

        validerSaksnummer(saksnummer);

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(request.enhetId, saksnummer, journalpost, nyJournalpostTittel, dokumenter, nyDokumentTypeId);

        return new SaksnummerDto(saksnummer);
    }

    @POST
    @Path("/knyttTilAnnenSak")
    @Operation(description = "For å knytte journalpost til annen sak. Det opprettes en ny fagsak om saksnummer ikke sendes.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "200", description = "Journalføring ferdigstillt"), @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    public JournalpostIdDto knyttTilAnnenSak(@Parameter(description = "Trenger journalpostId, saksnummer og enhet for å knytte til annen sak. "
        + "Om saksnummer ikke foreligger må ytelse type og aktørId oppgis for å opprette en ny sak.") @NotNull @Valid
                                                             @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) FerdigstillJournalføringRestTjeneste.FerdigstillRequest request) {

        validerJournalpostId(request.journalpostId());
        validerEnhetId(request.enhetId());

        // sikre at finnes før oppretting av sak
        var journalpost = journalføringTjeneste.hentJournalpost(request.journalpostId());
        if (journalpost == null) {
            throw new TekniskException(EXCEPTION_KODE, "Finner ikke journalpost.");
        }
        if (Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            throw new TekniskException(EXCEPTION_KODE, "Feil tilstand på journalpost " + journalpost.getJournalpostId());
        }

        LOG.info("FPFORDEL RESTJOURNALFØRING: Starter knytting av journalpost til annen/ny sak {}", request);

        //Endring av titler
        //var journalpost = jo.
        var saksnummer = request.saksnummer() != null ? request.saksnummer() : null;

        if (saksnummer == null) {
            if (request.opprettSak() == null || SakstypeDto.GENERELL.equals(request.opprettSak().sakstype()) || request.opprettSak().ytelseType() == null) {
                throw new TekniskException(EXCEPTION_KODE, "OpprettSakDto kan ikke være null ved opprettelse av en sak eller mangler ytelsestype.");
            }
            saksnummer = journalføringTjeneste.opprettSak(journalpost, mapOpprettSak(request.opprettSak()), journalpost.getHovedtype());
        }

        validerSaksnummer(saksnummer);

        var nyJournalpost = journalføringTjeneste.knyttTilAnnenSak(journalpost, request.enhetId, saksnummer);

        return Optional.ofNullable(nyJournalpost).map(JournalpostId::getVerdi).map(JournalpostIdDto::new).orElse(null);
    }

    private OpprettSak mapOpprettSak(OpprettSakDto opprettSakDto) {
        if (opprettSakDto == null) {
            throw new TekniskException(EXCEPTION_KODE, "OpprettSakDto kan ikke være null ved opprettelse av en sak.");
        }
        return new OpprettSak(new AktørId(opprettSakDto.aktørId), mapYtelseTypeFraDto(opprettSakDto.ytelseType));
    }

    public record OpprettSak(AktørId aktørId, FagsakYtelseTypeDto ytelseType ){}

    private List<FerdigstillJournalføringTjeneste.DokumenterMedNyTittel> mapTilDokumenter(List<OppdaterJournalpostMedTittelDto.DokummenterMedTitler> dokumenter) {
        return dokumenter.stream().map(d -> new FerdigstillJournalføringTjeneste.DokumenterMedNyTittel(d.dokumentId(), d.tittel())).toList();
    }

    private static void validerSaksnummer(String saksnummer) {
        if (erNullEllerTom(saksnummer)) {
            throw new TekniskException("FP-15677", lagUgyldigInputMelding("Saksnummer", saksnummer));
        }
    }

    private static void validerEnhetId(String enhetId) {
        if (enhetId == null) {
            throw new TekniskException("FP-15679", lagUgyldigInputMelding("EnhetId", enhetId));
        }
    }

    private static void validerJournalpostId(String journalpostId) {
        if (erNullEllerTom(journalpostId)) {
            throw new TekniskException("FP-15688", lagUgyldigInputMelding("JournalpostId", journalpostId));
        }
    }

    private static boolean erNullEllerTom(String s) {
        return ((s == null) || s.isEmpty());
    }

    private static String lagUgyldigInputMelding(String feltnavn, String verdi) {
        return String.format("Ugyldig input: %s med verdi: %s er ugyldig input.", feltnavn, verdi);
    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            var req = (FerdigstillRequest) obj;
            var opprett = AbacDataAttributter.opprett();
            if (req.opprettSak() != null) {
                opprett.leggTil(AppAbacAttributtType.AKTØR_ID, req.opprettSak().aktørId());
            }
            return opprett;
        }
    }

    enum SakstypeDto { FAGSAK, GENERELL }

    record OpprettSakDto(@Valid YtelseTypeDto ytelseType, @Valid FerdigstillJournalføringRestTjeneste.SakstypeDto sakstype,
                         @NotNull @Pattern(regexp = "^\\d{13}$", message = "aktørId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')") String aktørId) {
    }

    record FerdigstillRequest(
        @NotNull @Pattern(regexp = "^(-?[1-9]|[a-z0])[a-z0-9_:-]*$", message = "journalpostId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')") String journalpostId,
        @NotNull String enhetId,
        @Size(max = 11) @Pattern(regexp = "^[0-9_\\-]*$") String saksnummer,
        @Valid OpprettSakDto opprettSak,
        @Valid OppdaterJournalpostMedTittelDto oppdaterTitlerDto ) {}

}
