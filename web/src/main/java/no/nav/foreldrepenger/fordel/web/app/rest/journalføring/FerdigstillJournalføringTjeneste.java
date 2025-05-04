package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.gjelderAdopsjon;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.gjelderForeldrepenger;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.gjelderFødsel;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ikkeSpesifikkHendelse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.journalføring.ManuellOpprettSakValidator;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakV2Dto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.YtelseTypeDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.FerdigstillOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.FagsakYtelseTypeDto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.VLKlargjører;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.konfig.Tid;

@ApplicationScoped
public class FerdigstillJournalføringTjeneste {

    static final String BRUKER_MANGLER = "Journalpost mangler knyting til bruker - prøv igjen om et halv minutt";
    static final String JOURNALPOST_IKKE_INNGÅENDE = "Journalpost ikke Inngående";
    private static final Logger LOG = LoggerFactory.getLogger(FerdigstillJournalføringTjeneste.class);
    protected static final String ENDELIG = "ENDELIG";
    private VLKlargjører klargjører;
    private Fagsak fagsak;
    private PersonInformasjon pdl;
    private Journalføringsoppgave oppgaver;
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
                                            Journalføringsoppgave oppgaver,
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

    public void oppdaterJournalpostOgFerdigstill(String enhetId,
                                                 String saksnummer,
                                                 ArkivJournalpost journalpost,
                                                 String nyJournalpostTittel,
                                                 List<DokumenterMedNyTittel> dokumenterMedNyTittel,
                                                 DokumentTypeId nyDokumentTypeId) {

        validerJournalposttype(journalpost.getJournalposttype());

        LOG.info("FPFORDEL RESTJOURNALFØRING: Ferdigstiller journalpostId: {}", journalpost.getJournalpostId());

        var fagsakInfomasjon = hentOgValiderFagsak(saksnummer, journalpost);

        final var behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(fagsakInfomasjon.behandlingstemaOffisiellKode());
        final var aktørIdFagsak = fagsakInfomasjon.aktørId();

        var dokumentTypeId = journalpost.getHovedtype();
        var oppdatereTitler = nyJournalpostTittel != null || !dokumenterMedNyTittel.isEmpty();
        if (nyDokumentTypeId != null) {
            dokumentTypeId = nyDokumentTypeId;
        }

        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);
        final var behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);
        final var dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;

        validerKanJournalføreKlageDokument(behandlingTemaFagsak, brukDokumentTypeId, dokumentKategori);

        if (Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            oppdaterJournalpostMedTittelOgMangler(journalpost, nyJournalpostTittel, dokumenterMedNyTittel, aktørIdFagsak, behandlingTema);
            try {
                arkivTjeneste.settTilleggsOpplysninger(journalpost, brukDokumentTypeId, oppdatereTitler);
            } catch (Exception e) {
                LOG.info("FPFORDEL RESTJOURNALFØRING: Feil ved setting av tilleggsopplysninger for journalpostId {}", journalpost.getJournalpostId());
            }
            LOG.info("FPFORDEL RESTJOURNALFØRING: Kaller til Journalføring"); // NOSONAR
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
        if (DokumentTypeId.INNTEKTSMELDING.equals(brukDokumentTypeId)) {
            eksternReferanseId =
                journalpost.getEksternReferanseId() != null ? journalpost.getEksternReferanseId() : arkivTjeneste.hentEksternReferanseId(
                    journalpost.getOriginalJournalpost()).orElse(null);
        }

        var mottattTidspunkt = Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now);

        final var xml = hentDokumentSettMetadata(saksnummer, behandlingTema, aktørIdFagsak, journalpost);
        klargjører.klargjør(xml, saksnummer, journalpost.getJournalpostId(), brukDokumentTypeId, mottattTidspunkt, behandlingTema,
            forsendelseId.orElse(null), dokumentKategori, enhetId, eksternReferanseId);

        // For å unngå klonede journalposter fra GOSYS - de kan komme via Kafka.
        dokumentRepository.lagreJournalpostLokal(journalpost.getJournalpostId(), journalpost.getKanal(), ENDELIG,
            journalpost.getEksternReferanseId());

        opprettFerdigstillOppgaveTask(JournalpostId.fra(journalpost.getJournalpostId()));
    }

    public void oppdaterJournalpostOgFerdigstillGenerellSak(String enhetId,
                                                            ArkivJournalpost journalpost,
                                                            String aktørId,
                                                            String nyJournalpostTittel,
                                                            List<DokumenterMedNyTittel> dokumenterMedNyTittel,
                                                            DokumentTypeId nyDokumentTypeId) {

        validerJournalposttype(journalpost.getJournalposttype());

        LOG.info("FPFORDEL RESTJOURNALFØRING GENERELL: Ferdigstiller journalpostId: {}", journalpost.getJournalpostId());

        var dokumentTypeId = journalpost.getHovedtype();
        if (nyDokumentTypeId != null) {
            dokumentTypeId = nyDokumentTypeId;
        }

        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);
        final var behandlingTema = validerOgVelgBehandlingTema(BehandlingTema.UDEFINERT, behandlingTemaDok, dokumentTypeId);

        // For å unngå klonede journalposter fra GOSYS - de kan komme via Kafka - må skje før vi ferdigstiller.
        // Ellers kan kan kafka hendelsen komme tidligere en vi klarer å lagre og oppretter en ny oppgave.
        dokumentRepository.lagreJournalpostLokal(journalpost.getJournalpostId(), journalpost.getKanal(), ENDELIG,
            journalpost.getEksternReferanseId());

        if (Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            oppdaterJournalpostMedTittelOgMangler(journalpost, nyJournalpostTittel, dokumenterMedNyTittel, aktørId, behandlingTema);
            LOG.info("FPFORDEL RESTJOURNALFØRING GENERELL: Kaller til Journalføring"); // NOSONAR
            try {
                arkivTjeneste.oppdaterMedGenerellSak(journalpost.getJournalpostId(), aktørId);
                arkivTjeneste.ferdigstillJournalføring(journalpost.getJournalpostId(), enhetId);
            } catch (Exception e) {
                LOG.warn("FPFORDEL RESTJOURNALFØRING GENERELL: oppdaterJournalpostOgFerdigstill feiler for {}", journalpost.getJournalpostId(), e);
                throw new TekniskException("FP-15689", lagUgyldigInputMelding("Bruker", BRUKER_MANGLER));
            }
        }

        opprettFerdigstillOppgaveTask(JournalpostId.fra(journalpost.getJournalpostId()));
    }


    public void oppdaterJournalpostMedTittelOgMangler(ArkivJournalpost journalpost,
                                                      String nyJournalpostTittel,
                                                      List<DokumenterMedNyTittel> dokumenterMedNyTittel,
                                                      String aktørId,
                                                      BehandlingTema behandlingTema) {
        var journalpostId = journalpost.getJournalpostId();
        var kanal = journalpost.getKanal();

        if ((nyJournalpostTittel != null || !dokumenterMedNyTittel.isEmpty()) && List.of(MottakKanal.SELVBETJENING.getKode(),
            MottakKanal.ALTINN.getKode()).contains(kanal)) {
            // TFP-5791: Det er spesialhåndtering av tilfeller hvor klage/anke journalpost tittel ble endret i gosys uten at man har endret dokument tittel samtidig.
            if (nyJournalpostTittel == null && dokumenterMedNyTittel.size() == 1) {
                LOG.info(
                    "FPFORDEL RESTJOURNALFØRING: Det endres dokument tittel på et dokument som kommer fra {}. Gjelder journalpost: {}:{}. Ny dokument tittel: {}.",
                    kanal, journalpostId, journalpost.getTittel(), dokumenterMedNyTittel.getFirst().dokumentTittel());
            } else {
                throw new FunksjonellException("FP-963071",
                    String.format("Kan ikke endre tittel på journalpost med id %s som kommer fra Selvbetjening eller Altinn.", journalpostId),
                    "Tittel kan ikke endres når journalposten kommer fra selvbetjening eller altinn");
            }
        }

        LOG.info("FPFORDEL RESTJOURNALFØRING: Oppdaterer generelle mangler og titler for journalpostId: {}", journalpostId);

        //Fjernes når vi har fått informasjon om hvor ofte dette skjer
        if (nyJournalpostTittel != null) {
            var dokumenterFraArkiv = journalpost.getOriginalJournalpost().dokumenter();

            Set<DokumentTypeId> nyeDokumenttyper = utledDokumentTyper(dokumenterMedNyTittel, dokumenterFraArkiv);

            var utledetDokType = ArkivUtil.utledHovedDokumentType(nyeDokumenttyper);

            if (!utledetDokType.getTermNavn().equals(nyJournalpostTittel)) {
                LOG.info("FPFORDEL RESTJOURNALFØRING: Ny journalpost-tittel: {} avviker fra utledet journalpost-tittel: {} for journalpostId: {}",
                    nyJournalpostTittel, utledetDokType.getTermNavn(), journalpostId);
            }
        }
        List<OppdaterJournalpostRequest.DokumentInfoOppdater> dokumenterÅOppdatere = new ArrayList<>();

        if (!dokumenterMedNyTittel.isEmpty()) {
            dokumenterÅOppdatere = mapDokumenterTilOppdatering(dokumenterMedNyTittel);
        }
        arkivTjeneste.oppdaterJournalpostVedManuellJournalføring(journalpostId, nyJournalpostTittel, dokumenterÅOppdatere, journalpost, aktørId,
            behandlingTema);

    }

    public record DokumenterMedNyTittel(String dokumentId, String dokumentTittel) {
    }

    private List<no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest.DokumentInfoOppdater> mapDokumenterTilOppdatering(List<DokumenterMedNyTittel> dokumenter) {
        return dokumenter.stream()
            .filter(dt -> dt.dokumentId() != null && dt.dokumentTittel() != null)
            .map(dt -> new no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest.DokumentInfoOppdater(dt.dokumentId(),
                dt.dokumentTittel(), null))
            .toList();
    }

    private static Set<DokumentTypeId> utledDokumentTyper(List<DokumenterMedNyTittel> dokumenterMedNyTittel, List<DokumentInfo> dokumenterFraArkiv) {
        Set<DokumentTypeId> dokumentTyper = new HashSet<>();
        Set<String> oppdatereDokIder = dokumenterMedNyTittel.stream().map(DokumenterMedNyTittel::dokumentId).collect(Collectors.toSet());

        for (DokumentInfo dokumentFraArkiv : dokumenterFraArkiv) {
            if (!oppdatereDokIder.contains(dokumentFraArkiv.dokumentInfoId())) {
                dokumentTyper.add(DokumentTypeId.fraTermNavn(dokumentFraArkiv.tittel()));
            }
        }
        for (DokumenterMedNyTittel dokumentNyTittel : dokumenterMedNyTittel) {
            dokumentTyper.add(DokumentTypeId.fraTermNavn(dokumentNyTittel.dokumentTittel()));
        }
        return dokumentTyper;
    }

    public ArkivJournalpost hentJournalpost(String arkivId) {
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

    String opprettSak(ArkivJournalpost journalpost, FerdigstillJournalføringRestTjeneste.OpprettSak opprettSakInfo, DokumentTypeId nyDokumentTypeId) {
        new ManuellOpprettSakValidator(arkivTjeneste).validerKonsistensForOpprettSak(journalpost, opprettSakInfo.ytelseType(), opprettSakInfo.aktørId(),
            nyDokumentTypeId);

        return fagsak.opprettSak(opprettSakRequest(journalpost.getJournalpostId(), opprettSakInfo)).saksnummer();
    }

    // Validerer mot eksisterende men sikrer at det opprettes ny sak
    String opprettNySak(ArkivJournalpost journalpost,
                        FerdigstillJournalføringRestTjeneste.OpprettSak opprettSakInfo,
                        DokumentTypeId nyDokumentTypeId) {
        new ManuellOpprettSakValidator(arkivTjeneste).validerKonsistensForOpprettSak(journalpost, opprettSakInfo.ytelseType(), opprettSakInfo.aktørId(),
            nyDokumentTypeId);

        return fagsak.opprettSak(opprettSakRequest(null, opprettSakInfo)).saksnummer();
    }

    private static OpprettSakV2Dto opprettSakRequest(String journalpostId, FerdigstillJournalføringRestTjeneste.OpprettSak opprettSakInfo) {
        return new OpprettSakV2Dto(journalpostId, mapYtelseTypeV2TilDto(opprettSakInfo.ytelseType()), opprettSakInfo.aktørId().getId());
    }

    static YtelseTypeDto mapYtelseTypeV2TilDto(FagsakYtelseTypeDto ytelseType) {
        return switch (ytelseType) {
            case FORELDREPENGER -> YtelseTypeDto.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> YtelseTypeDto.SVANGERSKAPSPENGER;
            case ENGANGSTØNAD -> YtelseTypeDto.ENGANGSTØNAD;
            case null -> throw new IllegalStateException("YtelseType må være satt.");
        };
    }

    public JournalpostId knyttTilAnnenSak(ArkivJournalpost journalpost, String enhetId, String saksnummer) {
        var saksinfo = hentFagsakInfo(saksnummer).orElseThrow();
        final var behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(saksinfo.behandlingstemaOffisiellKode());
        final var aktørIdFagsak = saksinfo.aktørId();

        var dokumentTypeId = journalpost.getHovedtype();
        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);
        final var behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);
        final var dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;

        validerKanJournalføreKlageDokument(behandlingTemaFagsak, brukDokumentTypeId, dokumentKategori);

        new ManuellOpprettSakValidator(arkivTjeneste).validerKonsistensForKnyttTilAnnenSak(journalpost, behandlingTema.utledYtelseType(),
            new AktørId(aktørIdFagsak), journalpost.getHovedtype());

        // Do the business
        var nyJournalpostId = arkivTjeneste.knyttTilAnnenSak(journalpost, enhetId, saksnummer, aktørIdFagsak);

        final var forsendelseId = asUUID(journalpost.getEksternReferanseId());

        // Bruk fra opprinnelig
        final var xml = hentDokumentSettMetadata(saksnummer, behandlingTema, aktørIdFagsak, journalpost);
        var mottattTidspunkt = Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now);
        String eksternReferanseId = null;
        if (DokumentTypeId.INNTEKTSMELDING.equals(brukDokumentTypeId)) {
            eksternReferanseId =
                journalpost.getEksternReferanseId() != null ? journalpost.getEksternReferanseId() : arkivTjeneste.hentEksternReferanseId(
                    journalpost.getOriginalJournalpost()).orElse(null);
        }

        klargjører.klargjør(xml, saksnummer, nyJournalpostId, brukDokumentTypeId, mottattTidspunkt, behandlingTema, forsendelseId.orElse(null),
            dokumentKategori, enhetId, eksternReferanseId);

        // For å unngå klonede journalposter fra GOSYS - de kan komme via Kafka.
        dokumentRepository.lagreJournalpostLokal(nyJournalpostId, journalpost.getKanal(), ENDELIG, journalpost.getEksternReferanseId());

        return Optional.ofNullable(nyJournalpostId).map(JournalpostId::fra).orElse(null);
    }

    // Forvaltning only
    public void sendInnPåSak(ArkivJournalpost journalpost, String saksnummer) {
        var saksinfo = hentFagsakInfo(saksnummer).orElseThrow();
        final var behandlingTemaFagsak = BehandlingTema.fraOffisiellKode(saksinfo.behandlingstemaOffisiellKode());
        final var aktørIdFagsak = saksinfo.aktørId();

        var dokumentTypeId = journalpost.getHovedtype();
        final var behandlingTemaDok = ArkivUtil.behandlingTemaFraDokumentType(BehandlingTema.UDEFINERT, dokumentTypeId);
        final var behandlingTema = validerOgVelgBehandlingTema(behandlingTemaFagsak, behandlingTemaDok, dokumentTypeId);
        final var dokumentKategori = ArkivUtil.utledKategoriFraDokumentType(dokumentTypeId);
        var brukDokumentTypeId = DokumentTypeId.UDEFINERT.equals(dokumentTypeId) ? DokumentTypeId.ANNET : dokumentTypeId;

        final var forsendelseId = asUUID(journalpost.getEksternReferanseId());

        // Bruk fra opprinnelig
        final var xml = hentDokumentSettMetadata(saksnummer, behandlingTema, aktørIdFagsak, journalpost);
        var mottattTidspunkt = Optional.ofNullable(journalpost.getDatoOpprettet()).orElseGet(LocalDateTime::now);
        String eksternReferanseId = null;
        if (DokumentTypeId.INNTEKTSMELDING.equals(brukDokumentTypeId)) {
            eksternReferanseId =
                journalpost.getEksternReferanseId() != null ? journalpost.getEksternReferanseId() : arkivTjeneste.hentEksternReferanseId(
                    journalpost.getOriginalJournalpost()).orElse(null);
        }

        klargjører.klargjør(xml, saksnummer, journalpost.getJournalpostId(), brukDokumentTypeId, mottattTidspunkt, behandlingTema,
            forsendelseId.orElse(null), dokumentKategori, null, eksternReferanseId);
    }


    private static BehandlingTema validerOgVelgBehandlingTema(BehandlingTema behandlingTemaFagsak,
                                                              BehandlingTema behandlingTemaDok,
                                                              DokumentTypeId dokumentTypeId) {
        if (BehandlingTema.UDEFINERT.equals(behandlingTemaDok) && !BehandlingTema.UDEFINERT.equals(behandlingTemaFagsak)) {
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
        if (!ikkeSpesifikkHendelse(behandlingTemaDok) && !ikkeSpesifikkHendelse(behandlingTemaFagsak)) {
            if ((gjelderFødsel(behandlingTemaDok) && !gjelderFødsel(behandlingTemaFagsak)) || (gjelderAdopsjon(behandlingTemaDok) && !gjelderAdopsjon(
                behandlingTemaFagsak))) {
                throw new FunksjonellException("FP-963080", "Dokumentet samsvarer ikke med sakens hendelse type (fødsel/adopsjon) - kan ikke journalføre",
                    "Journalfør på annen sak eller opprett ny sak");
            }
        }
        return ikkeSpesifikkHendelse(behandlingTemaDok) ? behandlingTemaFagsak : behandlingTemaDok;
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
            f -> brukerAktørId.isEmpty() || Objects.equals(f.aktørId(), brukerAktørId.get()));

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
        if (DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD.equals(dokumentTypeId) && !ikkeSpesifikkHendelse(behandlingTema)) {
            dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        }
        try {
            mottattDokument.kopierTilMottakWrapper(dataWrapper, pdl::hentAktørIdForPersonIdent);
        } catch (FunksjonellException e) {
            // Her er det "greit" - da har man bestemt seg, men kan lage rot i saken.
            if ("FP-401245".equals(e.getKode()) || "FP-401246".equals(e.getKode())) {
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

    private static void validerJournalposttype(Journalposttype type) {
        if (!Journalposttype.INNGÅENDE.equals(type)) {
            throw new TekniskException("FP-15680", lagUgyldigInputMelding("JournalpostType", JOURNALPOST_IKKE_INNGÅENDE));
        }
    }

    void opprettFerdigstillOppgaveTask(JournalpostId journalpostId) {
        if (journalpostId != null) {
            try {
                oppgaver.ferdigstillAlleÅpneJournalføringsoppgaverFor(journalpostId);
            } catch (Exception e) {
                LOG.warn("FPFORDEL RESTJOURNALFØRING: Ferdigstilt oppgave med dokumentId {} feiler ", journalpostId, e);
                var ferdigstillOppgaveTask = ProsessTaskData.forProsessTask(FerdigstillOppgaveTask.class);
                ferdigstillOppgaveTask.setProperty(FerdigstillOppgaveTask.JOURNALPOSTID_KEY, journalpostId.getVerdi());
                taskTjeneste.lagre(ferdigstillOppgaveTask);
            }
        }
    }
}
