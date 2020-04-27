package no.nav.foreldrepenger.mottak.journal;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.MapNAVSkjemaDokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;
import no.nav.foreldrepenger.mottak.journal.saf.model.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;

@ApplicationScoped
public class ArkivTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ArkivTjeneste.class);

    private SafTjeneste safTjeneste;
    private AktørConsumerMedCache aktørConsumer;

    ArkivTjeneste() {
        // CDI
    }

    @Inject
    public ArkivTjeneste(SafTjeneste safTjeneste, AktørConsumerMedCache aktørConsumer) {
        this.safTjeneste = safTjeneste;
        this.aktørConsumer = aktørConsumer;
    }

    public ArkivJournalpost hentArkivJournalpost(String journalpostId) {
        var journalpost = safTjeneste.hentJournalpostInfo(journalpostId);

        var info = new ArkivJournalpost();

        var infoList = journalpost.getDokumenter().stream()
                .filter(it -> it.getDokumentvarianter().stream().anyMatch(at -> VariantFormat.ORIGINAL.equals(at.getVariantFormat())))
                .map(DokumentInfo::getDokumentInfoId)
                .collect(Collectors.toList());

        if (infoList.size() > 1) {
            throw new IllegalStateException("Journalposten har flere dokumenter med VariantFormat = ORIGINAL");
        } else if (!infoList.isEmpty()) {
            var dokumentInfo = infoList.get(0);
            var payload = safTjeneste.hentDokument(journalpostId, dokumentInfo, VariantFormat.ORIGINAL);
            info.setStrukturertPayload(payload);
            info.setDokumentInfoId(dokumentInfo);
        }

        Set<DokumentTypeId> alleTyper = utledDokumentTyper(journalpost);
        BehandlingTema behandlingTema = utledBehandlingTema(journalpost.getBehandlingstema(), alleTyper);
        info.setJournalpostId(journalpostId);
        info.setTilstand(journalpost.getJournalstatus());
        mapIdent(journalpost).ifPresent(info::setBrukerAktørId);
        info.setKanal(journalpost.getKanal());
        info.setAlleTyper(alleTyper);
        info.setHovedtype(utledHovedDokumentType(alleTyper));
        info.setTema(Tema.fraOffisiellKode(journalpost.getTema()));
        info.setBehandlingstema(behandlingTema);
        info.setJournalfoerendeEnhet(journalpost.getJournalfoerendeEnhet());
        info.setDatoOpprettet(journalpost.getDatoOpprettet());
        info.setEksternReferanseId(journalpost.getEksternReferanseId());

        return info;
    }

    private BehandlingTema utledBehandlingTema(String btJournalpost, Set<DokumentTypeId> dokumenttyper) {
        BehandlingTema bt = BehandlingTema.fraOffisiellKode(btJournalpost);
        for (DokumentTypeId type : dokumenttyper) {
            bt = HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(bt, type);
        }
        return bt;
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
                .min(Comparator.naturalOrder()).orElse(99);
        if (lavestrank == 90) {
            return alleTyper.stream()
                   .filter(t -> MapNAVSkjemaDokumentTypeId.dokumentTypeRank(t) == 90)
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

    public Optional<DokumentTypeId> loggSammenligning(MottakMeldingDataWrapper wrapper, Optional<String> brukerFraArkiv) {
        try {
            var ajp = hentArkivJournalpost(wrapper.getArkivId());
            if (DokumentTypeId.INNTEKTSMELDING.equals(ajp.getHovedtype())) {
                /*boolean fantInnhold = false;
                if (ajp.getInnholderStrukturertInformasjon()) {
                    fantInnhold = true;
                    MottakMeldingDataWrapper testWrapper = wrapper.nesteSteg(KlargjorForVLTask.TASKNAME);
                    MottattStrukturertDokument<?> mottattDokument = MeldingXmlParser.unmarshallXml(ajp.getStrukturertPayload());
                    mottattDokument.kopierTilMottakWrapper(testWrapper, aktørConsumer::hentAktørIdForPersonIdent);
                }*/
            }
            if (!Objects.equals(ajp.getHovedtype(), wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT)))
                LOG.info("FPFORDEL SAF avvik journalpost {} dokumenttypeid SAF {} IJ {} allesaf {}",
                        wrapper.getArkivId(), ajp.getHovedtype(), wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT), ajp.getAlleTyper());

            LOG.info("FPFORDEL SAF sammenlignet journalpost {} dokumenttypeid SAF {} IJ {} tilstandSAF {}",
                    wrapper.getArkivId(), ajp.getHovedtype(), wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT), ajp.getTilstand());

            if (MapNAVSkjemaDokumentTypeId.dokumentTypeRank(ajp.getHovedtype()) < MapNAVSkjemaDokumentTypeId.dokumentTypeRank(wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT)))
                return Optional.of(ajp.getHovedtype());

        } catch (Exception e) {
            LOG.info("Noe rart skjedde", e);
        }
        return Optional.empty();
    }

    public Optional<DokumentTypeId> loggSammenligningManuell(String journalpostId, DokumentTypeId dokumentTypeId) {
        try {
            var ajp = hentArkivJournalpost(journalpostId);
            if (!Objects.equals(ajp.getHovedtype(), dokumentTypeId))
                LOG.info("FPFORDEL SAF manuell avvik journalpost {} dokumenttypeid SAF {} IJ {} allesaf {}",
                        journalpostId, ajp.getHovedtype(), dokumentTypeId, ajp.getAlleTyper());

            LOG.info("FPFORDEL SAF manuell sammenlign journalpost {} dokumenttypeid SAF {} IJ {} tilstandSAF {}",
                    journalpostId, ajp.getHovedtype(), dokumentTypeId, ajp.getTilstand());

            if (MapNAVSkjemaDokumentTypeId.dokumentTypeRank(ajp.getHovedtype()) < MapNAVSkjemaDokumentTypeId.dokumentTypeRank(dokumentTypeId))
                return Optional.of(ajp.getHovedtype());

        } catch (Exception e) {
            LOG.info("Noe rart skjedde", e);
        }
        return Optional.empty();
    }

    public Boolean kanOppretteSak(String journalpostId) {
        var ajp = hentArkivJournalpost(journalpostId);
        return DokumentTypeId.erFørsteSøknadType(ajp.getHovedtype()) || DokumentTypeId.INNTEKTSMELDING.equals(ajp.getHovedtype());
    }
}
