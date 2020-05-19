package no.nav.foreldrepenger.mottak.tjeneste;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;

@ApplicationScoped
public class TilJournalføringTjeneste {

    private JournalTjeneste journal;
    private DokumentRepository dokumentRepository;

    @Inject
    public TilJournalføringTjeneste(JournalTjeneste journalTjeneste, DokumentRepository dokumentRepository) {
        this.journal = journalTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    public TilJournalføringTjeneste() {
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

}
