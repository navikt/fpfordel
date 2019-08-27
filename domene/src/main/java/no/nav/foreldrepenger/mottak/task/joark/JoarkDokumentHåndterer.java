package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste.hentMetadataForStrukturertDokument;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

@Dependent
class JoarkDokumentHåndterer {

    protected AktørConsumer aktørConsumer;
    private HentDataFraJoarkTjeneste hentDataFraJoarkTjeneste;
    private JournalDokument<DokumentTypeId> dokumentCached;

    @Inject
    JoarkDokumentHåndterer(HentDataFraJoarkTjeneste hentDataFraJoarkTjeneste, AktørConsumer aktørConsumer) {
        this.hentDataFraJoarkTjeneste = hentDataFraJoarkTjeneste;
        this.aktørConsumer = aktørConsumer;
    }

    @SuppressWarnings("unchecked")
    final <V> MottattStrukturertDokument<V> unmarshallXMLDokument(String xmlDokument) {
        return MeldingXmlParser.unmarshallXml(xmlDokument);
    }

    JournalDokument<DokumentTypeId> hentJournalDokument(List<JournalMetadata<DokumentTypeId>> hoveddokumenter) {
        JournalMetadata<DokumentTypeId> journalMetadata = hentMetadataForStrukturertDokument(hoveddokumenter);

        if (dokumentCached != null && dokumentCached.getMetadata().getDokumentId().equals(journalMetadata.getDokumentId())) {
            return dokumentCached;
        }

        Optional<JournalDokument<DokumentTypeId>> dokument = hentDataFraJoarkTjeneste.hentStrukturertJournalDokument(journalMetadata);
        if (dokument.isPresent()) {
            dokumentCached = dokument.get();
        } else {
            throw MottakMeldingFeil.FACTORY.hentDokumentIkkeFunnet().toException();
        }
        return dokumentCached;
    }

    List<JournalMetadata<DokumentTypeId>> hentJoarkDokumentMetadata(String journalpostId) {
        return hentDataFraJoarkTjeneste.hentDokumentMetadata(journalpostId);
    }

    private Set<String> hentUtAktørerFraMetadata(List<JournalMetadata<DokumentTypeId>> hoveddokumenter) {
        HashMap<String, String> brukere = new HashMap<>();
        for (JournalMetadata<DokumentTypeId> journalMetadata : hoveddokumenter) {
            for (String bruker : journalMetadata.getBrukerIdentListe()) {
                brukere.put(bruker, bruker);
            }
        }
        return brukere.keySet();
    }

    Optional<String> hentGyldigAktørFraMetadata(List<JournalMetadata<DokumentTypeId>> hoveddokumenter) {
        Set<String> personSet = hentUtAktørerFraMetadata(hoveddokumenter);
        if (personSet.isEmpty() || personSet.size() > 1) {
            return Optional.empty();
        }
        String personIdent = personSet.iterator().next();
        if ("".equals(personIdent)) {
            return Optional.empty();
        }
        return aktørConsumer.hentAktørIdForPersonIdent(personIdent);
    }

    Optional<String> hentGyldigAktørFraPersonident(String personIdent) {
        return aktørConsumer.hentAktørIdForPersonIdent(personIdent);
    }

}
