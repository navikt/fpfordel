package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static no.nav.vedtak.feil.LogLevel.WARN;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentFeil;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.dokumentforsendelse.BehandleDokumentforsendelseTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class DokumentforsendelseTjenesteImpl implements DokumentforsendelseTjeneste {
    public static final MediaType APPLICATION_PDF_TYPE = MediaType.valueOf("application/pdf");
    private static final Set<ArkivFilType> PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER = Set.of(
            ArkivFilType.XML,
            ArkivFilType.PDFA);

    private DokumentRepository repository;
    private ProsessTaskRepository prosessTaskRepository;
    private AktørConsumer aktørConsumer;

    public DokumentforsendelseTjenesteImpl() {
    }

    @Inject
    public DokumentforsendelseTjenesteImpl(DokumentRepository repository,
            ProsessTaskRepository prosessTaskRepository, AktørConsumer aktørConsumer) {
        this.repository = repository;
        this.prosessTaskRepository = prosessTaskRepository;
        this.aktørConsumer = aktørConsumer;
    }

    @Override
    public void nyDokumentforsendelse(DokumentMetadata metadata) {
        repository.lagre(metadata);
    }

    @Override
    public void lagreDokument(Dokument dokument) {
        if (dokument.erHovedDokument() && ArkivFilType.XML.equals(dokument.getArkivFilType())) {
            // Sjekker om nødvendige elementer er satt
            MottattStrukturertDokument<?> abstractDto = MeldingXmlParser.unmarshallXml(dokument.getKlartekstDokument());
            if (no.nav.foreldrepenger.mottak.domene.v3.Søknad.class.isInstance(abstractDto)) {
                ((no.nav.foreldrepenger.mottak.domene.v3.Søknad) abstractDto)
                        .sjekkNødvendigeFeltEksisterer(dokument.getForsendelseId());
            }
        }
        repository.lagre(dokument);
    }

    @Override
    public void validerDokumentforsendelse(UUID forsendelseId) {
        DokumentMetadata dokumentMetadata = repository.hentEksaktDokumentMetadata(forsendelseId);
        List<Dokument> dokumenter = repository.hentDokumenter(forsendelseId);

        Set<Dokument> hoveddokumenter = dokumenter.stream().filter(Dokument::erHovedDokument).collect(toSet());

        Optional<String> avsenderId = finnAvsenderId(dokumentMetadata);
        if (hoveddokumenter.isEmpty()) {
            if (dokumentMetadata.getSaksnummer().isPresent()) {
                opprettProsessTask(forsendelseId, avsenderId);
                return;
            }
            throw DokumentforsendelseTjenesteFeil.FACTORY.saksnummerPåkrevdVedEttersendelser().toException();
        }
        if (korrektAntallOgTyper(hoveddokumenter)) {
            opprettProsessTask(forsendelseId, avsenderId);
            return;
        }
        throw DokumentforsendelseTjenesteFeil.FACTORY
                .hoveddokumentSkalSendesSomToDokumenter(CONTENT_TYPE, APPLICATION_XML, APPLICATION_PDF_TYPE)
                .toException();
    }

    @Override
    public ForsendelseStatusDto finnStatusinformasjon(UUID forsendelseId) {
        Optional<DokumentMetadata> metadataOpt = repository.hentUnikDokumentMetadata(forsendelseId);
        if (!metadataOpt.isPresent()) {
            throw DokumentFeil.FACTORY.fantIkkeForsendelse(forsendelseId).toException();
        }
        DokumentMetadata dokumentMetadata = metadataOpt.get();
        ForsendelseStatus status = dokumentMetadata.getStatus();
        ForsendelseStatusDto forsendelseStatusDto = new ForsendelseStatusDto(status);

        if (status == ForsendelseStatus.PENDING) {
            forsendelseStatusDto.setPollInterval(POLL_INTERVALL);
        } else {
            dokumentMetadata.getArkivId().ifPresent(forsendelseStatusDto::setJournalpostId);
            dokumentMetadata.getSaksnummer().ifPresent(forsendelseStatusDto::setSaksnummer);
        }
        return forsendelseStatusDto;
    }

    private void opprettProsessTask(UUID forsendelseId, Optional<String> avsenderId) {
        ProsessTaskData prosessTaskData = new ProsessTaskData(BehandleDokumentforsendelseTask.TASKNAME);
        prosessTaskData.setCallIdFraEksisterende();
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
        dataWrapper.setForsendelseId(forsendelseId);
        avsenderId.ifPresent(dataWrapper::setAvsenderId);

        prosessTaskRepository.lagre(dataWrapper.getProsessTaskData());
    }

    private Optional<String> finnAvsenderId(DokumentMetadata metaData) {
        String ident = SubjectHandler.getSubjectHandler().getUid();
        if (ident != null) {
            Optional<String> aktørIdent = aktørConsumer.hentAktørIdForPersonIdent(ident);

            if (aktørIdent.isPresent() && !aktørIdent.get().equals(metaData.getBrukerId())) {
                return aktørIdent;
            }

        }
        return Optional.empty();

    }

    boolean korrektAntallOgTyper(Set<Dokument> hoveddokumentene) {
        if (hoveddokumentene.size() != 2) {
            return false;
        }

        Set<ArkivFilType> dokumentArkivFilTyper = hoveddokumentene.stream().map(Dokument::getArkivFilType)
                .collect(toSet());
        if (dokumentArkivFilTyper.size() != 2) {
            return false;
        }

        Predicate<ArkivFilType> aftCheck = aft -> dokumentArkivFilTyper.stream().anyMatch(aft::equals);
        long påkrevdeFunnetIhoveddokumentene = PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER.stream().filter(aftCheck).count();
        return påkrevdeFunnetIhoveddokumentene == 2;
    }

    private interface DokumentforsendelseTjenesteFeil extends DeklarerteFeil {
        DokumentforsendelseTjenesteImpl.DokumentforsendelseTjenesteFeil FACTORY = FeilFactory.create(DokumentforsendelseTjenesteImpl.DokumentforsendelseTjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-728553", feilmelding = "Saksnummer er påkrevd ved ettersendelser", logLevel = WARN)
        Feil saksnummerPåkrevdVedEttersendelser();

        @TekniskFeil(feilkode = "FP-728555", feilmelding = "Hoveddokumentet skal alltid sendes som to dokumenter med %s: %s og %s", logLevel = WARN)
        Feil hoveddokumentSkalSendesSomToDokumenter(String content_type, String dokumenttype1, MediaType dokumenttype2);
    }

}