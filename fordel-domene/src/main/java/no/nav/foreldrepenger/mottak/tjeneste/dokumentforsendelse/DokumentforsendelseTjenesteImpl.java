package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.dokumentforsendelse.BehandleDokumentforsendelseTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
@Transactional
public class DokumentforsendelseTjenesteImpl implements DokumentforsendelseTjeneste {
    public static final MediaType APPLICATION_PDF_TYPE = MediaType.valueOf("application/pdf");
    private static final Set<ArkivFilType> PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER = Set.of(
            ArkivFilType.XML,
            ArkivFilType.PDFA);

    private static final Logger LOG = LoggerFactory.getLogger(DokumentforsendelseTjenesteImpl.class);
    private DokumentRepository repository;
    private ProsessTaskRepository prosessTaskRepository;
    private PersonInformasjon person;

    public DokumentforsendelseTjenesteImpl() {
    }

    @Inject
    public DokumentforsendelseTjenesteImpl(DokumentRepository repository,
            ProsessTaskRepository prosessTaskRepository, @Jersey("onbehalf") PersonInformasjon person) {
        this.repository = repository;
        this.prosessTaskRepository = prosessTaskRepository;
        this.person = person;
    }

    @Override
    public void nyDokumentforsendelse(DokumentMetadata metadata) {
        repository.lagre(metadata);
    }

    @Override
    public void lagreDokument(Dokument dokument) {
        if (dokument.erHovedDokument() && ArkivFilType.XML.equals(dokument.getArkivFilType())) {
            // Sjekker om nødvendige elementer er satt
            var abstractDto = MeldingXmlParser.unmarshallXml(dokument.getKlartekstDokument());
            if (no.nav.foreldrepenger.mottak.domene.v3.Søknad.class.isInstance(abstractDto)) {
                ((no.nav.foreldrepenger.mottak.domene.v3.Søknad) abstractDto)
                        .sjekkNødvendigeFeltEksisterer(dokument.getForsendelseId());
            }
        }
        repository.lagre(dokument);
    }

    @Override
    public void validerDokumentforsendelse(UUID forsendelseId) {
        var dokumentMetadata = repository.hentEksaktDokumentMetadata(forsendelseId);
        var dokumenter = repository.hentDokumenter(forsendelseId);
        var hoveddokumenter = dokumenter.stream().filter(Dokument::erHovedDokument).collect(toSet());

        var avsenderId = finnAvsenderId(dokumentMetadata);
        if (hoveddokumenter.isEmpty()) {
            if (dokumentMetadata.getSaksnummer().isPresent()) {
                opprettProsessTask(forsendelseId, avsenderId);
                return;
            }
            throw new TekniskException("FP-728553", "Saksnummer er påkrevd ved ettersendelser");
        }
        if (korrektAntallOgTyper(hoveddokumenter)) {
            opprettProsessTask(forsendelseId, avsenderId);
            return;
        }
        throw new TekniskException("FP-728555", String.format("Hoveddokumentet skal alltid sendes som to dokumenter med %s: %s og %s",
                CONTENT_TYPE, APPLICATION_XML, APPLICATION_PDF_TYPE));
    }

    @Override
    public ForsendelseStatusDto finnStatusinformasjon(UUID forsendelseId) {
        return finnStatusinformasjonHvisEksisterer(forsendelseId)
                .orElseThrow(() -> new TekniskException("FP-295614", String.format("Ukjent forsendelseId %s", forsendelseId)));
    }

    @Override
    public Optional<ForsendelseStatusDto> finnStatusinformasjonHvisEksisterer(UUID forsendelseId) {
        LOG.info("Finner statusinformasjon");
        return repository.hentUnikDokumentMetadata(forsendelseId).map(dokumentMetadata -> {
            var status = dokumentMetadata.getStatus();
            var forsendelseStatusDto = new ForsendelseStatusDto(status);

            if (status == ForsendelseStatus.PENDING) {
                forsendelseStatusDto.setPollInterval(POLL_INTERVALL);
            } else {
                dokumentMetadata.getArkivId().ifPresent(forsendelseStatusDto::setJournalpostId);
                dokumentMetadata.getSaksnummer().ifPresent(forsendelseStatusDto::setSaksnummer);
            }
            return forsendelseStatusDto;
        });
    }

    private void opprettProsessTask(UUID forsendelseId, Optional<String> avsenderId) {
        var prosessTaskData = new ProsessTaskData(BehandleDokumentforsendelseTask.TASKNAME);
        prosessTaskData.setCallIdFraEksisterende();
        var dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
        dataWrapper.setForsendelseId(forsendelseId);
        avsenderId.ifPresent(dataWrapper::setAvsenderId);

        prosessTaskRepository.lagre(dataWrapper.getProsessTaskData());
    }

    private Optional<String> finnAvsenderId(DokumentMetadata metaData) {
        String ident = SubjectHandler.getSubjectHandler().getUid();
        if (ident != null) {
            var aktørIdent = person.hentAktørIdForPersonIdent(ident);
            var metadataBruker = metaData.getBrukerId();

            if (aktørIdent.filter(i -> !metadataBruker.equals(i)).isPresent()) {
                var identInfo = ident.length() > 4 ? "****" + ident.substring(ident.length() - 4) : ident;
                LOG.info("Avvik mellom Subject.uid {} og bruker fra forsendelse {}", identInfo,
                        aktørIdent.map(a -> "****" + a.substring(a.length() - 4)));
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
        return PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER.stream().filter(aftCheck).count() == 2;
    }

}
