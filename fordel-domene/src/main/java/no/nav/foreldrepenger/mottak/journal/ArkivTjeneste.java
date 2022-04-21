package no.nav.foreldrepenger.mottak.journal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.MapNAVSkjemaDokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.DokArkiv;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.AvsenderMottaker;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.AvsenderMottakerIdType;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Bruker;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.DokumentInfoOppdater;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.DokumentInfoOpprett;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Dokumentvariant;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OppdaterJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Sak;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Tilleggsopplysning;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Variantformat;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;
import no.nav.foreldrepenger.mottak.journal.saf.model.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;

@Dependent
public class ArkivTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ArkivTjeneste.class);

    private static final String FP_DOK_TYPE = "fp_innholdtype";

    // Fyll på med gjengangertitler som ikke omfattes av kodeverk DokumentTypeId
    private static final Map<String, DokumentTypeId> TITTEL_MAP = Map.of(
            "Klage", DokumentTypeId.KLAGE_DOKUMENT,
            "Anke", DokumentTypeId.KLAGE_DOKUMENT);

    private final SafTjeneste saf;
    private final DokArkiv dokArkivTjeneste;
    private final DokumentRepository dokumentRepository;
    private final PersonInformasjon personTjeneste;

    @Inject
    public ArkivTjeneste(SafTjeneste saf,
            /* @Jersey */ DokArkiv dokArkivTjeneste,
            DokumentRepository dokumentRepository,
            PersonInformasjon personTjeneste) {
        this.saf = saf;
        this.dokArkivTjeneste = dokArkivTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.personTjeneste = personTjeneste;
    }

    public ArkivJournalpost hentArkivJournalpost(String journalpostId) {
        var journalpost = saf.hentJournalpostInfo(journalpostId);

        var builder = ArkivJournalpost.getBuilder().medJournalpost(journalpost).medJournalpostId(journalpostId);

        var infoList = journalpost.dokumenter().stream()
                .filter(it -> it.dokumentvarianter().stream().anyMatch(at -> VariantFormat.ORIGINAL.equals(at.variantFormat())))
                .map(DokumentInfo::dokumentInfoId).toList();

        if (infoList.size() > 1) {
            throw new IllegalStateException("Journalposten har flere dokumenter med VariantFormat = ORIGINAL");
        } else if (!infoList.isEmpty()) {
            var dokumentInfo = infoList.get(0);
            var payload = saf.hentDokument(journalpostId, dokumentInfo, VariantFormat.ORIGINAL);
            builder.medStrukturertPayload(payload).medDokumentInfoId(dokumentInfo);
        }

        var alleTyper = utledDokumentTyper(journalpost);
        var behandlingTema = utledBehandlingTema(journalpost.behandlingstema(), alleTyper);
        mapIdent(journalpost).ifPresent(builder::medBrukerAktørId);
        if ((journalpost.avsenderMottaker() != null) && (journalpost.avsenderMottaker().type() != null)) {
            builder.medAvsender(journalpost.avsenderMottaker().id(), journalpost.avsenderMottaker().navn());
        }

        return builder.medKanal(journalpost.kanal())
                .medJournalposttype(Journalposttype.fraKodeDefaultUdefinert(journalpost.journalposttype()))
                .medTilstand(Journalstatus.fraKodeDefaultUdefinert(journalpost.journalstatus()))
                .medAlleTyper(alleTyper)
                .medHovedtype(ArkivUtil.utledHovedDokumentType(alleTyper))
                .medTema(Tema.fraOffisiellKode(journalpost.tema()))
                .medBehandlingstema(BehandlingTema.fraOffisiellKode(journalpost.behandlingstema()))
                .medUtledetBehandlingstema(behandlingTema)
                .medJournalfoerendeEnhet(journalpost.journalfoerendeEnhet())
                .medDatoOpprettet(journalpost.datoOpprettet())
                .medEksternReferanseId(journalpost.eksternReferanseId())
                .medTilleggsopplysninger(journalpost.tilleggsopplysninger())
                .medSaksnummer(Optional.ofNullable(journalpost.sak()).map(no.nav.foreldrepenger.mottak.journal.saf.model.Sak::fagsakId).orElse(null))
                .build();
    }

    public Optional<String> hentEksternReferanseId(Journalpost journalpost) {
        var dokumentInfoId = journalpost.dokumenter().get(0).dokumentInfoId();
        var referanse = saf.hentEksternReferanseId(dokumentInfoId).stream()
                .map(Journalpost::eksternReferanseId)
                .filter(Objects::nonNull)
                .findFirst();
        var loggtekst = referanse.orElse("ingen");
        LOG.info("FPFORDEL hentEksternReferanseId fant referanse {} for journalpost {}", loggtekst, journalpost.journalpostId());
        return referanse;
    }

    public OpprettetJournalpost opprettJournalpost(UUID forsendelse, UUID eksternReferanse, String avsenderAktørId) {
        var request = lagOpprettRequest(forsendelse, eksternReferanse, avsenderAktørId);
        var response = dokArkivTjeneste.opprettJournalpost(request, false);
        return new OpprettetJournalpost(response.journalpostId(), response.journalpostferdigstilt());
    }

    public OpprettetJournalpost opprettJournalpost(UUID forsendelse, String avsenderAktørId, String saksnummer) {
        var request = lagOpprettRequest(forsendelse, forsendelse, avsenderAktørId);
        request.setSak(lagSakForSaksnummer(saksnummer));
        request.setJournalfoerendeEnhet("9999");
        var response = dokArkivTjeneste.opprettJournalpost(request, true);
        return new OpprettetJournalpost(response.journalpostId(), response.journalpostferdigstilt());
    }

    public void oppdaterBehandlingstemaBruker(String journalpostId, DokumentTypeId dokumentTypeId, String behandlingstema, String aktørId) {
        var builder = OppdaterJournalpostRequest.ny()
                .medBehandlingstema(behandlingstema)
                .leggTilTilleggsopplysning(new Tilleggsopplysning(FP_DOK_TYPE, dokumentTypeId.getOffisiellKode()))
                .medBruker(aktørId);
        if (!dokArkivTjeneste.oppdaterJournalpost(journalpostId, builder.build())) {
            throw new IllegalStateException("FPFORDEL Kunne ikke oppdatere " + journalpostId);
        }
    }

    public void settTilleggsOpplysninger(ArkivJournalpost arkivJournalpost, DokumentTypeId defaultDokumentTypeId) {
        var journalpost = arkivJournalpost.getOriginalJournalpost();
        var hovedtype = DokumentTypeId.UDEFINERT.equals(arkivJournalpost.getHovedtype()) ?
                defaultDokumentTypeId : arkivJournalpost.getHovedtype();
        var tilleggDokumentType = arkivJournalpost.getTilleggsopplysninger()
                .stream().filter(to -> FP_DOK_TYPE.equals(to.nokkel())).findFirst();
        if (tilleggDokumentType.isEmpty()) {
            var builder = OppdaterJournalpostRequest.ny();
            LOG.info("FPFORDEL oppdaterer tilleggsopplysninger for {} med {}", journalpost.journalpostId(), hovedtype.getOffisiellKode());
            if (!arkivJournalpost.getTilleggsopplysninger().isEmpty()) {
                builder.medTilleggsopplysninger(arkivJournalpost.getTilleggsopplysninger());
            }
            builder.leggTilTilleggsopplysning(new Tilleggsopplysning(FP_DOK_TYPE, hovedtype.getOffisiellKode()));
            if (!dokArkivTjeneste.oppdaterJournalpost(journalpost.journalpostId(), builder.build())) {
                throw new IllegalStateException("FPFORDEL Kunne ikke oppdatere " + journalpost.journalpostId());
            }
        } else {
            LOG.info("FPFORDEL tilleggsopplysninger allerede satt for {} med {}", journalpost.journalpostId(), tilleggDokumentType);
        }
    }

    public boolean oppdaterRettMangler(ArkivJournalpost arkivJournalpost, String aktørId, BehandlingTema behandlingTema,
            DokumentTypeId defaultDokumentTypeId) {
        var journalpost = arkivJournalpost.getOriginalJournalpost();
        var alleTyper = arkivJournalpost.getAlleTyper();
        var hovedtype = DokumentTypeId.UDEFINERT.equals(arkivJournalpost.getHovedtype()) ? defaultDokumentTypeId : arkivJournalpost.getHovedtype();
        var utledetBehandlingTema = utledBehandlingTema(
                BehandlingTema.UDEFINERT.equals(behandlingTema) ? journalpost.behandlingstema() : behandlingTema.getOffisiellKode(), alleTyper);
        var tittelMangler = false;
        var builder = OppdaterJournalpostRequest.ny();
        if ((journalpost.avsenderMottaker() == null) || (journalpost.avsenderMottaker().id() == null)
                || (journalpost.avsenderMottaker().navn() == null)) {
            var fnr = personTjeneste.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
            var navn = personTjeneste.hentNavn(aktørId);
            LOG.info("FPFORDEL oppdaterer manglende avsender for {}", journalpost.journalpostId());
            builder.medAvsender(fnr, navn);
        }
        if (journalpost.tittel() == null) {
            if (DokumentTypeId.UDEFINERT.equals(hovedtype)) {
                tittelMangler = true;
            } else {
                LOG.info("FPFORDEL oppdaterer manglende tittel for {}", journalpost.journalpostId());
                builder.medTittel(hovedtype.getTermNavn());
            }
        }
        if (journalpost.tema() == null) {
            LOG.info("FPFORDEL oppdaterer manglende tema for {}", journalpost.journalpostId());
            builder.medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode());
        }
        if ((journalpost.behandlingstema() == null) && !BehandlingTema.UDEFINERT.equals(utledetBehandlingTema)) {
            // Logges ikke da den nesten alltid oppdateres
            builder.medBehandlingstema(utledetBehandlingTema.getOffisiellKode());
        }
        if ((journalpost.bruker() == null) || (journalpost.bruker().id() == null)) {
            LOG.info("FPFORDEL oppdaterer manglende bruker for {}", journalpost.journalpostId());
            builder.medBruker(aktørId);
        }
        var oppdaterDok = journalpost.dokumenter().stream()
                .filter(d -> (d.tittel() == null) || d.tittel().isEmpty())
                .filter(d -> d.brevkode() != null)
                .map(d -> new DokumentInfoOppdater(d.dokumentInfoId(), NAVSkjema.fraOffisiellKode(d.brevkode()).getTermNavn(), d.brevkode())).toList();
        if (!oppdaterDok.isEmpty()) {
            LOG.info("FPFORDEL oppdaterer manglende dokumenttitler for {}", journalpost.journalpostId());
        }
        oppdaterDok.forEach(builder::leggTilDokument);
        if (!dokArkivTjeneste.oppdaterJournalpost(journalpost.journalpostId(), builder.build())) {
            throw new IllegalStateException("FPFORDEL Kunne ikke oppdatere " + journalpost.journalpostId());
        }
        var resultat = !tittelMangler
                && (journalpost.dokumenter().stream().filter(d -> (d.tittel() == null) || d.tittel().isEmpty()).count() == oppdaterDok
                        .size());
        if (!resultat) {
            LOG.info("FPFORDEL oppdaterer gjenstår tittel eller dokumenttittel for {}", journalpost.journalpostId());
        }
        return resultat;
    }

    public void oppdaterMedSak(String journalpostId, String sakId, String aktørId) {
        if (sakId == null) {
            throw new IllegalArgumentException("FPFORDEL oppdaterMedSak mangler saksnummer " + journalpostId);
        }
        // Egne regler for FAGSAK
        // https://confluence.adeo.no/display/BOA/oppdaterJournalpost - dette blir
        // standard i contract-phase
        var builder = OppdaterJournalpostRequest.ny()
                .medSak(lagSakForSaksnummer(sakId))
                .medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode())
                .medBruker(aktørId);

        if (dokArkivTjeneste.oppdaterJournalpost(journalpostId, builder.build())) {
            LOG.info("FPFORDEL SAKSOPPDATERING oppdaterte {} med sak {}", journalpostId, sakId);
        } else {
            throw new IllegalStateException("FPFORDEL Kunne ikke knytte journalpost " + journalpostId + " til sak " + sakId);
        }
    }

    public void ferdigstillJournalføring(String journalpostId, String enhet) {
        if (dokArkivTjeneste.ferdigstillJournalpost(journalpostId, enhet)) {
            LOG.info("FPFORDEL FERDIGSTILLING ferdigstilte journalpost {} enhet {}", journalpostId, enhet);
        } else {
            throw new IllegalStateException("FPFORDEL Kunne ikke ferdigstille journalpost " + journalpostId);
        }
    }

    private static BehandlingTema utledBehandlingTema(String btJournalpost, Set<DokumentTypeId> dokumenttyper) {
        var bt = BehandlingTema.fraOffisiellKode(btJournalpost);
        return ArkivUtil.behandlingTemaFraDokumentTypeSet(bt, dokumenttyper);
    }

    private static Optional<DokumentTypeId> dokumentTypeFraKjenteTitler(String tittel) {
        return Optional.ofNullable(tittel)
                .map(TITTEL_MAP::get);
    }

    private Sak lagSakForSaksnummer(String saksnummer) {
        return new Sak(saksnummer, "FS36", "FAGSAK");
    }

    private static Set<DokumentTypeId> utledDokumentTyper(Journalpost journalpost) {
        Set<DokumentTypeId> alletyper = new HashSet<>();
        Set<NAVSkjema> allebrevkoder = new HashSet<>();
        alletyper.add(DokumentTypeId.fraTermNavn(journalpost.tittel()));
        allebrevkoder.add(NAVSkjema.fraTermNavn(journalpost.tittel()));
        dokumentTypeFraKjenteTitler(journalpost.tittel()).ifPresent(alletyper::add);
        journalpost.dokumenter().forEach(d -> {
            alletyper.add(DokumentTypeId.fraTermNavn(d.tittel()));
            d.logiskeVedlegg().forEach(v -> alletyper.add(DokumentTypeId.fraTermNavn(v.tittel())));
            dokumentTypeFraKjenteTitler(d.tittel()).ifPresent(alletyper::add);
            d.logiskeVedlegg().forEach(v -> dokumentTypeFraKjenteTitler(v.tittel()).ifPresent(alletyper::add));
            allebrevkoder.add(NAVSkjema.fraOffisiellKode(d.brevkode()));
            allebrevkoder.add(NAVSkjema.fraTermNavn(d.tittel()));
            d.logiskeVedlegg().forEach(v -> allebrevkoder.add(NAVSkjema.fraTermNavn(v.tittel())));
        });
        allebrevkoder.forEach(b -> alletyper.add(MapNAVSkjemaDokumentTypeId.mapBrevkode(b)));
        Optional.ofNullable(journalpost.tilleggsopplysninger()).orElse(List.of()).stream()
                .filter(to -> FP_DOK_TYPE.equals(to.nokkel()))
                .map(to -> DokumentTypeId.fraOffisiellKode(to.verdi()))
                .filter(dt -> !DokumentTypeId.UDEFINERT.equals(dt))
                .forEach(alletyper::add);
        return alletyper;
    }

    public static boolean harBrevKode(Journalpost journalpost, NAVSkjema brevkode) {
        if (journalpost == null || brevkode == null) return false;
        Set<NAVSkjema> allebrevkoder = new HashSet<>();
        allebrevkoder.add(NAVSkjema.fraTermNavn(journalpost.tittel()));
        journalpost.dokumenter().forEach(d -> {
                allebrevkoder.add(NAVSkjema.fraOffisiellKode(d.brevkode()));
                allebrevkoder.add(NAVSkjema.fraTermNavn(d.tittel()));
                d.logiskeVedlegg().forEach(v -> allebrevkoder.add(NAVSkjema.fraTermNavn(v.tittel())));
        });
        return allebrevkoder.contains(brevkode);
    }

    private Optional<String> mapIdent(Journalpost journalpost) {
        var bruker = journalpost.bruker();
        if (bruker == null) {
            return Optional.empty();
        }
        if (bruker.erAktoerId()) {
            return Optional.of(bruker.id());
        } else if (BrukerIdType.FNR.equals(bruker.type())) {
            return personTjeneste.hentAktørIdForPersonIdent(bruker.id());
        } else if (BrukerIdType.ORGNR.equals(bruker.type())) {
            return Optional.empty();
        }
        throw new IllegalArgumentException("Ukjent brukerType=" + bruker.type());
    }

    private OpprettJournalpostRequest lagOpprettRequest(UUID forsendelseId, UUID eksternReferanse, String avsenderAktørId) {
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        var dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        var opprettDokumenter = lagAlleDokumentForOpprett(dokumenter);
        var dokumenttyper = dokumenter.stream().map(Dokument::getDokumentTypeId).collect(Collectors.toSet());
        var hovedtype = ArkivUtil.utledHovedDokumentType(dokumenttyper);
        var behandlingstema = utledBehandlingTema(null, dokumenttyper);
        var tittel = DokumentTypeId.UDEFINERT.equals(hovedtype) ? DokumentTypeId.ANNET.getTermNavn() : hovedtype.getTermNavn();
        var bruker = new Bruker(metadata.getBrukerId(), BrukerIdType.AKTOERID);
        var ident = personTjeneste.hentPersonIdentForAktørId(avsenderAktørId).orElseThrow(() -> new IllegalStateException("Aktør uten personident"));
        var avsender = new AvsenderMottaker(ident, AvsenderMottakerIdType.FNR, personTjeneste.hentNavn(avsenderAktørId));

        var request = OpprettJournalpostRequest.nyInngående();
        request.setTittel(tittel);
        request.setKanal(MottakKanal.SELVBETJENING.getKode());
        request.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode());
        request.setBehandlingstema(behandlingstema.getOffisiellKode());
        request.setDatoMottatt(metadata.getForsendelseMottatt().toLocalDate());
        request.setEksternReferanseId(eksternReferanse.toString());
        request.setBruker(bruker);
        request.setAvsenderMottaker(avsender);
        request.setDokumenter(opprettDokumenter);
        request.setTilleggsopplysninger(
                DokumentTypeId.UDEFINERT.equals(hovedtype) ? List.of() : List.of(new Tilleggsopplysning(FP_DOK_TYPE, hovedtype.getOffisiellKode())));

        return request;
    }

    private static List<DokumentInfoOpprett> lagAlleDokumentForOpprett(List<Dokument> dokumenter) {
        List<DokumentInfoOpprett> dokumenterRequest = new ArrayList<>();
        var hoveddokument = dokumenter.stream().filter(Dokument::erHovedDokument).toList();
        if (!hoveddokument.isEmpty()) {
            var strukturert = hoveddokument.stream()
                    .filter(dok -> ArkivFilType.XML.equals(dok.getArkivFilType()))
                    .findFirst()
                    .map(dok -> new Dokumentvariant(Variantformat.ORIGINAL, dok.getArkivFilType().name(), dok.getBase64EncodetDokument()))
                    .orElse(null);
            var arkivvariant = hoveddokument.stream()
                    .filter(dok -> !ArkivFilType.XML.equals(dok.getArkivFilType()))
                    .findFirst().orElseThrow(() -> new IllegalStateException("Utviklerfeil mangler arkivversjon"));
            dokumenterRequest.add(lagDokumentForOpprett(arkivvariant, strukturert));
        }
        dokumenter.stream()
                .filter(d -> !d.erHovedDokument())
                .map(d -> lagDokumentForOpprett(d, null))
                .forEach(dokumenterRequest::add);
        return dokumenterRequest;
    }

    private static DokumentInfoOpprett lagDokumentForOpprett(Dokument dokument, Dokumentvariant struktuert) {
        List<Dokumentvariant> varianter = new ArrayList<>();
        if (struktuert != null) {
            varianter.add(struktuert);
        }
        varianter.add(new Dokumentvariant(Variantformat.ARKIV, dokument.getArkivFilType().name(), dokument.getBase64EncodetDokument()));
        var type = DokumentTypeId.UDEFINERT.equals(dokument.getDokumentTypeId()) ? DokumentTypeId.ANNET : dokument.getDokumentTypeId();
        var tittel = DokumentTypeId.ANNET.equals(type) && (dokument.getBeskrivelse() != null) ? dokument.getBeskrivelse() : type.getTermNavn();
        var brevkode = MapNAVSkjemaDokumentTypeId.mapDokumentTypeId(type);
        final DokumentKategori kategori;
        if (DokumentTypeId.erSøknadType(type)) {
            kategori = DokumentKategori.SØKNAD;
        } else {
            kategori = DokumentTypeId.erKlageType(type) ? DokumentKategori.KLAGE_ELLER_ANKE : DokumentKategori.IKKE_TOLKBART_SKJEMA;
        }
        return new DokumentInfoOpprett(tittel, brevkode.getOffisiellKode(), kategori.getOffisiellKode(), varianter);
    }

}
