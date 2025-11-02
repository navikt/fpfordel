package no.nav.foreldrepenger.mottak.journal;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.MapNAVSkjemaDokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.vedtak.felles.integrasjon.dokarkiv.DokArkiv;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Bruker;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Dokumentvariant;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.KnyttTilAnnenSakRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Sak;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Tilleggsopplysning;

@ApplicationScoped
public class ArkivTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ArkivTjeneste.class);

    private static final String KUNNE_IKKE_OPPDATERE_JP = "FPFORDEL Kunne ikke oppdatere journalpost ";

    private static final String FP_DOK_TYPE = "fp_innholdtype";

    // Fyll på med gjengangertitler som ikke omfattes av kodeverk DokumentTypeId
    private static final Map<String, DokumentTypeId> TITTEL_MAP = Map.of("Klage", DokumentTypeId.KLAGE_DOKUMENT, "Anke",
        DokumentTypeId.KLAGE_DOKUMENT);

    private SafTjeneste saf;
    private DokArkiv dokArkivTjeneste;
    private PersonInformasjon personTjeneste;

    ArkivTjeneste() {
        // CDI
    }

    @Inject
    public ArkivTjeneste(SafTjeneste saf, DokArkiv dokArkivTjeneste, PersonInformasjon personTjeneste) {
        this.saf = saf;
        this.dokArkivTjeneste = dokArkivTjeneste;
        this.personTjeneste = personTjeneste;
    }

    private static Set<DokumentTypeId> utledDokumentTyper(Journalpost journalpost) {
        Set<NAVSkjema> allebrevkoder = new HashSet<>();
        allebrevkoder.add(NAVSkjema.fraTermNavn(journalpost.tittel()));

        Set<DokumentTypeId> alletyper = new HashSet<>();
        alletyper.add(DokumentTypeId.fraTermNavn(journalpost.tittel()));

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
        Optional.ofNullable(journalpost.tilleggsopplysninger())
            .orElse(List.of())
            .stream()
            .filter(to -> FP_DOK_TYPE.equals(to.nokkel()))
            .map(to -> DokumentTypeId.fraOffisiellKode(to.verdi()))
            .filter(dt -> !DokumentTypeId.UDEFINERT.equals(dt))
            .forEach(alletyper::add);
        return alletyper;
    }

    private static BehandlingTema utledBehandlingTema(String btJournalpost, Set<DokumentTypeId> dokumenttyper) {
        var bt = BehandlingTema.fraOffisiellKode(btJournalpost);
        return ArkivUtil.behandlingTemaFraDokumentTypeSet(bt, dokumenttyper);
    }

    private static Optional<DokumentTypeId> dokumentTypeFraKjenteTitler(String tittel) {
        return Optional.ofNullable(tittel).map(TITTEL_MAP::get);
    }

    public static boolean harBrevKode(Journalpost journalpost, Set<NAVSkjema> brevkoder) {
        if (journalpost == null || brevkoder == null || brevkoder.isEmpty()) {
            return false;
        }
        Set<NAVSkjema> allebrevkoder = new HashSet<>();
        allebrevkoder.add(NAVSkjema.fraTermNavn(journalpost.tittel()));
        journalpost.dokumenter().forEach(d -> {
            allebrevkoder.add(NAVSkjema.fraOffisiellKode(d.brevkode()));
            allebrevkoder.add(NAVSkjema.fraTermNavn(d.tittel()));
            d.logiskeVedlegg().forEach(v -> allebrevkoder.add(NAVSkjema.fraTermNavn(v.tittel())));
        });
        return brevkoder.stream().anyMatch(allebrevkoder::contains);
    }

    public ArkivJournalpost hentArkivJournalpost(String journalpostId) {
        var journalpost = saf.hentJournalpostInfo(journalpostId);

        var builder = ArkivJournalpost.getBuilder().medJournalpost(journalpost).medJournalpostId(journalpostId);

        var infoList = journalpost.dokumenter()
            .stream()
            .filter(it -> it.dokumentvarianter().stream().anyMatch(at -> Dokumentvariant.Variantformat.ORIGINAL.equals(at.variantformat())))
            .map(DokumentInfo::dokumentInfoId)
            .toList();

        if (infoList.size() > 1) {
            throw new IllegalStateException("Journalposten har flere dokumenter med VariantFormat = ORIGINAL");
        } else if (!infoList.isEmpty() && Tema.FORELDRE_OG_SVANGERSKAPSPENGER.equals(Tema.fraOffisiellKode(journalpost.tema()))) {
            var dokumentInfo = infoList.getFirst();
            var payload = saf.hentDokument(journalpostId, dokumentInfo, Dokumentvariant.Variantformat.ORIGINAL);
            builder.medStrukturertPayload(payload).medDokumentInfoId(dokumentInfo);
        }

        var alleTyper = utledDokumentTyper(journalpost);
        var behandlingTema = utledBehandlingTema(journalpost.behandlingstema(), alleTyper);
        mapIdent(journalpost).ifPresent(builder::medBrukerAktørId);
        if ((journalpost.avsenderMottaker() != null) && (journalpost.avsenderMottaker().idType() != null)) {
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
            .medSaksnummer(Optional.ofNullable(journalpost.sak()).map(Sak::fagsakId).orElse(null))
            .build();
    }

    public Optional<String> hentEksternReferanseId(Journalpost journalpost) {
        var dokumentInfoId = journalpost.dokumenter().getFirst().dokumentInfoId();
        var referanse = saf.hentEksternReferanseId(dokumentInfoId).stream().map(Journalpost::eksternReferanseId).filter(Objects::nonNull).findFirst();
        var loggtekst = referanse.orElse("ingen");
        if (LOG.isInfoEnabled()) {
            LOG.info("FPFORDEL hentEksternReferanseId fant referanse {} for journalpost {}", loggtekst, journalpost.journalpostId());
        }
        return referanse;
    }

    public void oppdaterBehandlingstemaBruker(String journalpostId, DokumentTypeId dokumentTypeId, String behandlingstema, String aktørId) {
        var builder = OppdaterJournalpostRequest.ny()
            .medBehandlingstema(behandlingstema)
            .leggTilTilleggsopplysning(new Tilleggsopplysning(FP_DOK_TYPE, dokumentTypeId.getOffisiellKode()))
            .medBruker(aktørId);
        if (!dokArkivTjeneste.oppdaterJournalpost(journalpostId, builder.build())) {
            throw new IllegalStateException(KUNNE_IKKE_OPPDATERE_JP + journalpostId);
        }
    }

    public void oppdaterJournalpostBruker(String journalpostId, String fødselsnummer) {
        var aktørId = personTjeneste.hentAktørIdForPersonIdent(fødselsnummer).orElseThrow();
        if (!dokArkivTjeneste.oppdaterJournalpost(journalpostId, OppdaterJournalpostRequest.ny().medBruker(aktørId).build())) {
            throw new IllegalStateException(KUNNE_IKKE_OPPDATERE_JP + journalpostId + " med person opplysninger.");
        }
    }

    public void settTilleggsOpplysninger(ArkivJournalpost arkivJournalpost, DokumentTypeId defaultDokumentTypeId, boolean manuellJournalføring) {
        var journalpost = arkivJournalpost.getOriginalJournalpost();
        var tilleggDoktype = arkivJournalpost.getTilleggsopplysninger().stream().filter(to -> FP_DOK_TYPE.equals(to.nokkel())).toList();
        var tilleggAnnet = arkivJournalpost.getTilleggsopplysninger().stream().filter(to -> !FP_DOK_TYPE.equals(to.nokkel())).toList();
        //saksbehandler kan ha valgt ny tittel ifm manuellJournalføring
        var hovedtype = manuellJournalføring || DokumentTypeId.UDEFINERT.equals(arkivJournalpost.getHovedtype()) ?
            defaultDokumentTypeId : arkivJournalpost.getHovedtype();

        if (DokumentTypeId.UDEFINERT.equals(hovedtype)) {
            return;
        }

        if (tilleggDoktype.isEmpty() || tilleggDoktype.stream().noneMatch(to -> hovedtype.getOffisiellKode().equals(to.verdi()))) {
            var builder = OppdaterJournalpostRequest.ny();
            if (LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL oppdaterer/legger til tilleggsopplysninger for {} med {}", journalpost.journalpostId(), hovedtype.getOffisiellKode());
            }
            if (!tilleggAnnet.isEmpty()) {
                builder.medTilleggsopplysninger(tilleggAnnet);
            }
            builder.leggTilTilleggsopplysning(new Tilleggsopplysning(FP_DOK_TYPE, hovedtype.getOffisiellKode()));
            if (!dokArkivTjeneste.oppdaterJournalpost(journalpost.journalpostId(), builder.build())) {
                throw new IllegalStateException(KUNNE_IKKE_OPPDATERE_JP + journalpost.journalpostId());
            }
        }
    }

    public boolean oppdaterRettMangler(ArkivJournalpost arkivJournalpost,
                                       String aktørId,
                                       BehandlingTema behandlingTema,
                                       DokumentTypeId defaultDokumentTypeId) {
        var journalpost = arkivJournalpost.getOriginalJournalpost();
        var alleTyper = arkivJournalpost.getAlleTyper();
        var hovedtype = DokumentTypeId.UDEFINERT.equals(arkivJournalpost.getHovedtype()) ? defaultDokumentTypeId : arkivJournalpost.getHovedtype();
        var utledetBehandlingTema = utledBehandlingTema(
            BehandlingTema.UDEFINERT.equals(behandlingTema) ? journalpost.behandlingstema() : behandlingTema.getOffisiellKode(), alleTyper);
        var tittelMangler = false;
        var builder = OppdaterJournalpostRequest.ny();
        if ((journalpost.avsenderMottaker() == null) || (journalpost.avsenderMottaker().id() == null) || (journalpost.avsenderMottaker().navn()
            == null)) {
            var fnr = personTjeneste.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
            var navn = personTjeneste.hentNavn(utledetBehandlingTema, aktørId);
            if (LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL oppdaterer manglende avsender for {}", journalpost.journalpostId());
            }
            builder.medAvsender(fnr, navn);
        }
        if (journalpost.tittel() == null) {
            if (DokumentTypeId.UDEFINERT.equals(hovedtype)) {
                tittelMangler = true;
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("FPFORDEL oppdaterer manglende tittel for {}", journalpost.journalpostId());
                }
                builder.medTittel(hovedtype.getTermNavn());
            }
        }
        if (journalpost.tema() == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL oppdaterer manglende tema for {}", journalpost.journalpostId());
            }
            builder.medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode());
        }
        if ((journalpost.behandlingstema() == null) && !BehandlingTema.UDEFINERT.equals(utledetBehandlingTema)) {
            // Logges ikke da den nesten alltid oppdateres
            builder.medBehandlingstema(utledetBehandlingTema.getOffisiellKode());
        }
        if ((journalpost.bruker() == null) || (journalpost.bruker().id() == null)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL oppdaterer manglende bruker for {}", journalpost.journalpostId());
            }
            builder.medBruker(aktørId);
        }
        var oppdaterDok = journalpost.dokumenter()
            .stream()
            .filter(d -> (d.tittel() == null) || d.tittel().isEmpty())
            .filter(d -> d.brevkode() != null)
            .map(d -> new OppdaterJournalpostRequest.DokumentInfoOppdater(d.dokumentInfoId(), NAVSkjema.fraOffisiellKode(d.brevkode()).getTermNavn(),
                d.brevkode()))
            .toList();
        if (!oppdaterDok.isEmpty() && LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL oppdaterer manglende dokumenttitler for {}", journalpost.journalpostId());
        }
        oppdaterDok.forEach(builder::leggTilDokument);
        if (!dokArkivTjeneste.oppdaterJournalpost(journalpost.journalpostId(), builder.build())) {
            throw new IllegalStateException(KUNNE_IKKE_OPPDATERE_JP + journalpost.journalpostId());
        }
        var resultat = !tittelMangler && (journalpost.dokumenter().stream().filter(d -> (d.tittel() == null) || d.tittel().isEmpty()).count()
            == oppdaterDok.size());
        if (!resultat && LOG.isInfoEnabled()) {
            LOG.info("FPFORDEL oppdaterer gjenstår tittel eller dokumenttittel for {}", journalpost.journalpostId());
        }
        return resultat;
    }

    public void oppdaterJournalpostVedManuellJournalføring(String journalpostId, String nyJournalpostTittel, List<OppdaterJournalpostRequest.DokumentInfoOppdater> dokumenter,
                                                           ArkivJournalpost journalpost, String aktørId, BehandlingTema behandlingTema) {
        var originalJournalpost = journalpost.getOriginalJournalpost();
        var utledetBehandlingTema = utledBehandlingTema(
            BehandlingTema.UDEFINERT.equals(behandlingTema) ? originalJournalpost.behandlingstema() : behandlingTema.getOffisiellKode(), journalpost.getAlleTyper());
        var oppdaterJournalpostBuilder = OppdaterJournalpostRequest.ny();

        if ((originalJournalpost.avsenderMottaker() == null) || (originalJournalpost.avsenderMottaker().id() == null) || (originalJournalpost.avsenderMottaker().navn() == null)) {
            var fnr = personTjeneste.hentPersonIdentForAktørId(aktørId).orElseThrow(() -> new IllegalStateException("Mangler fnr for aktørid"));
            var navn = personTjeneste.hentNavn(utledetBehandlingTema, aktørId);
            if (LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL RESTJOURNALFØRING: oppdaterer manglende avsender for {}", originalJournalpost.journalpostId());
            }
            oppdaterJournalpostBuilder.medAvsender(fnr, navn);
        }

        if (originalJournalpost.tema() == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("FPFORDEL RESTJOURNALFØRING: oppdaterer manglende tema for {}", originalJournalpost.journalpostId());
            }
            oppdaterJournalpostBuilder.medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode());
        }

        if ((originalJournalpost.behandlingstema() == null) && !BehandlingTema.UDEFINERT.equals(utledetBehandlingTema)) {
            // Logges ikke da den nesten alltid oppdateres
            oppdaterJournalpostBuilder.medBehandlingstema(utledetBehandlingTema.getOffisiellKode());
        }

        if (originalJournalpost.tittel() == null && nyJournalpostTittel == null) {
            throw new IllegalStateException("FPFORDEL RESTJOURNALFØRING: Kan ikke ferdigstille journalpost uten tittel på journalpost");
        }

        if (nyJournalpostTittel != null) {
            LOG.info("FPFORDEL RESTJOURNALFØRING: Legger på ny journalpostTittel:{} for journalpostId:{} ", nyJournalpostTittel, journalpostId);
            oppdaterJournalpostBuilder.medTittel(nyJournalpostTittel);
        }

        if (!dokumenter.isEmpty()) {
            dokumenter.forEach( dok-> {
                LOG.info("FPFORDEL RESTJOURNALFØRING:Legger på tittel:{} for dokumentId:{} for journalspostId:{} ", dok.tittel(), dok.dokumentInfoId(), journalpostId);
                oppdaterJournalpostBuilder.leggTilDokument(dok);
            });
        }
        var journalpostRequest = oppdaterJournalpostBuilder.build();

        if (!dokArkivTjeneste.oppdaterJournalpost(journalpostId, journalpostRequest)) {
            throw new IllegalStateException(KUNNE_IKKE_OPPDATERE_JP + journalpostId);
        }
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

    public void oppdaterMedGenerellSak(String journalpostId, String aktørId) {
        var builder = OppdaterJournalpostRequest.ny()
            .medSak(new Sak(null, null, Sak.Sakstype.GENERELL_SAK))
            .medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode())
            .medBruker(aktørId);

        if (dokArkivTjeneste.oppdaterJournalpost(journalpostId, builder.build())) {
            LOG.info("FPFORDEL SAKSOPPDATERING oppdaterte {} med generell sak", journalpostId);
        } else {
            throw new IllegalStateException("FPFORDEL Kunne ikke knytte journalpost " + journalpostId + " til generell sak");
        }
    }

    public void ferdigstillJournalføring(String journalpostId, String enhet) {
        if (dokArkivTjeneste.ferdigstillJournalpost(journalpostId, enhet)) {
            LOG.info("FPFORDEL FERDIGSTILLING ferdigstilte journalpost {} enhet {}", journalpostId, enhet);
        } else {
            throw new IllegalStateException("FPFORDEL Kunne ikke ferdigstille journalpost " + journalpostId);
        }
    }

    public String knyttTilAnnenSak(ArkivJournalpost journalpost, String enhet, String sakId, String aktørId) {
        var bruker = new Bruker(aktørId, Bruker.BrukerIdType.AKTOERID);
        var knyttTilAnnenSakRequest = new KnyttTilAnnenSakRequest(Sak.Sakstype.FAGSAK.name(), sakId, "FS36", bruker,
            Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), enhet);
        var resultat =  dokArkivTjeneste.knyttTilAnnenSak(journalpost.getJournalpostId(), knyttTilAnnenSakRequest);
        if (resultat != null) {
            LOG.info("FPFORDEL KNYTTILANNENSAK journalpost {} ny sak {} ny journalpost {} enhet {}", journalpost.getJournalpostId(), sakId, resultat.nyJournalpostId(), enhet);
            return resultat.nyJournalpostId();
        } else {
            throw new IllegalStateException("FPFORDEL Kunne ikke knytte journalpost " + journalpost.getJournalpostId() + " til sak " + sakId);
        }
    }

    private Sak lagSakForSaksnummer(String saksnummer) {
        return new Sak(saksnummer, "FS36", Sak.Sakstype.FAGSAK);
    }

    private Optional<String> mapIdent(Journalpost journalpost) {
        var bruker = journalpost.bruker();
        if (bruker == null) {
            return Optional.empty();
        }
        if (Bruker.BrukerIdType.AKTOERID.equals(bruker.idType())) {
            return Optional.of(bruker.id());
        } else if (Bruker.BrukerIdType.FNR.equals(bruker.idType())) {
            return personTjeneste.hentAktørIdForPersonIdent(bruker.id());
        } else if (Bruker.BrukerIdType.ORGNR.equals(bruker.idType())) {
            return Optional.empty();
        }
        throw new IllegalArgumentException("Ukjent brukerType=" + bruker.idType());
    }

    public byte[] hentDokumet(String journalpostId, String dokumentId) {
        return saf.hentDokumentByteArray(journalpostId, dokumentId, Dokumentvariant.Variantformat.ARKIV);
    }

}
