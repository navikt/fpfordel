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
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;
import no.nav.foreldrepenger.mottak.journal.saf.model.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;
import no.nav.foreldrepenger.mottak.task.KlargjorForVLTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

@ApplicationScoped
public class ArkivTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ArkivTjeneste.class);

    private SafTjeneste safTjeneste;
    private AktørConsumer aktørConsumer;

    ArkivTjeneste() {
        // CDI
    }

    @Inject
    public ArkivTjeneste(SafTjeneste safTjeneste, AktørConsumer aktørConsumer) {
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

        info.setJournalpostId(journalpostId);
        info.setTilstand(journalpost.getJournalstatus());
        mapIdent(journalpost).ifPresent(info::setBrukerAktørId);
        info.setKanal(journalpost.getKanal());
        info.setAlleTyper(utledDokumentTyper(journalpost));
        info.setHovedtype(utledHovedDokumentType(info.getAlleTyper()));
        info.setTema(Tema.fraOffisiellKode(journalpost.getTema()));
        info.setBehandlingstema(BehandlingTema.fraOffisiellKode(journalpost.getBehandlingstema()));
        info.setJournalfoerendeEnhet(journalpost.getJournalfoerendeEnhet());
        info.setDatoOpprettet(journalpost.getDatoOpprettet());
        info.setEksternReferanseId(journalpost.getEksternReferanseId());

        return info;
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
                   .findFirst().orElse(DokumentTypeId.ANNET);
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

    public void loggSammenligning(MottakMeldingDataWrapper wrapper, Optional<String> brukerFraArkiv) {
        try {
            var ajp = hentArkivJournalpost(wrapper.getArkivId());
            if (DokumentTypeId.INNTEKTSMELDING.equals(ajp.getHovedtype())) {
                boolean fantInnhold = false;
                if (ajp.getInnholderStrukturertInformasjon()) {
                    fantInnhold = true;
                    MottakMeldingDataWrapper testWrapper = wrapper.nesteSteg(KlargjorForVLTask.TASKNAME);
                    MottattStrukturertDokument<?> mottattDokument = MeldingXmlParser.unmarshallXml(ajp.getStrukturertPayload());
                    mottattDokument.kopierTilMottakWrapper(testWrapper, aktørConsumer::hentAktørIdForPersonIdent);
                    if (!Objects.equals(wrapper.getAktørId(), testWrapper.getAktørId()))
                        LOG.info("FPFORDEL SAF avvik journalpost {} omhandler saf {} ij {}", wrapper.getArkivId(), testWrapper.getAktørId(), wrapper.getAktørId());
                }
                LOG.info("FPFORDEL SAF inntektsmelding journalpost {} innhold {} bruker JP {}", wrapper.getArkivId(), fantInnhold ? "xml" : "tom", ajp.getBrukerAktørId());
            }
            if (!Objects.equals(ajp.getBrukerAktørId(), brukerFraArkiv.orElse(null)))
                LOG.info("FPFORDEL SAF avvik journalpost {} bruker saf {} ij {}", wrapper.getArkivId(), ajp.getBrukerAktørId(), brukerFraArkiv);
            if (!Objects.equals(ajp.getJournalfoerendeEnhet(), wrapper.getJournalførendeEnhet().orElse(null)))
                LOG.info("FPFORDEL SAF avvik journalpost {} jfEnhet", wrapper.getArkivId());
            if (!Objects.equals(ajp.getEksternReferanseId(), wrapper.getEksternReferanseId().orElse(null)))
                LOG.info("FPFORDEL SAF avvik journalpost {} eksternref", wrapper.getArkivId());
            if (ajp.getInnholderStrukturertInformasjon() && !Objects.equals(ajp.getStrukturertPayload(), wrapper.getPayloadAsString().orElse(null)))
                LOG.info("FPFORDEL SAF avvik journalpost {} payload", wrapper.getArkivId());
            if (!Objects.equals(ajp.getHovedtype(), wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT)))
                LOG.info("FPFORDEL SAF avvik journalpost {} dokumenttypeid SAF {} IJ {} allesaf {}",
                        wrapper.getArkivId(), ajp.getHovedtype(), wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT), ajp.getAlleTyper());

            LOG.info("FPFORDEL SAF sammenlignet journalpost {} dokumenttypeid SAF {} IJ {} tilstandSAF {}",
                    wrapper.getArkivId(), ajp.getHovedtype(), wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT), ajp.getTilstand());

            if (MapNAVSkjemaDokumentTypeId.dokumentTypeRank(ajp.getHovedtype()) < MapNAVSkjemaDokumentTypeId.dokumentTypeRank(wrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT)))
                wrapper.setDokumentTypeId(ajp.getHovedtype());
        } catch (Exception e) {
            LOG.info("Noe rart skjedde", e);
        }
    }
}
