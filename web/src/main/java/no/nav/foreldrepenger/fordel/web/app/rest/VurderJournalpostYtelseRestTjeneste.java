package no.nav.foreldrepenger.fordel.web.app.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostVurderingDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonTjeneste;
import no.nav.foreldrepenger.mottak.task.joark.HentDataFraJoarkTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.sikkerhet.abac.BeskyttetRessursAttributt;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;

@Path("/vurdering")
@RequestScoped
@Transactional
public class VurderJournalpostYtelseRestTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(VurderJournalpostYtelseRestTjeneste.class);

    private ArkivTjeneste arkivTjeneste;
    private PersonTjeneste aktørConsumer;

    public VurderJournalpostYtelseRestTjeneste() {
        // CDI
    }

    @Inject
    public VurderJournalpostYtelseRestTjeneste(ArkivTjeneste arkivTjeneste,
                                               PersonTjeneste aktørConsumer) {
        this.aktørConsumer = aktørConsumer;
        this.arkivTjeneste = arkivTjeneste;
    }

    @GET
    @Path("/ytelsetype")
    @Produces(APPLICATION_JSON)
    @BeskyttetRessurs(action = READ, resource = BeskyttetRessursAttributt.FAGSAK)
    @Operation(description = "Returnerer ES, FP, SVP, IMFP, IMSVP eller '-' (udefinert)", tags = "Vurdering", responses = {
            @ApiResponse(responseCode = "200", description = "Svar"),
            @ApiResponse(responseCode = "500", description = "Feilet pga ukjent feil.")
    })
    public Response utledYtelsetypeForSak(
            @QueryParam("journalpostId") @Parameter(name = "journalpostId", description = "Journalpost ID", example = "false") @NotNull @Valid AbacJournalpostIdDto journalpostIdDto) {
        return Response.ok(utledBehandlingstemaFra(journalpostIdDto.getJournalpostId())).build();
    }

    JournalpostVurderingDto utledBehandlingstemaFra(String journalpostId) {
        try {
            var ajp = arkivTjeneste.hentArkivJournalpost(journalpostId);
            LOG.info("FPFORDEL VURDERING journalpost {} dokumenttype {}", journalpostId, ajp.getHovedtype());
            if (!Journalposttype.INNGÅENDE.equals(ajp.getJournalposttype())) {
                throw new IllegalArgumentException("Journalpost ikke inngående " + journalpostId);
            }
            if (DokumentTypeId.erFørsteSøknadType(ajp.getHovedtype())) {
                if (DokumentTypeId.erForeldrepengerRelatert(ajp.getHovedtype())) {
                    return new JournalpostVurderingDto(BehandlingTema.FORELDREPENGER.getOffisiellKode(), true, false);
                }
                if (DokumentTypeId.erEngangsstønadRelatert(ajp.getHovedtype())) {
                    return new JournalpostVurderingDto(BehandlingTema.ENGANGSSTØNAD.getOffisiellKode(), true, false);
                }
                if (DokumentTypeId.erSvangerskapspengerRelatert(ajp.getHovedtype())) {
                    return new JournalpostVurderingDto(BehandlingTema.SVANGERSKAPSPENGER.getOffisiellKode(), true, false);
                }
            }
            if (DokumentTypeId.INNTEKTSMELDING.equals(ajp.getHovedtype()) && ajp.getInnholderStrukturertInformasjon()) {
                var taskdata = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
                MottakMeldingDataWrapper testWrapper = new MottakMeldingDataWrapper(taskdata);
                MottattStrukturertDokument<?> mottattDokument = MeldingXmlParser.unmarshallXml(ajp.getStrukturertPayload());
                mottattDokument.kopierTilMottakWrapper(testWrapper, aktørConsumer::hentAktørIdForPersonIdent);
                BehandlingTema temaFraIM = testWrapper.getInntektsmeldingYtelse().map(BehandlingTema::fraTermNavn).orElse(BehandlingTema.UDEFINERT);
                LOG.info("FPFORDEL VURDERING IM journalpost {} dokumenttype {} behtema {}", journalpostId, ajp.getHovedtype(), temaFraIM);
                if (BehandlingTema.gjelderForeldrepenger(temaFraIM)) {
                    return new JournalpostVurderingDto(BehandlingTema.FORELDREPENGER.getOffisiellKode(), false, true);
                }
                if (BehandlingTema.gjelderSvangerskapspenger(temaFraIM)) {
                    return new JournalpostVurderingDto(BehandlingTema.SVANGERSKAPSPENGER.getOffisiellKode(), false, true);
                }
            }
        } catch (Exception e) {
            LOG.info("FPFORDEL VURDERING feil", e);
        }
        return new JournalpostVurderingDto(BehandlingTema.UDEFINERT.getOffisiellKode(), false, false);
    }

    public static class AbacJournalpostIdDto extends JournalpostIdDto implements AbacDto {
        public AbacJournalpostIdDto() {
            super();
        }

        public AbacJournalpostIdDto(String journalpostId) {
            super(journalpostId);
        }

        @Override
        public AbacDataAttributter abacAttributter() {
            return AbacDataAttributter.opprett();
        }
    }

}
