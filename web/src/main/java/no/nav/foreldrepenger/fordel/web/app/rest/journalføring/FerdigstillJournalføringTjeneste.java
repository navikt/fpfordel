package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.gjelderForeldrepenger;
import static no.nav.foreldrepenger.fordel.web.app.rest.journalføring.ManuellJournalføringMapper.mapYtelseTypeTilDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.journalføring.ManuellOpprettSakValidator;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.FerdigstillOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.FagsakYtelseTypeDto;
import no.nav.foreldrepenger.mottak.klient.OpprettSakV2Dto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.VLKlargjører;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.foreldrepenger.typer.JournalpostId;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class FerdigstillJournalføringTjeneste {

    static final String BRUKER_MANGLER = "Journalpost mangler knyting til bruker - prøv igjen om et halv minutt";
    static final String JOURNALPOST_IKKE_INNGÅENDE = "Journalpost ikke Inngående";
    private static final Logger LOG = LoggerFactory.getLogger(FerdigstillJournalføringTjeneste.class);
    private VLKlargjører klargjører;
    private Fagsak fagsak;
    private PersonInformasjon pdl;
    private Oppgaver oppgaver;
    private ProsessTaskTjeneste taskTjeneste;
    private ArkivTjeneste arkivTjeneste;
    private DokumentRepository dokumentRepository;

    FerdigstillJournalføringTjeneste() {
        //CDI
    }

    @Inject
    public FerdigstillJournalføringTjeneste(VLKlargjører klargjører,
                                            Fagsak fagsak,
                                            PersonInformasjon pdl,
                                            Oppgaver oppgaver,
                                            ProsessTaskTjeneste taskTjeneste,
                                            ArkivTjeneste arkivTjeneste,
                                            DokumentRepository dokumentRepository) {
        this.klargjører = klargjører;
        this.fagsak = fagsak;
        this.pdl = pdl;
        this.oppgaver = oppgaver;
        this.taskTjeneste = taskTjeneste;
        this.arkivTjeneste = arkivTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    public void oppdaterJournalpostOgFerdigstill(String enhetId, String saksnummer, JournalpostId journalpostId, FagsakYtelseTypeDto ytelseType, AktørId aktørId, String oppgaveId ) {

        final var journalpost = hentJournalpost(journalpostId.getVerdi());
        validerJournalposttype(journalpost.getJournalposttype());

        LOG.info("FPFORDEL RESTJOURNALFØRING: Ferdigstiller journalpostId: {}", journalpostId);

        if (saksnummer == null) {
        saksnummer = opprettSak(journalpostId, ytelseType, aktørId);
        }

        validerSaksnummer(saksnummer);

        var fagsakInfomasjon = hentOgValiderFagsak(saksnummer, journalpost);

        final var behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(fagsakInfomasjon.getBehandlingstemaOffisiellKode());
        final var aktørIdFagsak = fagsakInfomasjon.getAktørId();
        final var dokumentTypeId = journalpost.getHovedtype();
        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);
        final var behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);
        final var dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);

        validerKanJournalføreKlageDokument(behandlingTemaFagsak, dokumentTypeId, dokumentKategori);

        if (Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;
            if (!arkivTjeneste.oppdaterRettMangler(journalpost, aktørIdFagsak, behandlingTema, brukDokumentTypeId)) {
                throw new TekniskException("FP-15678", lagUgyldigInputMelding("Bruker", BRUKER_MANGLER));
            }
            try {
                arkivTjeneste.settTilleggsOpplysninger(journalpost, brukDokumentTypeId);
            } catch (Exception e) {
                LOG.info("FPFORDEL RESTJOURNALFØRING: Feil ved setting av tilleggsopplysninger for journalpostId {}", journalpost.getJournalpostId());
            }
            LOG.info("FPFORDEL RESTJOURNALFØRING: Kaller tilJournalføring"); // NOSONAR
            try {
                arkivTjeneste.oppdaterMedSak(journalpost.getJournalpostId(), saksnummer, aktørIdFagsak);
                arkivTjeneste.ferdigstillJournalføring(journalpost.getJournalpostId(), enhetId);
            } catch (Exception e) {
                LOG.warn("FPFORDEL RESTJOURNALFØRING: oppdaterJournalpostOgFerdigstill feiler for {}", journalpost.getJournalpostId(), e);
                throw new TekniskException("FP-15689", lagUgyldigInputMelding("Bruker", BRUKER_MANGLER));
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

        final var xml = hentDokumentSettMetadata(saksnummer, behandlingTema, aktørIdFagsak, journalpost);
        klargjører.klargjør(xml, saksnummer, journalpostId.getVerdi(), dokumentTypeId, mottattTidspunkt, behandlingTema, forsendelseId.orElse(null),
            dokumentKategori, enhetId, eksternReferanseId);

        // For å unngå klonede journalposter fra GOSYS - de kan komme via Kafka
        dokumentRepository.lagreJournalpostLokal(journalpost.getJournalpostId(), journalpost.getKanal(), "ENDELIG", journalpost.getEksternReferanseId());

        opprettFerdigstillOppgaveTask(oppgaveId);
    }

    public Optional<String> utledJournalpostTittelOgOppdater(List<DokumenterMedNyTittel> dokumenterMedNyTittel, JournalpostId journalpostId) {

        final var arkivJournalpost = hentJournalpost(journalpostId.getVerdi());

        LOG.info("FPFORDEL RESTJOURNALFØRING: Oppdaterer titler for journalpostId: {}", journalpostId.getVerdi());

        var opprinneligHovedType = arkivJournalpost.getHovedtype();
        var dokumenterFraArkiv = arkivJournalpost.getOriginalJournalpost().dokumenter();

        Set<DokumentTypeId> dokumenttyper = utledDokumentTyper(dokumenterMedNyTittel, dokumenterFraArkiv);

        var nyHovedtype = ArkivUtil.utledHovedDokumentType(dokumenttyper);
        var nyJournalpostTittel = skalOppdatereJournalpostTittel(nyHovedtype, opprinneligHovedType);
        var dokumenterÅOppdatere = mapDokumenterTilOppdatering(dokumenterMedNyTittel);

        arkivTjeneste.oppdaterJournalpostMedTitler(arkivJournalpost.getJournalpostId(), nyJournalpostTittel, dokumenterÅOppdatere);
        return nyJournalpostTittel;
    }

    public record DokumenterMedNyTittel(String dokumentId, String dokumentTittel) {}

    private List<no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest.DokumentInfoOppdater> mapDokumenterTilOppdatering(List<DokumenterMedNyTittel> dokumenter) {
        return dokumenter.stream()
            .filter(dt -> dt.dokumentId() != null && dt.dokumentTittel() != null)
            .map( dt -> new no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest.DokumentInfoOppdater(dt.dokumentId(), dt.dokumentTittel(),null))
            .toList();
    }

    private Optional<String> skalOppdatereJournalpostTittel(DokumentTypeId nyId, DokumentTypeId gammelId) {
        return nyId.equals(gammelId) ? Optional.empty() : Optional.of(nyId.getTermNavn());
    }

    private static Set<DokumentTypeId> utledDokumentTyper(List<DokumenterMedNyTittel> dokumenterMedNyTittel,
                                                          List<DokumentInfo> dokumenterFraArkiv) {
        Set<DokumentTypeId> dokumentTyper = new HashSet<>();
        Set<String> oppdatereDokIder =dokumenterMedNyTittel.stream().map(DokumenterMedNyTittel::dokumentId).collect(Collectors.toSet());

        for(DokumentInfo dokumentFraArkiv : dokumenterFraArkiv) {
            if (!oppdatereDokIder.contains(dokumentFraArkiv.dokumentInfoId())) {
                dokumentTyper.add(DokumentTypeId.fraTermNavn(dokumentFraArkiv.tittel()));
            }
        }
        for (DokumenterMedNyTittel dokumentNyTittel : dokumenterMedNyTittel) {
            dokumentTyper.add(DokumentTypeId.fraTermNavn(dokumentNyTittel.dokumentTittel()));
        }
        return dokumentTyper;
    }

    private ArkivJournalpost hentJournalpost(String arkivId) {
        try {
            return arkivTjeneste.hentArkivJournalpost(arkivId);
        } catch (Exception e) {
            LOG.warn("FORDEL fikk feil fra hentjournalpost: ", e);
            throw new TekniskException("FP-15676", lagUgyldigInputMelding("Journalpost", "Finner ikke journalpost med dokumentId " + arkivId));
        }
    }

    private static Optional<UUID> asUUID(String eksternReferanseId) {
        try {
            return Optional.of(UUID.fromString(eksternReferanseId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    String opprettSak(JournalpostId journalpostId, FagsakYtelseTypeDto ytelseType, AktørId aktørId) {
        String saksnummer;

        new ManuellOpprettSakValidator(arkivTjeneste, fagsak).validerKonsistensMedSak(journalpostId, ytelseType, aktørId);

        saksnummer = fagsak.opprettSak(new OpprettSakV2Dto(journalpostId.getVerdi(), mapYtelseTypeTilDto(ytelseType), aktørId.getId())).getSaksnummer();
        return saksnummer;
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

    private FagsakInfomasjonDto hentOgValiderFagsak(String saksnummer, ArkivJournalpost journalpost) {
        // Finn sak i fpsak med samme aktør
        final var brukerAktørId = journalpost.getBrukerAktørId();

        final var fagsakFraRequestSomTrefferRettAktør = hentFagsakInfo(saksnummer).filter(
            f -> brukerAktørId.isEmpty() || Objects.equals(f.getAktørId(), brukerAktørId.get()));

        if (fagsakFraRequestSomTrefferRettAktør.isEmpty()) {
            throw new FunksjonellException("FP-963070", "Kan ikke journalføre på saksnummer: " + saksnummer,
                "Journalføre dokument på annen sak i VL");
        }

        LOG.info("FPFORDEL RESTJOURNALFØRING: Fant en FP-sak med saksnummer {} som har rett aktør", saksnummer);
        return fagsakFraRequestSomTrefferRettAktør.get();
    }

    private Optional<FagsakInfomasjonDto> hentFagsakInfo(String saksnummerFraArkiv) {
        return fagsak.finnFagsakInfomasjon(new SaksnummerDto(saksnummerFraArkiv));
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
            LOG.info("FPFORDEL RESTJOURNALFØRING: Journalpost med type {} er strukturert men er ikke gyldig XML", dokumentTypeId);
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
                LOG.info("FPFORDEL RESTJOURNALFØRING: {}", logMessage);
            } else {
                throw e;
            }
        }
        var imType = dataWrapper.getInntektsmeldingYtelse().orElse(null);
        var startDato = dataWrapper.getOmsorgsovertakelsedato().orElse(dataWrapper.getFørsteUttaksdag().orElse(Tid.TIDENES_ENDE));
        validerDokumentData(dataWrapper, behandlingTema, dokumentTypeId, imType, startDato);
        return xml;
    }

    private static void validerSaksnummer(String saksnummer) {
        if (erNullEllerTom(saksnummer)) {
            throw new TekniskException("FP-15677", lagUgyldigInputMelding("Saksnummer", saksnummer));
        }
    }
    private static boolean erNullEllerTom(String s) {
        return ((s == null) || s.isEmpty());
    }

    private static void validerJournalposttype(Journalposttype type) {
        if (!Journalposttype.INNGÅENDE.equals(type)) {
            throw new TekniskException("FP-15680", lagUgyldigInputMelding("JournalpostType", JOURNALPOST_IKKE_INNGÅENDE));
        }
    }

    void opprettFerdigstillOppgaveTask(String oppgaveId) {
        if (oppgaveId != null) {
            try {
                oppgaver.ferdigstillOppgave(oppgaveId);
            } catch (Exception e) {
                LOG.warn("FPFORDEL RESTJOURNALFØRING: Ferdigstilt oppgave med dokumentId {} feiler ", oppgaveId, e);
                var ferdigstillOppgaveTask = ProsessTaskData.forProsessTask(FerdigstillOppgaveTask.class);
                ferdigstillOppgaveTask.setProperty(FerdigstillOppgaveTask.OPPGAVEID_KEY, oppgaveId);
                ferdigstillOppgaveTask.setCallIdFraEksisterende();
                taskTjeneste.lagre(ferdigstillOppgaveTask);
            }
        }
    }
}
