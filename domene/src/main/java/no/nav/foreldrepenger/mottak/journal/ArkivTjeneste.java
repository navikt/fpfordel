package no.nav.foreldrepenger.mottak.journal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
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
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Variantformat;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;
import no.nav.foreldrepenger.mottak.journal.saf.model.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;
import no.nav.foreldrepenger.mottak.person.PersonTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;

@ApplicationScoped
public class ArkivTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ArkivTjeneste.class);

    // Fyll på med gjengangertitler som ikke omfattes av kodeverk DokumentTypeId
    private static final Map<String, DokumentTypeId> TITTEL_MAP = Map.of(
            "Klage", DokumentTypeId.KLAGE_DOKUMENT,
            "Anke", DokumentTypeId.KLAGE_DOKUMENT);

    private SafTjeneste safTjeneste;
    private DokArkiv dokArkivTjeneste;
    private DokumentRepository dokumentRepository;
    private PersonTjeneste personTjeneste;

    ArkivTjeneste() {
        // CDI
    }

    @Inject
    public ArkivTjeneste(SafTjeneste safTjeneste,
            DokArkiv dokArkivTjeneste,
            DokumentRepository dokumentRepository,
            PersonTjeneste personTjeneste) {
        this.safTjeneste = safTjeneste;
        this.dokArkivTjeneste = dokArkivTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.personTjeneste = personTjeneste;
    }

    public ArkivJournalpost hentArkivJournalpost(String journalpostId) {
        var journalpost = safTjeneste.hentJournalpostInfo(journalpostId);

        var builder = ArkivJournalpost.getBuilder().medJournalpost(journalpost).medJournalpostId(journalpostId);

        var infoList = journalpost.getDokumenter().stream()
                .filter(it -> it.getDokumentvarianter().stream().anyMatch(at -> VariantFormat.ORIGINAL.equals(at.getVariantFormat())))
                .map(DokumentInfo::getDokumentInfoId)
                .collect(Collectors.toList());

        if (infoList.size() > 1) {
            throw new IllegalStateException("Journalposten har flere dokumenter med VariantFormat = ORIGINAL");
        } else if (!infoList.isEmpty()) {
            var dokumentInfo = infoList.get(0);
            var payload = safTjeneste.hentDokument(journalpostId, dokumentInfo, VariantFormat.ORIGINAL);
            builder.medStrukturertPayload(payload).medDokumentInfoId(dokumentInfo);
        }

        Set<DokumentTypeId> alleTyper = utledDokumentTyper(journalpost);
        BehandlingTema behandlingTema = utledBehandlingTema(journalpost.getBehandlingstema(), alleTyper);
        mapIdent(journalpost).ifPresent(builder::medBrukerAktørId);
        if ((journalpost.getAvsenderMottaker() != null) && (journalpost.getAvsenderMottaker().getType() != null)) {
            builder.medAvsender(journalpost.getAvsenderMottaker().getId(), journalpost.getAvsenderMottaker().getNavn());
        }

        return builder.medKanal(journalpost.getKanal())
                .medJournalposttype(Journalposttype.fraKodeDefaultUdefinert(journalpost.getJournalposttype()))
                .medTilstand(Journalstatus.fraKodeDefaultUdefinert(journalpost.getJournalstatus()))
                .medAlleTyper(alleTyper)
                .medHovedtype(utledHovedDokumentType(alleTyper))
                .medTema(Tema.fraOffisiellKode(journalpost.getTema()))
                .medBehandlingstema(BehandlingTema.fraOffisiellKode(journalpost.getBehandlingstema()))
                .medUtledetBehandlingstema(behandlingTema)
                .medJournalfoerendeEnhet(journalpost.getJournalfoerendeEnhet())
                .medDatoOpprettet(journalpost.getDatoOpprettet())
                .medEksternReferanseId(journalpost.getEksternReferanseId())
                .build();
    }

    public Optional<String> hentEksternReferanseId(Journalpost journalpost) {
        var dokumentInfoId = journalpost.getDokumenter().get(0).getDokumentInfoId();
        var referanse = safTjeneste.hentEksternReferanseId(dokumentInfoId).stream()
                .map(Journalpost::getEksternReferanseId)
                .filter(Objects::nonNull)
                .findFirst();
        var loggtekst = referanse.orElse("ingen");
        LOG.info("FPFORDEL hentEksternReferanseId fant referanse {} for journalpost {}", loggtekst, journalpost.getJournalpostId());
        return referanse;
    }

    public OpprettetJournalpost opprettJournalpost(UUID forsendelse, String avsenderAktørId) {
        var request = lagOpprettRequest(forsendelse, avsenderAktørId);
        var response = dokArkivTjeneste.opprettJournalpost(request, false);
        return new OpprettetJournalpost(response.getJournalpostId(), response.getJournalpostferdigstilt());
    }

    public OpprettetJournalpost opprettJournalpost(UUID forsendelse, String avsenderAktørId, String saksnummer) {
        var request = lagOpprettRequest(forsendelse, avsenderAktørId);
        request.setSak(new Sak(null, null, "ARKIVSAK", saksnummer, "GSAK"));
        request.setJournalfoerendeEnhet("9999");
        var response = dokArkivTjeneste.opprettJournalpost(request, true);
        return new OpprettetJournalpost(response.getJournalpostId(), response.getJournalpostferdigstilt());
    }

    public void oppdaterBehandlingstemaBruker(String journalpostId, String behandlingstema, String aktørId) {
        var builder = OppdaterJournalpostRequest.ny()
                .medBehandlingstema(behandlingstema)
                .medBruker(aktørId);
        if (!dokArkivTjeneste.oppdaterJournalpost(journalpostId, builder.build())) {
            throw new IllegalStateException("FPFORDEL Kunne ikke oppdatere " + journalpostId);
        }
    }

    public boolean oppdaterRettMangler(ArkivJournalpost arkivJournalpost, String aktørId, BehandlingTema behandlingTema,
            DokumentTypeId defaultDokumentTypeId) {
        var journalpost = arkivJournalpost.getOriginalJournalpost();
        Set<DokumentTypeId> alleTyper = arkivJournalpost.getAlleTyper();
        var hovedtype = DokumentTypeId.UDEFINERT.equals(arkivJournalpost.getHovedtype()) ? defaultDokumentTypeId : arkivJournalpost.getHovedtype();
        var utledetBehandlingTema = utledBehandlingTema(
                BehandlingTema.UDEFINERT.equals(behandlingTema) ? journalpost.getBehandlingstema() : behandlingTema.getOffisiellKode(), alleTyper);
        var tittelMangler = false;
        var builder = OppdaterJournalpostRequest.ny();
        if ((journalpost.getAvsenderMottaker() == null) || (journalpost.getAvsenderMottaker().getId() == null)
                || (journalpost.getAvsenderMottaker().getNavn() == null)) {
            var fnr = personTjeneste.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
            var navn = personTjeneste.hentNavn(aktørId);
            LOG.info("FPFORDEL oppdaterer manglende avsender for {}", journalpost.getJournalpostId());
            builder.medAvsender(fnr, navn);
        }
        if (journalpost.getTittel() == null) {
            if (DokumentTypeId.UDEFINERT.equals(hovedtype)) {
                tittelMangler = true;
            } else {
                LOG.info("FPFORDEL oppdaterer manglende tittel for {}", journalpost.getJournalpostId());
                builder.medTittel(hovedtype.getTermNavn());
            }
        }
        if (journalpost.getTema() == null) {
            LOG.info("FPFORDEL oppdaterer manglende tema for {}", journalpost.getJournalpostId());
            builder.medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode());
        }
        if ((journalpost.getBehandlingstema() == null) && !BehandlingTema.UDEFINERT.equals(utledetBehandlingTema)) {
            // Logges ikke da den nesten alltid oppdateres
            builder.medBehandlingstema(utledetBehandlingTema.getOffisiellKode());
        }
        if ((journalpost.getBruker() == null) || (journalpost.getBruker().getId() == null)) {
            LOG.info("FPFORDEL oppdaterer manglende bruker for {}", journalpost.getJournalpostId());
            builder.medBruker(aktørId);
        }
        var oppdaterDok = journalpost.getDokumenter().stream()
                .filter(d -> (d.getTittel() == null) || d.getTittel().isEmpty())
                .filter(d -> d.getBrevkode() != null)
                .map(d -> new DokumentInfoOppdater(d.getDokumentInfoId(), NAVSkjema.fraOffisiellKode(d.getBrevkode()).getTermNavn(), d.getBrevkode()))
                .collect(Collectors.toList());
        if (!oppdaterDok.isEmpty()) {
            LOG.info("FPFORDEL oppdaterer manglende dokumenttitler for {}", journalpost.getJournalpostId());
        }
        oppdaterDok.forEach(builder::leggTilDokument);
        if (builder.harVerdier() && !dokArkivTjeneste.oppdaterJournalpost(journalpost.getJournalpostId(), builder.build())) {
            throw new IllegalStateException("FPFORDEL Kunne ikke oppdatere " + journalpost.getJournalpostId());
        }
        var resultat = !tittelMangler
                && (journalpost.getDokumenter().stream().filter(d -> (d.getTittel() == null) || d.getTittel().isEmpty()).count() == oppdaterDok
                        .size());
        if (!resultat) {
            LOG.info("FPFORDEL oppdaterer gjenstår tittel eller dokumenttittel for {}", journalpost.getJournalpostId());
        }
        return resultat;
    }

    public void oppdaterMedSak(String journalpostId, String arkivSakId) {
        if (arkivSakId == null) {
            throw new IllegalArgumentException("FPFORDEL oppdaterMedSak mangler saksnummer " + journalpostId);
        }
        var builder = OppdaterJournalpostRequest.ny().medArkivSak(arkivSakId);
        if (dokArkivTjeneste.oppdaterJournalpost(journalpostId, builder.build())) {
            LOG.info("FPFORDEL SAKSOPPDATERING oppdaterte {} med sak {}", journalpostId, arkivSakId);
        } else {
            throw new IllegalStateException("FPFORDEL Kunne ikke knytte journalpost " + journalpostId + " til sak " + arkivSakId);
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
        BehandlingTema bt = BehandlingTema.fraOffisiellKode(btJournalpost);
        return ArkivUtil.behandlingTemaFraDokumentTypeSet(bt, dokumenttyper);
    }

    private static Optional<DokumentTypeId> dokumentTypeFraKjenteTitler(String tittel) {
        if (tittel == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(TITTEL_MAP.get(tittel));
    }

    private static Set<DokumentTypeId> utledDokumentTyper(Journalpost journalpost) {
        Set<DokumentTypeId> alletyper = new HashSet<>();
        Set<NAVSkjema> allebrevkoder = new HashSet<>();
        alletyper.add(DokumentTypeId.fraTermNavn(journalpost.getTittel()));
        allebrevkoder.add(NAVSkjema.fraTermNavn(journalpost.getTittel()));
        dokumentTypeFraKjenteTitler(journalpost.getTittel()).ifPresent(alletyper::add);
        journalpost.getDokumenter().forEach(d -> {
            alletyper.add(DokumentTypeId.fraTermNavn(d.getTittel()));
            d.getLogiskeVedlegg().forEach(v -> alletyper.add(DokumentTypeId.fraTermNavn(v.getTittel())));
            dokumentTypeFraKjenteTitler(d.getTittel()).ifPresent(alletyper::add);
            d.getLogiskeVedlegg().forEach(v -> dokumentTypeFraKjenteTitler(v.getTittel()).ifPresent(alletyper::add));
            allebrevkoder.add(NAVSkjema.fraOffisiellKode(d.getBrevkode()));
            allebrevkoder.add(NAVSkjema.fraTermNavn(d.getTittel()));
            d.getLogiskeVedlegg().forEach(v -> allebrevkoder.add(NAVSkjema.fraTermNavn(v.getTittel())));
        });
        allebrevkoder.forEach(b -> alletyper.add(MapNAVSkjemaDokumentTypeId.mapBrevkode(b)));
        return alletyper;
    }

    private static DokumentTypeId utledHovedDokumentType(Set<DokumentTypeId> alleTyper) {
        int lavestrank = alleTyper.stream()
                .map(MapNAVSkjemaDokumentTypeId::dokumentTypeRank)
                .min(Comparator.naturalOrder()).orElse(MapNAVSkjemaDokumentTypeId.UDEF_RANK);
        if (lavestrank == MapNAVSkjemaDokumentTypeId.GEN_RANK) {
            return alleTyper.stream()
                    .filter(t -> MapNAVSkjemaDokumentTypeId.dokumentTypeRank(t) == MapNAVSkjemaDokumentTypeId.GEN_RANK)
                    .findFirst().orElse(DokumentTypeId.UDEFINERT);
        }
        return MapNAVSkjemaDokumentTypeId.dokumentTypeFromRank(lavestrank);
    }

    private Optional<String> mapIdent(Journalpost journalpost) {
        var bruker = journalpost.getBruker();
        if (bruker == null) {
            return Optional.empty();
        }
        if (bruker.erAktoerId()) {
            return Optional.of(bruker.getId());
        } else if (BrukerIdType.FNR.equals(bruker.getType())) {
            return personTjeneste.hentAktørIdForPersonIdent(bruker.getId());
        } else if (BrukerIdType.ORGNR.equals(bruker.getType())) {
            return Optional.empty();
        }
        throw new IllegalArgumentException("Ukjent brukerType=" + bruker.getType());
    }

    private OpprettJournalpostRequest lagOpprettRequest(UUID forsendelseId, String avsenderAktørId) {
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        var dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        var opprettDokumenter = lagAlleDokumentForOpprett(dokumenter);
        var dokumenttyper = dokumenter.stream().map(Dokument::getDokumentTypeId).collect(Collectors.toSet());
        var hovedtype = utledHovedDokumentType(dokumenttyper);
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
        request.setEksternReferanseId(forsendelseId.toString());
        request.setBruker(bruker);
        request.setAvsenderMottaker(avsender);
        request.setDokumenter(opprettDokumenter);

        return request;
    }

    private static List<DokumentInfoOpprett> lagAlleDokumentForOpprett(List<Dokument> dokumenter) {
        List<DokumentInfoOpprett> dokumenterRequest = new ArrayList<>();
        var hoveddokument = dokumenter.stream().filter(Dokument::erHovedDokument).collect(Collectors.toList());
        if (!hoveddokument.isEmpty()) {
            var strukturert = hoveddokument.stream()
                    .filter(dok -> ArkivFilType.XML.equals(dok.getArkivFilType()))
                    .findFirst()
                    .map(dok -> new Dokumentvariant(Variantformat.ORIGINAL, dok.getArkivFilType().getKode(), dok.getBase64EncodetDokument()))
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
        varianter.add(new Dokumentvariant(Variantformat.ARKIV, dokument.getArkivFilType().getKode(), dokument.getBase64EncodetDokument()));
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
