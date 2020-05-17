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
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

@ApplicationScoped
public class TilJournalføringTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(TilJournalføringTjeneste.class);

    private JournalTjeneste journal;
    private DokumentRepository dokumentRepository;
    private AktørConsumerMedCache aktørConsumer;
    private PersonConsumer personConsumer;

    @Inject
    public TilJournalføringTjeneste(JournalTjeneste journalTjeneste, DokumentRepository dokumentRepository,
                                    AktørConsumerMedCache aktørConsumer, PersonConsumer personConsumer) {
        this.journal = journalTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.aktørConsumer = aktørConsumer;
        this.personConsumer = personConsumer;
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

        builder.medAvsender(avsenderId.orElse(metadata.getBrukerId()));

        return journal.journalførDokumentforsendelse(builder.build());
    }

    private boolean retteOppMangler(String sakId, String arkivId, String aktørId, String innhold,
            JournalPostMangler journalføringsbehov) {
        final JournalPost journalPost = new JournalPost(arkivId);
        List<JournalPostMangler.JournalMangel> manglene = journalføringsbehov.getMangler();
        var fnr = aktørConsumer.hentPersonIdentForAktørId(aktørId).orElse(null);

        LOG.info("Journalpost retter mangler: arkivsak {} mangler {}", arkivId, journalføringsbehov.getMangelTyper());
        for (JournalPostMangler.JournalMangel mangel : manglene) {
            switch (mangel.getMangeltype()) {
            case ARKIVSAK:
                journalPost.setArkivSakId(sakId);
                journalPost.setArkivSakSystem(Fagsystem.GOSYS.getKode());
                journalføringsbehov.rettetMangel(mangel);
                break;
            case AVSENDERID:
                journalPost.setAvsenderFnr(fnr);
                journalføringsbehov.rettetMangel(mangel);
                break;
            case AVSENDERNAVN:
                journalPost.setAvsenderFnr(fnr);
                journalPost.setAvsenderNavn(brukersNavn(fnr));
                journalføringsbehov.rettetMangel(mangel);
                break;
            case INNHOLD:
                journalPost.setInnhold(innhold);
                journalføringsbehov.rettetMangel(mangel);
                break;
            case TEMA:
                break;
            case BRUKER:
                journalPost.setFnr(fnr);
                journalføringsbehov.rettetMangel(mangel);
                break;
             case HOVEDOK_TITTEL:
                 journalPost.setHovedDokumentTittel(innhold);
                 journalPost.setHovedDokumentId(mangel.getDokumentId());
                 journalføringsbehov.rettetMangel(mangel);
                 break;
            default:
                // Too be implemented
                break;
            }
        }

        journal.oppdaterJournalpost(journalPost);

        if (journalføringsbehov.harMangler()) {
            LOG.info("Journalpost resterende mangler: arkivsak {} mangler {}", arkivId, journalføringsbehov.getMangelTyper());
        }

        return journalføringsbehov.harMangler();
    }

    private String brukersNavn(String fnr) {
        if (fnr == null)
            return null;
        PersonIdent personIdent = new PersonIdent();
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fnr);
        Personidenter type = new Personidenter();
        type.setValue(fnr.charAt(0) >= '4' && fnr.charAt(0) <= '7' ? "DNR" : "FNR");
        norskIdent.setType(type);
        personIdent.setIdent(norskIdent);
        HentPersonRequest request = new HentPersonRequest();
        request.setAktoer(personIdent);
        try {
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            return response.getPerson().getPersonnavn().getSammensattNavn();
        } catch (Exception e) {
            throw new IllegalArgumentException("Fant ikke person", e);
        }
    }
}
