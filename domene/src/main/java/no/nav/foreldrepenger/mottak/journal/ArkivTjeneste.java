package no.nav.foreldrepenger.mottak.journal;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.MapNAVSkjemaDokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.NAVSkjema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.DokArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.OppdaterJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.saf.SafTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;
import no.nav.foreldrepenger.mottak.journal.saf.model.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;

@ApplicationScoped
public class ArkivTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(ArkivTjeneste.class);

    private SafTjeneste safTjeneste;
    private DokArkivTjeneste dokArkivTjeneste;
    private AktørConsumerMedCache aktørConsumer;

    ArkivTjeneste() {
        // CDI
    }

    @Inject
    public ArkivTjeneste(SafTjeneste safTjeneste,
                         DokArkivTjeneste dokArkivTjeneste,
                         AktørConsumerMedCache aktørConsumer) {
        this.safTjeneste = safTjeneste;
        this.dokArkivTjeneste = dokArkivTjeneste;
        this.aktørConsumer = aktørConsumer;
    }

    public ArkivJournalpost hentArkivJournalpost(String journalpostId) {
        var journalpost = safTjeneste.hentJournalpostInfo(journalpostId);

        var builder = ArkivJournalpost.getBuilder().medJournalpostId(journalpostId);

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
        if (journalpost.getAvsenderMottaker() != null) {
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

    public void oppdaterBehandlingstemaBruker(String journalpostId, String behandlingstema, String aktørId) {
        var builder = OppdaterJournalpostRequest.ny()
                .medBehandlingstema(behandlingstema)
                .medBruker(aktørId);
        dokArkivTjeneste.oppdaterJournalpost(journalpostId, builder.build());
        LOG.info("FPFORDEL INNTEKTSMELDING oppdaterte bt {} og bruker for {}", behandlingstema, journalpostId);
    }

    public void oppdaterBehandlingstemaFor(ArkivJournalpost journalpost) {
        if (BehandlingTema.UDEFINERT.equals(journalpost.getBehandlingstema()) &&
            !BehandlingTema.UDEFINERT.equals(journalpost.getUtledetBehandlingstema())) {
            var builder = OppdaterJournalpostRequest.ny()
                    .medBehandlingstema(journalpost.getUtledetBehandlingstema().getOffisiellKode());
            dokArkivTjeneste.oppdaterJournalpost(journalpost.getJournalpostId(), builder.build());
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

}
