package no.nav.foreldrepenger.mottak.journal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
import no.nav.foreldrepenger.mottak.journal.dokarkiv.AvsenderMottaker;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.AvsenderMottakerIdType;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.Bruker;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.DokArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.DokumentInfoOppdater;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.DokumentInfoOpprett;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.Dokumentvariant;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.OppdaterJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.OpprettJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.Sak;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.Variantformat;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;
import no.nav.foreldrepenger.mottak.journal.saf.model.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

@ApplicationScoped
public class ArkivTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ArkivTjeneste.class);

    private SafTjeneste safTjeneste;
    private DokArkivTjeneste dokArkivTjeneste;
    private DokumentRepository dokumentRepository;
    private AktørConsumerMedCache aktørConsumer;
    private PersonConsumer personConsumer;

    ArkivTjeneste() {
        // CDI
    }

    @Inject
    public ArkivTjeneste(SafTjeneste safTjeneste,
                         DokArkivTjeneste dokArkivTjeneste,
                         DokumentRepository dokumentRepository,
                         PersonConsumer personConsumer,
                         AktørConsumerMedCache aktørConsumer) {
        this.safTjeneste = safTjeneste;
        this.dokArkivTjeneste = dokArkivTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.personConsumer = personConsumer;
        this.aktørConsumer = aktørConsumer;
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
        if (journalpost.getAvsenderMottaker() != null && journalpost.getAvsenderMottaker().getType() != null) {
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
        if (dokArkivTjeneste.oppdaterJournalpost(journalpostId, builder.build())) {
            LOG.info("FPFORDEL INNTEKTSMELDING oppdaterte bt {} og bruker for {}", behandlingstema, journalpostId);
        } else {
            throw new IllegalStateException("FPFORDEL Kunne ikke oppdatere " + journalpostId);
        }
    }

    public boolean oppdaterRettMangler(ArkivJournalpost arkivJournalpost, String aktørId, BehandlingTema behandlingTema, DokumentTypeId defaultDokumentTypeId) {
        var journalpost = arkivJournalpost.getOriginalJournalpost();
        Set<DokumentTypeId> alleTyper = arkivJournalpost.getAlleTyper();
        var hovedtype = DokumentTypeId.UDEFINERT.equals(arkivJournalpost.getHovedtype()) ? defaultDokumentTypeId : arkivJournalpost.getHovedtype();
        var utledetBehandlingTema = utledBehandlingTema(BehandlingTema.UDEFINERT.equals(behandlingTema) ? journalpost.getBehandlingstema() : behandlingTema.getOffisiellKode(), alleTyper);
        var tittelMangler = false;
        var builder = OppdaterJournalpostRequest.ny();
        if (journalpost.getAvsenderMottaker() == null || journalpost.getAvsenderMottaker().getId() == null || journalpost.getAvsenderMottaker().getNavn() == null ) {
            var fnr = aktørConsumer.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
            var navn = brukersNavn(fnr);
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
        if (journalpost.getBehandlingstema() == null && !BehandlingTema.UDEFINERT.equals(utledetBehandlingTema)) {
            LOG.info("FPFORDEL oppdaterer manglende behandlingstema for {}", journalpost.getJournalpostId());
            builder.medBehandlingstema(utledetBehandlingTema.getOffisiellKode());
        }
        if (journalpost.getBruker() == null || journalpost.getBruker().getId() == null) {
            LOG.info("FPFORDEL oppdaterer manglende bruker for {}", journalpost.getJournalpostId());
            builder.medBruker(aktørId);
        }
        journalpost.getDokumenter().stream()
                .filter(d -> d.getTittel() == null || d.getTittel().isEmpty())
                .filter(d -> d.getBrevkode() != null)
                .map(d -> new DokumentInfoOppdater(d.getDokumentInfoId(), NAVSkjema.fraOffisiellKode(d.getBrevkode()).getTermNavn(), d.getBrevkode()))
                .forEach(builder::leggTilDokument);
        if (builder.harVerdier()) {
            if (dokArkivTjeneste.oppdaterJournalpost(journalpost.getJournalpostId(), builder.build())) {
                LOG.info("FPFORDEL oppdaterer journalpost med mangler {}", journalpost.getJournalpostId());
            } else {
                throw new IllegalStateException("FPFORDEL Kunne ikke oppdatere " + journalpost.getJournalpostId());
            }
        }
        return !tittelMangler && journalpost.getDokumenter().stream().noneMatch(d -> d.getTittel() == null);
    }

    public void ferdigstillJournalføring(String journalpostId, String arkivSakId, String enhet) {
        if (arkivSakId == null || enhet == null) {
            throw new IllegalArgumentException("FPFORDEL ferdigstill mangler saksnummer og enhet " + journalpostId);
        }
        var builder = OppdaterJournalpostRequest.ny().medArkivSak(arkivSakId);
        if (dokArkivTjeneste.oppdaterJournalpost(journalpostId, builder.build())) {
            LOG.info("FPFORDEL FERDIGSTILLING oppdaterte {} med sak {}", journalpostId, arkivSakId);
        } else {
            throw new IllegalStateException("FPFORDEL Kunne ikke knytte journalpost " + journalpostId + " til sak " + arkivSakId);
        }
        if (dokArkivTjeneste.ferdigstillJournalpost(journalpostId, enhet)) {
            LOG.info("FPFORDEL FERDIGSTILLING ferdigstilte journalpost {} enhet {}", journalpostId, enhet);
        } else {
            throw new IllegalStateException("FPFORDEL Kunne ikke ferdigstille journalpost " + journalpostId);
        }
    }

    private BehandlingTema utledBehandlingTema(String btJournalpost, Set<DokumentTypeId> dokumenttyper) {
        BehandlingTema bt = BehandlingTema.fraOffisiellKode(btJournalpost);
        return ArkivUtil.behandlingTemaFraDokumentTypeSet(bt, dokumenttyper);
    }

    private Set<DokumentTypeId> utledDokumentTyper(Journalpost journalpost) {
        Set<DokumentTypeId> alletyper = new HashSet<>();
        Set<NAVSkjema> allebrevkoder = new HashSet<>();
        alletyper.add(DokumentTypeId.fraTermNavn(journalpost.getTittel()));
        allebrevkoder.add(NAVSkjema.fraTermNavn(journalpost.getTittel()));
        journalpost.getDokumenter().forEach(d -> {
            alletyper.add(DokumentTypeId.fraTermNavn(d.getTittel()));
            d.getLogiskeVedlegg().forEach(v -> alletyper.add(DokumentTypeId.fraTermNavn(v.getTittel())));
            allebrevkoder.add(NAVSkjema.fraOffisiellKode(d.getBrevkode()));
            allebrevkoder.add(NAVSkjema.fraTermNavn(d.getTittel()));
            d.getLogiskeVedlegg().forEach(v -> allebrevkoder.add(NAVSkjema.fraTermNavn(v.getTittel())));
        });
        allebrevkoder.forEach(b -> alletyper.add(MapNAVSkjemaDokumentTypeId.mapBrevkode(b)));
        return alletyper;
    }

    private DokumentTypeId utledHovedDokumentType(Set<DokumentTypeId> alleTyper) {
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
            return aktørConsumer.hentAktørIdForPersonIdent(bruker.getId());
        } else if (BrukerIdType.ORGNR.equals(bruker.getType())) {
            return Optional.empty();
        }
        throw new IllegalArgumentException("Ukjent brukerType=" + bruker.getType());
    }

    private String mapAktørIdTilFnr(String aktørId) {
        if (aktørId == null)
            return null;
        return aktørConsumer.hentPersonIdentForAktørId(aktørId)
                .orElseThrow(() -> new IllegalStateException("Aktør uten personident"));
    }

    private String brukersNavn(String fnr) {
        if (fnr == null)
            return null;
        PersonIdent personIdent = new PersonIdent();
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fnr);
        Personidenter type = new Personidenter();
        type.setValue(fnr.charAt(0) >= '4' && fnr.charAt(0) <= '7' ? "DNR" : "FNR");
        norskIdent.setType(type);
        personIdent.setIdent(norskIdent);
        HentPersonRequest request = new HentPersonRequest();
        request.setAktoer(personIdent);
        try {
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            return response.getPerson().getPersonnavn().getSammensattNavn();
        } catch (Exception e) {
            throw new IllegalArgumentException("Fant ikke person", e);
        }
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
        var ident = mapAktørIdTilFnr(avsenderAktørId);
        var avsender = new AvsenderMottaker(ident, AvsenderMottakerIdType.FNR, brukersNavn(ident));

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

    private List<DokumentInfoOpprett> lagAlleDokumentForOpprett(List<Dokument> dokumenter) {
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

    private DokumentInfoOpprett lagDokumentForOpprett(Dokument dokument, Dokumentvariant struktuert) {
        List<Dokumentvariant> varianter = new ArrayList<>();
        if (struktuert != null)
            varianter.add(struktuert);
        varianter.add(new Dokumentvariant(Variantformat.ARKIV, dokument.getArkivFilType().getKode(), dokument.getBase64EncodetDokument()));
        var type = DokumentTypeId.UDEFINERT.equals(dokument.getDokumentTypeId()) ? DokumentTypeId.ANNET : dokument.getDokumentTypeId();
        var tittel = DokumentTypeId.ANNET.equals(type) && dokument.getBeskrivelse() != null ? dokument.getBeskrivelse() : type.getTermNavn();
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
