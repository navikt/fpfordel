package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentFeil;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.v1.Søknad;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.dokumentforsendelse.BehandleDokumentforsendelseTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class DokumentforsendelseTjenesteImpl implements DokumentforsendelseTjeneste {
    public static final MediaType APPLICATION_PDF_TYPE = MediaType.valueOf("application/pdf");
    private static final Collection<ArkivFilType> PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER = new HashSet<>();

    static {
        PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER.add(ArkivFilType.XML);
        PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER.add(ArkivFilType.PDFA);
    }

    private DokumentRepository repository;
    private KodeverkRepository kodeverkRepository;
    private ProsessTaskRepository prosessTaskRepository;
    private AktørConsumer aktørConsumer;

    public DokumentforsendelseTjenesteImpl() { // For CDI
    }

    @Inject
    public DokumentforsendelseTjenesteImpl(DokumentRepository repository, KodeverkRepository kodeverkRepository,
                                           ProsessTaskRepository prosessTaskRepository, AktørConsumer aktørConsumer) {
        this.repository = repository;
        this.kodeverkRepository = kodeverkRepository;
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
            if (Søknad.class.isInstance(abstractDto)) {
                ((Søknad)abstractDto).sjekkNødvendigeFeltEksisterer(dokument.getForsendelseId());
            }
            if (no.nav.foreldrepenger.mottak.domene.v2.Søknad.class.isInstance(abstractDto)) {
                ((no.nav.foreldrepenger.mottak.domene.v2.Søknad) abstractDto).sjekkNødvendigeFeltEksisterer(dokument.getForsendelseId());
            }
            if (no.nav.foreldrepenger.mottak.domene.v3.Søknad.class.isInstance(abstractDto)) {
                ((no.nav.foreldrepenger.mottak.domene.v3.Søknad) abstractDto).sjekkNødvendigeFeltEksisterer(dokument.getForsendelseId());
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
                .hoveddokumentSkalSendesSomToDokumenter(CONTENT_TYPE, APPLICATION_XML, APPLICATION_PDF_TYPE).toException();
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
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository,
                prosessTaskData);
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

        Set<ArkivFilType> dokumentArkivFilTyper = hoveddokumentene.stream().map(Dokument::getArkivFilType).collect(toSet());
        if (dokumentArkivFilTyper.size() != 2) {
            return false;
        }

        Predicate<ArkivFilType> aftCheck = aft -> dokumentArkivFilTyper.stream().anyMatch(aft::equals);
        long påkrevdeFunnetIhoveddokumentene = PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER.stream().filter(aftCheck).count();
        return påkrevdeFunnetIhoveddokumentene == 2;
    }

}