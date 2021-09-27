package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import static java.util.stream.Collectors.toSet;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static no.nav.foreldrepenger.fordel.StringUtil.partialMask;

import java.util.List;
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
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
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
    private ProsessTaskTjeneste prosessTaskTjeneste;
    private PersonInformasjon person;

    public DokumentforsendelseTjenesteImpl() {
    }

    @Inject
    public DokumentforsendelseTjenesteImpl(DokumentRepository repository,
                                           ProsessTaskTjeneste prosessTaskTjeneste,
                                           @Jersey("onbehalf") PersonInformasjon person) {
        this.repository = repository;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
        this.person = person;
    }

    @Override
    public void lagreForsendelseValider(DokumentMetadata metadata, List<Dokument> dokumenter, Optional<String> avsenderId) {
        var hoveddokumenter = dokumenter.stream().filter(Dokument::erHovedDokument).collect(toSet());
        if (hoveddokumenter.isEmpty() && metadata.getSaksnummer().isEmpty()) {
            throw new TekniskException("FP-728553", "Saksnummer er påkrevd ved ettersendelser");
        } else if (!hoveddokumenter.isEmpty() && !korrektAntallOgTyper(hoveddokumenter)) {
            throw new TekniskException("FP-728555", String.format("Hoveddokumentet skal alltid sendes som to dokumenter med %s: %s og %s",
                    CONTENT_TYPE, APPLICATION_XML, APPLICATION_PDF_TYPE));
        }

        repository.lagre(metadata);
        dokumenter.forEach(repository::lagre);


        if (hoveddokumenter.isEmpty() && metadata.getSaksnummer().isPresent()) {
            opprettProsessTask(metadata.getForsendelseId(), avsenderId);
        } else if (!hoveddokumenter.isEmpty() && korrektAntallOgTyper(hoveddokumenter)) {
            opprettProsessTask(metadata.getForsendelseId(), avsenderId);
        } else {
            throw new IllegalStateException("Utviklerfeil: Logisk brist");
        }
    }

    @Override
    public ForsendelseStatusDto finnStatusinformasjon(UUID forsendelseId) {
        return finnStatusinformasjonHvisEksisterer(forsendelseId)
                .orElseThrow(() -> new TekniskException("FP-295614", String.format("Ukjent forsendelseId %s", forsendelseId)));
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

    private void opprettProsessTask(UUID forsendelseId, Optional<String> avsenderId) {
        var prosessTaskData = ProsessTaskData.forProsessTaskHandler(BehandleDokumentforsendelseTask.class);
        prosessTaskData.setCallIdFraEksisterende();
        var dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
        dataWrapper.setForsendelseId(forsendelseId);
        avsenderId.ifPresent(dataWrapper::setAvsenderId);

        prosessTaskTjeneste.lagre(dataWrapper.getProsessTaskData());
    }

    @Override
    public Optional<String> bestemAvsenderAktørId(String aktørId) {
        String ident = SubjectHandler.getSubjectHandler().getUid();
        if (ident != null) {
            var aktørIdent = person.hentAktørIdForPersonIdent(ident);
            if ((aktørIdent.isPresent() && aktørId == null) || aktørIdent.filter(i -> !aktørId.equals(i)).isPresent()) {
                LOG.warn("Avvik mellom Subject.uid {} og bruker fra forsendelse {}", partialMask(ident), partialMask(aktørIdent.get()));
                return aktørIdent;
            }
        }
        return Optional.empty();

    }

    boolean korrektAntallOgTyper(Set<Dokument> hoveddokumentene) {
        if (hoveddokumentene.size() != 2) {
            return false;
        }

        var dokumentArkivFilTyper = hoveddokumentene.stream().map(Dokument::getArkivFilType)
                .collect(toSet());
        if (dokumentArkivFilTyper.size() != 2) {
            return false;
        }

        return PÅKREVDE_HOVEDDOKUMENT_ARKIV_FIL_TYPER.stream().filter((Predicate<ArkivFilType>) aft -> dokumentArkivFilTyper.stream().anyMatch(aft::equals)).count() == 2;
    }

}
