package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import com.google.common.collect.Sets;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilType;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.klient.AktørIdDto;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.Los;
import no.nav.foreldrepenger.mottak.klient.TilhørendeEnhetDto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.mapper.YtelseTypeMapper.mapTilDto;

@Path("/journalfoering")
@RequestScoped
@Transactional
@Unprotected
public class ManuellJournalføringRestTjeneste {
    private Oppgaver oppgaver;
    private PersonInformasjon pdl;
    private ArkivTjeneste arkiv;
    private Fagsak fagsak;
    private Los los;
    private final String LIMIT = "50";
    private static final Logger LOG = LoggerFactory.getLogger(ManuellJournalføringRestTjeneste.class);

    public ManuellJournalføringRestTjeneste() {
        // For inject
    }

    @Inject
    public ManuellJournalføringRestTjeneste(Oppgaver oppgaver,
                                            PersonInformasjon pdl,
                                            ArkivTjeneste arkiv,
                                            Fagsak fagsak,
                                            Los los) {
        this.oppgaver = oppgaver;
        this.pdl = pdl;
        this.arkiv = arkiv;
        this.fagsak = fagsak;
        this.los = los;
    }

    @GET
    @Path("/enhet")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(description = "Henter alle åpne journalføringsoppgaver for tema FOR og for saksbehandlers tilhørende enhet.", tags = "Manuell journalføring", responses = {
            @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),
    })
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public List<TilhørendeEnhetDto> hentTilhørendeEnhet(@TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class)
                                                            @NotNull @Valid SaksbehandlerIdentDto saksbehandlerIdentDto) {
        var enhetDtos = los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident());

        if (enhetDtos.isEmpty()) {
            throw new IllegalStateException(String.format("Det forventes at saksbehandler %s har minst en tilførende enhet. Fant ingen.", saksbehandlerIdentDto.ident()));
        }
        return enhetDtos;
    }

    @GET
    @Path("/oppgaver")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(description = "Henter alle åpne journalføringsoppgaver for tema FOR og for saksbehandlers tilhørende enhet.", tags = "Manuell journalføring", responses = {
            @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),
    })
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public List<OppgaveDto> hentÅpneOppgaver() throws Exception {
        var liste = oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), null, LIMIT);
        LOG.info("Hentet totalt {} journalføringsoppgaver fra Gosys", liste.size() );

        return liste.stream().map(this::lagOppgaveDto).toList();
    }

    @GET
    @Path("/oppgave/detaljer")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(description = "Henter detaljer for en gitt jornalpostId som er relevante for å kunne ferdigstille journalføring på en fagsak.", tags = "Manuell journalføring", responses = {
            @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),
    })
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public JournalpostDetaljerDto hentJournalpostDetaljer(
            @TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class)
            @QueryParam("journalpostId") @NotNull @Valid JournalpostIdDto journalpostId) {
        try {
            return Optional.ofNullable(arkiv.hentArkivJournalpost(journalpostId.getJournalpostId())).map(this::mapTilJournalpostDetaljerDto).orElseThrow();
        } catch (NoSuchElementException ex) {
            throw new TekniskException("FORDEL-123", "Journapost " + journalpostId.getJournalpostId() + " finnes ikke i arkivet.", ex);
        }
    }

    @GET
    @Path("/dokument/hent")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(description = "Søk etter dokument på JOARK-identifikatorene journalpostId og dokumentId", summary = ("Retunerer dokument som er tilknyttet journalpost og dokumentId."), tags = "Manuell journalføring")
    @BeskyttetRessurs(actionType = ActionType.READ, resourceType = ResourceType.FAGSAK)
    public Response hentDokument(@TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class) @QueryParam("journalpostId") @Valid JournalpostIdDto journalpostId,
                                 @TilpassetAbacAttributt(supplierClass = EmptyAbacDataSupplier.class) @QueryParam("dokumentId") @Valid DokumentIdDto dokumentId) {
        try {
            var responseBuilder = Response.ok(new ByteArrayInputStream(arkiv.hentDokumet(journalpostId.getJournalpostId(), dokumentId.getDokumentId())));
            responseBuilder.type("application/pdf");
            responseBuilder.header("Content-Disposition", "filename=dokument.pdf");
            return responseBuilder.build();
        } catch (Exception e) {
            var feilmelding = String.format("Dokument ikke funnet for journalpost= %s dokId= %s",
                    journalpostId.getJournalpostId(), dokumentId.getDokumentId());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new FeilDto(feilmelding, FeilType.TOMT_RESULTAT_FEIL))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }

    private JournalpostDetaljerDto mapTilJournalpostDetaljerDto(ArkivJournalpost journalpost) {
        return new JournalpostDetaljerDto(
                journalpost.getJournalpostId(),
                journalpost.getTittel().orElse(""),
                journalpost.getBehandlingstema().getOffisiellKode(),
                journalpost.getKanal(),
                journalpost.getBrukerAktørId().map(this::mapBruker).orElse(null),
                new JournalpostDetaljerDto.AvsenderDto(journalpost.getAvsenderNavn(), journalpost.getAvsenderIdent()),
                mapTilDto(journalpost.getBehandlingstema().utledYtelseType()),
                mapDokumenter(journalpost.getJournalpostId(), journalpost.getOriginalJournalpost().dokumenter()),
                mapBrukersFagsaker(journalpost.getBrukerAktørId().orElse(null))
        );
    }


    private Set<JournalpostDetaljerDto.FagsakDto> mapBrukersFagsaker(String aktørId) {
        if (aktørId != null) {
            return Set.of();
        }
        return fagsak.hentBrukersSaker(new AktørIdDto(aktørId)).stream()
                .map(sak -> new JournalpostDetaljerDto.FagsakDto(
                        sak.saksnummer().getSaksnummer(),
                        sak.ytelseType(),
                        sak.opprettetDato(),
                        sak.endretDato(),
                        sak.status()
                ))
                .collect(Collectors.toSet());
    }

    private static Set<JournalpostDetaljerDto.DokumentDto> mapDokumenter(String journalpostId, List<DokumentInfo> dokumenter) {
        return dokumenter.stream()
                .map(dok -> new JournalpostDetaljerDto.DokumentDto(
                        dok.dokumentInfoId(),
                        dok.tittel(),
                        String.format("/fpfordel/api/journalfoering/dokument/hent?journalpostId=%s&dokumentId=%s", journalpostId, dok.dokumentInfoId())))
                .collect(Collectors.toSet());
    }

    private JournalpostDetaljerDto.BrukerDto mapBruker(String aktørId) {
        if (aktørId != null) {
            var fnr = pdl.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
            var navn = pdl.hentNavn(aktørId);
            return new JournalpostDetaljerDto.BrukerDto(navn, fnr, aktørId);
        }
        return null;
    }

    private OppgaveDto lagOppgaveDto(Oppgave oppgave) {
        var trimmetBeskrivelse = tekstFraBeskrivelse(oppgave.beskrivelse());
        return new OppgaveDto(
                oppgave.id(),
                oppgave.journalpostId(),
                oppgave.aktoerId(),
                hentPersonIdent(oppgave.aktoerId()).orElse(null),
                mapTilYtelseType(oppgave.behandlingstema()),
                oppgave.fristFerdigstillelse(),
                mapPrioritet(oppgave.prioritet()),
                trimmetBeskrivelse,
                oppgave.aktivDato(),
                harJournalpostMangler(oppgave),
                oppgave.tildeltEnhetsnr(),
                mapJournalpostMangel(oppgave.aktoerId(), trimmetBeskrivelse));
    }

    private String  tekstFraBeskrivelse(String beskrivelse) {
        if (beskrivelse == null) {
            return "";
        }
        int i = beskrivelse.length();
        while (i > 0 && !(Character.isDigit(beskrivelse.charAt(i-1)) || beskrivelse.charAt(i-1) == ',')) i--;
        if (i < beskrivelse.length() && beskrivelse.charAt(i) == ' ') i++;
        if (i == beskrivelse.length() ) {
            return beskrivelse;
        }
        if (beskrivelse.substring(i).length() < 10) {
            var i2 = beskrivelse.length();
            while (i2 > 0 && (beskrivelse.charAt(i2-1) != ',')) i2--;
            return beskrivelse.substring(i2);
        }
        return beskrivelse.substring(i);
    }

    private OppgavePrioritet mapPrioritet(Prioritet prioritet) {
        return switch (prioritet) {
            case HOY -> OppgavePrioritet.HØY;
            case LAV -> OppgavePrioritet.LAV;
            case NORM -> OppgavePrioritet.NORM;
        };
    }

    private Optional<String> hentPersonIdent(String aktørId) {
        if (aktørId != null) {
            return pdl.hentPersonIdentForAktørId(aktørId);
        }
        return Optional.empty();
    }

    //Denne skal fjernes
    private boolean harJournalpostMangler(Oppgave oppgave) {
        return oppgave.aktoerId() == null || tekstFraBeskrivelse(oppgave.beskrivelse()).startsWith("Journalføring");
    }

    private List<JournalpostMangel> mapJournalpostMangel(String aktørId, String beskrivelse) {
        List<JournalpostMangel> mangler = new ArrayList<>();
        if (aktørId == null) {
            mangler.add(JournalpostMangel.MANGLER_BRUKER);
        }
        if (beskrivelse.startsWith("Journalføring")) {
            mangler.add(JournalpostMangel.MANGLER_TITTEL);
        }
        return mangler;
    }

    public enum JournalpostMangel {
        MANGLER_BRUKER,
        MANGLER_TITTEL
    }

    private String mapTilYtelseType(String behandlingstema) {
        var behandlingTemaMappet = BehandlingTema.fraOffisiellKode(behandlingstema);
        return switch (behandlingTemaMappet) {
            case FORELDREPENGER, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FØDSEL ->
                    BehandlingTema.FORELDREPENGER.getTermNavn();
            case SVANGERSKAPSPENGER -> BehandlingTema.SVANGERSKAPSPENGER.getTermNavn();
            case ENGANGSSTØNAD, ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_FØDSEL ->
                    BehandlingTema.ENGANGSSTØNAD.getTermNavn();
            default -> "Ukjent";
        };
    }

    public record OppgaveDto(@NotNull Long id,
                             @NotNull String journalpostId,
                             String aktørId,
                             String fødselsnummer,
                             @NotNull String ytelseType,
                             @NotNull LocalDate frist,
                             OppgavePrioritet prioritet,
                             String beskrivelse,
                             @NotNull LocalDate opprettetDato,
                             @NotNull boolean journalpostHarMangler,
                             String enhetId,
                             @NotNull List<JournalpostMangel> mangler) {

    }

    public enum OppgavePrioritet {
        HØY,
        NORM,
        LAV
    }

    public static class EmptyAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            return AbacDataAttributter.opprett();
        }
    }
}
