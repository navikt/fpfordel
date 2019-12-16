package no.nav.foreldrepenger.mottak.tjeneste;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.journal.JournalPost;
import no.nav.foreldrepenger.mottak.journal.JournalPostMangler;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;

@ApplicationScoped
public class TilJournalføringTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(TilJournalføringTjeneste.class);

    private JournalTjeneste journal;
    private DokumentRepository dokumentRepository;

    @Inject
    public TilJournalføringTjeneste(JournalTjeneste journalTjeneste, DokumentRepository dokumentRepository) {
        this.journal = journalTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    public TilJournalføringTjeneste() {
    }

    public boolean tilJournalføring(String journalpostId, String sakId, String aktørId, String enhetId,
            String innhold) {

        var journalføringsbehov = journal.utledJournalføringsbehov(journalpostId);

        if (journalføringsbehov.harMangler()) {
            if (retteOppMangler(sakId, journalpostId, aktørId, innhold, journalføringsbehov)) {
                LOG.info("Journalpost har mangler som må rettes manuelt {}", journalpostId);
                return false;
            }
        }
        journal.ferdigstillJournalføring(journalpostId, enhetId);
        return true;
    }

    public DokumentforsendelseResponse journalførDokumentforsendelse(UUID forsendelseId,
            Optional<String> saksnummer,
            Optional<String> avsenderId,
            Boolean forsøkEndeligJF, Optional<String> retrySuffix) {
        var metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        var dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        var hoveddokument = dokumenter
                .stream()
                .filter(Dokument::erHovedDokument)
                .collect(Collectors.toList());

        var vedlegg = dokumenter.stream()
                .filter(dokument -> !dokument.erHovedDokument())
                .collect(Collectors.toList());

        if (forsøkEndeligJF && saksnummer.isEmpty()) {
            throw MottakMeldingFeil.FACTORY.manglerSaksnummerForJournalføring(forsendelseId).toException();
        }

        var builder = DokumentforsendelseRequest.builder();
        builder.medForsøkEndeligJF(forsøkEndeligJF);
        builder.medForsendelseId(metadata.getForsendelseId().toString());
        builder.medBruker(metadata.getBrukerId());
        builder.medForsendelseMottatt(metadata.getForsendelseMottatt());
        retrySuffix.ifPresent(builder::medRetrySuffix);
        builder.medHoveddokument(hoveddokument);
        builder.medVedlegg(vedlegg);

        saksnummer.ifPresent(builder::medSaksnummer);

        if (avsenderId.isPresent()) {
            builder.medAvsender(avsenderId.get());
        } else {
            builder.medAvsender(metadata.getBrukerId());
        }

        return journal.journalførDokumentforsendelse(builder.build());
    }

    private boolean retteOppMangler(String sakId, String arkivId, String aktørId, String innhold,
            JournalPostMangler journalføringsbehov) {
        final JournalPost journalPost = new JournalPost(arkivId);
        List<JournalPostMangler.JournalMangelType> manglene = journalføringsbehov.getMangler();
        for (JournalPostMangler.JournalMangelType mangel : manglene) {
            switch (mangel) {
            case ARKIVSAK:
                journalPost.setArkivSakId(sakId);
                journalPost.setArkivSakSystem(Fagsystem.GOSYS.getKode());
                journalføringsbehov.rettetMangel(mangel);
                break;
            case AVSENDERID:
                journalPost.setAvsenderAktørId(aktørId);
                journalføringsbehov.rettetMangel(mangel);
                break;
            case AVSENDERNAVN:
                break;
            case INNHOLD:
                journalPost.setInnhold(innhold);
                journalføringsbehov.rettetMangel(mangel);
                break;
            case TEMA:
                break;
            case BRUKER:
                journalPost.setAktørId(aktørId);
                journalføringsbehov.rettetMangel(mangel);
                break;
            default:
                // Too be implemented
                break;
            }
        }

        journal.oppdaterJournalpost(journalPost);

        if (journalføringsbehov.harMangler()) {
            String mangler = journalføringsbehov.getMangler().toString();
            LOG.info("Journalpost resterende mangler: arkivsak {} mangler {}", arkivId, mangler);
        }

        return journalføringsbehov.harMangler();
    }
}
