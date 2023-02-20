package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.gjelderForeldrepenger;
import static no.nav.foreldrepenger.mapper.YtelseTypeMapper.mapFraDto;
import static no.nav.foreldrepenger.mapper.YtelseTypeMapper.mapTilDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.fordel.web.server.abac.AppAbacAttributtType;
import no.nav.foreldrepenger.journalføring.ManuellOpprettSakValidator;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.FerdigstillOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.OpprettSakV2Dto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.VLKlargjører;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.foreldrepenger.typer.JournalpostId;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.konfig.Tid;
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
@Unprotected // midlertidig, fram til vi skrur over
public class FerdigstillJournalføringRestTjeneste {
    static final String JOURNALPOST_IKKE_INNGÅENDE = "Journalpost ikke Inngående";
    static final String BRUKER_MANGLER = "Journalpost mangler knyting til bruker - prøv igjen om et halv minutt";
    private static final Logger LOG = LoggerFactory.getLogger(FerdigstillJournalføringRestTjeneste.class);
    private VLKlargjører klargjører;
    private Fagsak fagsak;
    private PersonInformasjon pdl;
    private ArkivTjeneste arkivTjeneste;
    private DokumentRepository dokumentRepository;
    private Oppgaver oppgaver;
    private ProsessTaskTjeneste taskTjeneste;

    protected FerdigstillJournalføringRestTjeneste() {
        // CDI proxy
    }

