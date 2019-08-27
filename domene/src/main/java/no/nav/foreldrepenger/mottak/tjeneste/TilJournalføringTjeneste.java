package no.nav.foreldrepenger.mottak.tjeneste;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.journal.JournalPost;
import no.nav.foreldrepenger.mottak.journal.JournalPostMangler;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;

@ApplicationScoped
public class TilJournalføringTjeneste {

    private static final Logger log = LoggerFactory.getLogger(TilJournalføringTjeneste.class);

    private JournalTjeneste journalTjeneste;
    private DokumentRepository dokumentRepository;

    @Inject
    public TilJournalføringTjeneste(JournalTjeneste journalTjeneste, DokumentRepository dokumentRepository) {
        this.journalTjeneste = journalTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    public TilJournalføringTjeneste() {
        //NOSONAR for cdi
    }

    public boolean tilJournalføring(String journalpostId, String sakId, String aktørId, String enhetId, String innhold) {

        final JournalPostMangler journalføringsbehov = journalTjeneste.utledJournalføringsbehov(journalpostId);

        if (journalføringsbehov.harMangler()) {
            if (retteOppMangler(sakId, journalpostId, aktørId, innhold, journalføringsbehov)) {
                log.info("Journalpost har mangler som må rettes manuelt {}", journalpostId);
                return false;
            }
        }
        journalTjeneste.ferdigstillJournalføring(journalpostId, enhetId);
        return true;
    }

    public DokumentforsendelseResponse journalførDokumentforsendelse(UUID forsendelseId,
                                                                     Optional<String> saksnummer,
                                                                     Optional<String> avsenderId,
                                                                     Boolean forsøkEndeligJF, Optional<String> retrySuffix) {
        DokumentMetadata metadata = dokumentRepository.hentEksaktDokumentMetadata(forsendelseId);
        List<Dokument> dokumenter = dokumentRepository.hentDokumenter(forsendelseId);
        List<Dokument> hoveddokument = dokumenter.stream().filter(dokument -> dokument.erHovedDokument()).collect(Collectors.toList());
        List<Dokument> vedlegg = dokumenter.stream().filter(dokument -> !dokument.erHovedDokument()).collect(Collectors.toList());

        if (forsøkEndeligJF && !saksnummer.isPresent()) {
            throw MottakMeldingFeil.FACTORY.manglerSaksnummerForJournalføring(forsendelseId).toException();
        }

        DokumentforsendelseRequest.Builder builder = DokumentforsendelseRequest.builder();
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

        return journalTjeneste.journalførDokumentforsendelse(builder.build());
    }

    private boolean retteOppMangler(String sakId, String arkivId, String aktørId, String innhold, JournalPostMangler journalføringsbehov) {
        final JournalPost journalPost = new JournalPost(arkivId);
        List<JournalPostMangler.JournalMangelType> manglene = journalføringsbehov.getMangler();
        for (JournalPostMangler.JournalMangelType mangel : manglene) {
            switch (mangel) {
                case ARKIVSAK:
                    journalPost.setArkivSakId(sakId);
                    journalPost.setArkivSakSystem(Fagsystem.GOSYS.getOffisiellKode());
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

        journalTjeneste.oppdaterJournalpost(journalPost);

        if (journalføringsbehov.harMangler()) {
            String mangler = journalføringsbehov.getMangler().toString();
            log.info("Journalpost resterende mangler: arkivsak {} mangler {}", arkivId, mangler);
        }

        return journalføringsbehov.harMangler();
    }
}


