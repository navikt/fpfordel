package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.dokumentforsendelse.BehandleDokumentforsendelseTask;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ApplicationScoped
public class DokumentforsendelseTjenesteImpl implements DokumentforsendelseTjeneste {
    public static final MediaType APPLICATION_PDF_TYPE = MediaType.valueOf("application/pdf");
    private static final Set<ArkivFilType> PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER = Set.of(ArkivFilType.XML, ArkivFilType.PDFA);

    private static final Logger LOG = LoggerFactory.getLogger(DokumentforsendelseTjenesteImpl.class);
    private DokumentRepository repository;
    private ProsessTaskTjeneste prosessTaskTjeneste;

    public DokumentforsendelseTjenesteImpl() {
    }

    @Inject
    public DokumentforsendelseTjenesteImpl(DokumentRepository repository, ProsessTaskTjeneste prosessTaskTjeneste) {
        this.repository = repository;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void lagreForsendelseValider(DokumentMetadata metadata, List<Dokument> dokumenter) {
        var hoveddokumenter = dokumenter.stream().filter(Dokument::erHovedDokument).collect(toSet());
        if (hoveddokumenter.isEmpty() && metadata.getSaksnummer().isEmpty()) {
            throw new TekniskException("FP-728553", "Saksnummer er påkrevd ved ettersendelser");
        } else if (!hoveddokumenter.isEmpty() && !korrektAntallOgTyper(hoveddokumenter)) {
            throw new TekniskException("FP-728555",
                String.format("Hoveddokumentet skal alltid sendes som to dokumenter med %s: %s og %s", CONTENT_TYPE, APPLICATION_XML,
                    APPLICATION_PDF_TYPE));
        }

        repository.lagre(metadata);
        dokumenter.forEach(repository::lagre);

        if (hoveddokumenter.isEmpty() && metadata.getSaksnummer().isPresent()) {
            opprettProsessTask(metadata);
        } else if (!hoveddokumenter.isEmpty() && korrektAntallOgTyper(hoveddokumenter)) {
            opprettProsessTask(metadata);
        } else {
            throw new IllegalStateException("Utviklerfeil: Logisk brist");
        }
    }

    @Override
    public ForsendelseStatusDto finnStatusinformasjon(UUID forsendelseId) {
        return finnStatusinformasjonHvisEksisterer(forsendelseId).orElseThrow(
            () -> new TekniskException("FP-295614", String.format("Ukjent forsendelseId %s", forsendelseId)));
    }

    @Override
    public Optional<ForsendelseStatusDto> finnStatusinformasjonHvisEksisterer(UUID forsendelseId) {
        LOG.info("Finner statusinformasjon");
        var info = repository.hentUnikDokumentMetadata(forsendelseId).map(dokumentMetadata -> {
            LOG.info("Fant dokumentMetadata {}", dokumentMetadata);
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
        LOG.info("Fant statusinformasjon {}", info);
        return info;
    }

    private void opprettProsessTask(DokumentMetadata metadata) {
        var prosessTaskData = ProsessTaskData.forProsessTask(BehandleDokumentforsendelseTask.class);
        prosessTaskData.setCallIdFraEksisterende();
        var dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
        dataWrapper.setForsendelseId(metadata.getForsendelseId());
        dataWrapper.setForsendelseMottattTidspunkt(metadata.getForsendelseMottatt());

        prosessTaskTjeneste.lagre(dataWrapper.getProsessTaskData());
    }

    boolean korrektAntallOgTyper(Set<Dokument> hoveddokumentene) {
        if (hoveddokumentene.size() != 2) {
            return false;
        }

        var dokumentArkivFilTyper = hoveddokumentene.stream().map(Dokument::getArkivFilType).collect(toSet());
        if (dokumentArkivFilTyper.size() != 2) {
            return false;
        }

        return PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER.stream().filter(aft -> dokumentArkivFilTyper.stream().anyMatch(aft::equals)).count() == 2;
    }

}