    @Inject
    public FerdigstillJournalføringRestTjeneste(VLKlargjører klargjører,
                                                Fagsak fagsak,
                                                PersonInformasjon pdl,
                                                ArkivTjeneste arkivTjeneste,
                                                Oppgaver oppgaver,
                                                ProsessTaskTjeneste taskTjeneste,
                                                DokumentRepository dokumentRepository) {
        this.klargjører = klargjører;
        this.fagsak = fagsak;
        this.pdl = pdl;
        this.arkivTjeneste = arkivTjeneste;
        this.oppgaver = oppgaver;
        this.taskTjeneste = taskTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    private static Optional<UUID> asUUID(String eksternReferanseId) {
        try {
            return Optional.of(UUID.fromString(eksternReferanseId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static BehandlingTema validerOgVelgBehandlingTema(BehandlingTema behandlingTemaFagsak,
                                                              BehandlingTema behandlingTemaDok,
                                                              DokumentTypeId dokumentTypeId) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTemaDok)) {
            return behandlingTemaFagsak;
        }
        if (BehandlingTema.UDEFINERT.equals(behandlingTemaFagsak)) {
            return behandlingTemaDok;
        }
        if (!DokumentTypeId.erSøknadType(dokumentTypeId)) {
            return behandlingTemaFagsak;
        }
        if ((gjelderForeldrepenger(behandlingTemaFagsak) && !gjelderForeldrepenger(behandlingTemaDok)) || (
            BehandlingTema.gjelderEngangsstønad(behandlingTemaFagsak) && !BehandlingTema.gjelderEngangsstønad(behandlingTemaDok)) || (
            BehandlingTema.gjelderSvangerskapspenger(behandlingTemaFagsak) && !BehandlingTema.gjelderSvangerskapspenger(behandlingTemaDok))) {
            throw new FunksjonellException("FP-963079", "Dokumentet samsvarer ikke med sakens type - kan ikke journalføre",
                "Journalfør på annen sak eller opprett ny sak");
        }
        return BehandlingTema.ikkeSpesifikkHendelse(behandlingTemaDok) ? behandlingTemaFagsak : behandlingTemaDok;
    }

    private static void validerKanJournalføreKlageDokument(BehandlingTema behandlingTema,
                                                           DokumentTypeId dokumentTypeId,
                                                           DokumentKategori dokumentKategori) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTema) && (DokumentTypeId.KLAGE_DOKUMENT.equals(dokumentTypeId)
            || DokumentKategori.KLAGE_ELLER_ANKE.equals(dokumentKategori))) {
            throw new FunksjonellException("FP-963074", "Klager må journalføres på sak med tidligere behandling",
                "Journalføre klagen på sak med avsluttet behandling");
        }
    }

    private static void validerSaksnummer(String saksnummer) {
        if (erNullEllerTom(saksnummer)) {
            throw new TekniskException("FP-15677", lagUgyldigInputMelding("Saksnummer", saksnummer));
        }
    }

    private static void validerJournalpostId(String journalpostId) {
        if (erNullEllerTom(journalpostId)) {
            throw new TekniskException("FP-15688", lagUgyldigInputMelding("JournalpostId", journalpostId));
        }
    }

    private static void validerEnhetId(String enhetId) {
        if (enhetId == null) {
            throw new TekniskException("FP-15679", lagUgyldigInputMelding("EnhetId", enhetId));
        }
    }

    private static void validerJournalposttype(Journalposttype type) {
        if (!Journalposttype.INNGÅENDE.equals(type)) {
            throw new TekniskException("FP-15680", lagUgyldigInputMelding("JournalpostType", JOURNALPOST_IKKE_INNGÅENDE));
        }
    }

    private static boolean erNullEllerTom(String s) {
        return ((s == null) || s.isEmpty());
    }

    private static void validerDokumentData(MottakMeldingDataWrapper dataWrapper,
                                            BehandlingTema behandlingTema,
                                            DokumentTypeId dokumentTypeId,
                                            String imType,
                                            LocalDate startDato) {
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            var behandlingTemaFraIM = BehandlingTema.fraTermNavn(imType);
            if (gjelderForeldrepenger(behandlingTemaFraIM)) {
                if (dataWrapper.getInntektsmeldingStartDato().isEmpty()) { // Kommer ingen vei uten startdato
                    throw new FunksjonellException("FP-963076", "Inntektsmelding mangler startdato - kan ikke journalføre",
                        "Be om ny Inntektsmelding med startdato");

                } else if (!gjelderForeldrepenger(behandlingTema)) { // Prøver journalføre på annen
                    // fagsak - ytelsetype
                    throw new FunksjonellException("FP-963075", "Inntektsmelding årsak samsvarer ikke med sakens type - kan ikke journalføre",
                        "Be om ny Inntektsmelding for Foreldrepenger");
                }
            } else if (!behandlingTemaFraIM.equals(behandlingTema)) {
                throw new FunksjonellException("FP-963075", "Inntektsmelding årsak samsvarer ikke med sakens type - kan ikke journalføre",
                    "Be om ny Inntektsmelding for Foreldrepenger");
            }
        }
        if (gjelderForeldrepenger(behandlingTema) && startDato.isBefore(KonfigVerdier.ENDRING_BEREGNING_DATO)) {
            throw new FunksjonellException("FP-963077", "For tidlig uttak",
                "Søknad om uttak med oppstart i 2018 skal journalføres mot sak i Infotrygd");
        }
    }

    private static String lagUgyldigInputMelding(String feltnavn, String verdi) {
        return String.format("Ugyldig input: %s med verdi: %s er ugyldig input.", feltnavn, verdi);
    }

    @POST
    @Path("/ferdigstill")
    @Operation(description = "For å ferdigstille journalføring. Det opprettes en ny fagsak om saksnummer ikke sendes.", tags = "Manuell journalføring", responses = {@ApiResponse(responseCode = "200", description = "Journalføring ferdigstillt"), @ApiResponse(responseCode = "500", description = "Feil i request", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = FeilDto.class))),})
    @BeskyttetRessurs(actionType = ActionType.CREATE, resourceType = ResourceType.FAGSAK)
    public void oppdaterOgFerdigstillJournalfoering(@Parameter(description = "Trenger journalpostId, saksnummer og enhet til ferdigstille en journalføring. "
            + "Om saksnummer ikke foreligger må ytelse type og aktørId oppgis for å opprette en ny sak.") @NotNull @Valid
            @TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) FerdigstillJournalføringRestTjeneste.FerdigstillRequest request) {

        validerJournalpostId(request.journalpostId());
        validerEnhetId(request.enhetId());

        final var saksnummer = Optional.ofNullable(request.saksnummer()).orElseGet(() -> opprettSak(request.opprettSak(), request.journalpostId()));

        validerSaksnummer(saksnummer);

        final var journalpost = hentJournalpost(request.journalpostId());

        validerJournalposttype(journalpost.getJournalposttype());

        var fagsakInfomasjon = hentOgValiderFagsak(saksnummer, journalpost);

        final var behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(fagsakInfomasjon.getBehandlingstemaOffisiellKode());
        final var aktørIdFagsak = fagsakInfomasjon.getAktørId();

        final var dokumentTypeId = journalpost.getHovedtype();
        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);

        final var behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);

        final var dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        validerKanJournalføreKlageDokument(behandlingTemaFagsak, dokumentTypeId, dokumentKategori);

        final var xml = hentDokumentSettMetadata(saksnummer, behandlingTema, aktørIdFagsak, journalpost);

        if (Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;
            if (!arkivTjeneste.oppdaterRettMangler(journalpost, aktørIdFagsak, behandlingTema, brukDokumentTypeId)) {
                throw new TekniskException("FP-15678", lagUgyldigInputMelding("Bruker", BRUKER_MANGLER));
            }
            try {
                arkivTjeneste.settTilleggsOpplysninger(journalpost, brukDokumentTypeId);
            } catch (Exception e) {
                LOG.info("FPFORDEL JOURNALFØRING Feil ved setting av tilleggsopplysninger for journalpostId {}", journalpost.getJournalpostId());
            }
            LOG.info("FPFORDEL JOURNALFØRING Kaller tilJournalføring"); // NOSONAR
            try {
                arkivTjeneste.oppdaterMedSak(journalpost.getJournalpostId(), saksnummer, aktørIdFagsak);
                arkivTjeneste.ferdigstillJournalføring(journalpost.getJournalpostId(), request.enhetId());
            } catch (Exception e) {
                LOG.warn("FPFORDEL JOURNALFØRING oppdaterOgFerdigstillJournalfoering feiler for {}", journalpost.getJournalpostId(), e);
                throw new TekniskException("FP-15689", lagUgyldigInputMelding("Bruker", BRUKER_MANGLER), e);
            }
        }

        final var forsendelseId = asUUID(journalpost.getEksternReferanseId());

        String eksternReferanseId = null;
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            eksternReferanseId =
                journalpost.getEksternReferanseId() != null ? journalpost.getEksternReferanseId() : arkivTjeneste.hentEksternReferanseId(
                    journalpost.getOriginalJournalpost()).orElse(null);
        }

        var mottattTidspunkt = Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now);

        klargjører.klargjør(xml, saksnummer, request.journalpostId(), dokumentTypeId, mottattTidspunkt, behandlingTema, forsendelseId.orElse(null),
            dokumentKategori, request.enhetId(), eksternReferanseId);

        // For å unngå klonede journalposter fra GOSYS - de kan komme via Kafka
        dokumentRepository.lagreJournalpostLokal(request.journalpostId(), journalpost.getKanal(), "ENDELIG", journalpost.getEksternReferanseId());

        if (request.oppgaveId() != null) {
            var oppgaveId = String.valueOf(request.oppgaveId());
            try {
                oppgaver.ferdigstillOppgave(oppgaveId);
            } catch (Exception e) {
                LOG.warn("Ferdigstilt oppgave med id {} feiler ", oppgaveId, e);
                var ferdigstillOppgaveTask = ProsessTaskData.forProsessTask(FerdigstillOppgaveTask.class);
                ferdigstillOppgaveTask.setProperty(FerdigstillOppgaveTask.OPPGAVEID_KEY, oppgaveId);
                ferdigstillOppgaveTask.setCallIdFraEksisterende();
                taskTjeneste.lagre(ferdigstillOppgaveTask);
            }
        }
    }

    private FagsakInfomasjonDto hentOgValiderFagsak(String saksnummer, ArkivJournalpost journalpost) {
        // Finn sak i fpsak med samme aktør
        final var brukerAktørId = journalpost.getBrukerAktørId();

        final var fagsakFraRequestSomTrefferRettAktør = hentFagsakInfo(saksnummer).filter(
            f -> brukerAktørId.isEmpty() || Objects.equals(f.getAktørId(), brukerAktørId.get()));

        if (fagsakFraRequestSomTrefferRettAktør.isEmpty()) {
            throw new FunksjonellException("FP-963070", "Kan ikke journalføre på saksnummer: " + saksnummer,
                "Journalføre dokument på annen sak i VL");
        }

        LOG.info("FPFORDEL JOURNALFØRING Fant en FP-sak med saksnummer {} som har rett aktør", saksnummer);
        return fagsakFraRequestSomTrefferRettAktør.get();
    }

    private String opprettSak(OpprettSakDto request, String journalpost) {
        String saksnummer;
        var opprettSak = Optional.ofNullable(request)
            .orElseThrow(() -> new TekniskException("FP-32354", "OpprettSakDto kan ikke være null ved opprettelse av en sak."));

        var journalpostId = new JournalpostId(journalpost);
        var ytelseType = mapFraDto(opprettSak.ytelseType());
        var aktørId = new AktørId(opprettSak.aktørId());

        new ManuellOpprettSakValidator(arkivTjeneste, fagsak).validerKonsistensMedSak(journalpostId, ytelseType, aktørId);

        saksnummer = fagsak.opprettSak(new OpprettSakV2Dto(journalpostId.getVerdi(), mapTilDto(ytelseType), aktørId.getId())).getSaksnummer();
        return saksnummer;
    }

    private Optional<FagsakInfomasjonDto> hentFagsakInfo(String saksnummerFraArkiv) {
        return fagsak.finnFagsakInfomasjon(new SaksnummerDto(saksnummerFraArkiv));
    }

    private ArkivJournalpost hentJournalpost(String arkivId) {
        try {
            return arkivTjeneste.hentArkivJournalpost(arkivId);
        } catch (Exception e) {
            LOG.warn("FORDEL WS fikk feil fra hentjournalpost: ", e);
            throw new TekniskException("FP-15676", lagUgyldigInputMelding("Journalpost", "Finner ikke journalpost med id " + arkivId));
        }
    }

    private String hentDokumentSettMetadata(String saksnummer, BehandlingTema behandlingTema, String aktørId, ArkivJournalpost journalpost) {
        final var xml = journalpost.getStrukturertPayload();
        if (journalpost.getInnholderStrukturertInformasjon()) {
            // Bruker eksisterende infrastruktur for å hente ut og validere XML-data.
            // Tasktype tilfeldig valgt
            var prosessTaskData = ProsessTaskData.forProsessTask(VLKlargjørerTask.class);
            var dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
            dataWrapper.setBehandlingTema(behandlingTema);
            dataWrapper.setSaksnummer(saksnummer);
            dataWrapper.setAktørId(aktørId);
            return validerXml(dataWrapper, behandlingTema, journalpost.getHovedtype(), xml);
        }
        return xml;
    }

    private String validerXml(MottakMeldingDataWrapper dataWrapper, BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, String xml) {
        MottattStrukturertDokument<?> mottattDokument;
        try {
            mottattDokument = MeldingXmlParser.unmarshallXml(xml);
        } catch (Exception e) {
            LOG.info("FPFORDEL JOURNALFØRING Journalpost med type {} er strukturert men er ikke gyldig XML", dokumentTypeId);
            return null;
        }
        if (DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD.equals(dokumentTypeId) && !BehandlingTema.ikkeSpesifikkHendelse(behandlingTema)) {
            dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        }
        try {
            mottattDokument.kopierTilMottakWrapper(dataWrapper, pdl::hentAktørIdForPersonIdent);
        } catch (FunksjonellException e) {
            // Her er det "greit" - da har man bestemt seg, men kan lage rot i saken.
            if ("FP-401245".equals(e.getKode())) {
                var logMessage = e.getMessage();
                LOG.info("FPFORDEL JOURNALFØRING {}", logMessage);
            } else {
                throw e;
            }
        }
        var imType = dataWrapper.getInntektsmeldingYtelse().orElse(null);
        var startDato = dataWrapper.getOmsorgsovertakelsedato().orElse(dataWrapper.getFørsteUttaksdag().orElse(Tid.TIDENES_ENDE));
        validerDokumentData(dataWrapper, behandlingTema, dokumentTypeId, imType, startDato);
        return xml;
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

    record OpprettSakDto(@NotNull YtelseTypeDto ytelseType,
                         @NotNull @Pattern(regexp = "^\\d{13}$", message = "aktørId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')") String aktørId) {
    }

    record FerdigstillRequest(
        @NotNull @Pattern(regexp = "^(-?[1-9]|[a-z0])[a-z0-9_:-]*$", message = "journalpostId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')") String journalpostId,
        @NotNull String enhetId,
        @Size(max = 11) @Pattern(regexp = "^[0-9_\\-]*$") String saksnummer,
        Long oppgaveId,
        @Valid OpprettSakDto opprettSak) {
    }
}
