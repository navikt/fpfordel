package no.nav.foreldrepenger.mottak.tjeneste;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.JournalpostSender;
import no.nav.vedtak.felles.integrasjon.rest.NativeClient;

@Dependent
public class VLKlargjører {

    private static final Logger LOG = LoggerFactory.getLogger(VLKlargjører.class);

    private final JournalpostSender dokumentJournalpostSender;
    private final Fagsak fagsak;
    private final JournalpostSender tilbakeJournalpostSender;

    @Inject
    public VLKlargjører(
            @NativeClient("dokument") JournalpostSender dokumentJournalpostSender,
            @NativeClient Fagsak fagsak,
            @NativeClient("tilbake") JournalpostSender tilbakeJournalpostSender) {
        this.dokumentJournalpostSender = dokumentJournalpostSender;
        this.fagsak = fagsak;
        this.tilbakeJournalpostSender = tilbakeJournalpostSender;
    }

    public void klargjør(String xml, String saksnummer, String arkivId, DokumentTypeId dokumenttypeId,
            LocalDateTime forsendelseMottatt,
            BehandlingTema behandlingsTema, UUID forsendelseId, DokumentKategori dokumentKategori,
            String journalFørendeEnhet, String eksternReferanseId) {
        String behandlingTemaString = (behandlingsTema == null) || BehandlingTema.UDEFINERT.equals(behandlingsTema)
                ? BehandlingTema.UDEFINERT.getKode()
                : behandlingsTema.getOffisiellKode();
        String dokumentTypeIdOffisiellKode = null;
        String dokumentKategoriOffisiellKode = null;
        if (dokumenttypeId != null) {
            dokumentTypeIdOffisiellKode = dokumenttypeId.getOffisiellKode();
        }
        if (dokumentKategori != null) {
            dokumentKategoriOffisiellKode = dokumentKategori.getOffisiellKode();
        }
        fagsak.knyttSakOgJournalpost(new JournalpostKnyttningDto(saksnummer, arkivId));

        var journalpost = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString,
                dokumentTypeIdOffisiellKode, forsendelseMottatt, xml);
        journalpost.setForsendelseId(forsendelseId);
        journalpost.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
        journalpost.setJournalForendeEnhet(journalFørendeEnhet);
        journalpost.setEksternReferanseId(eksternReferanseId);
        dokumentJournalpostSender.send(journalpost);

        try {
            var tilbakeMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString,
                    dokumentTypeIdOffisiellKode, forsendelseMottatt, null);
            tilbakeMottakDto.setForsendelseId(forsendelseId);
            tilbakeMottakDto.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
            tilbakeMottakDto.setJournalForendeEnhet(journalFørendeEnhet);
            tilbakeJournalpostSender.send(tilbakeMottakDto);
        } catch (Exception e) {
            LOG.warn("Feil ved sending av forsendelse til fptilbake, ukjent feil", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [dokumentJournalpostSender=" + dokumentJournalpostSender + ", fagsak=" + fagsak
                + ", tilbakeJournalpostSender="
                + tilbakeJournalpostSender + "]";
    }
}
