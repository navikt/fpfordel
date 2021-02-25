package no.nav.foreldrepenger.mottak.tjeneste;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.mottak.klient.FagsakTjeneste;
import no.nav.foreldrepenger.mottak.klient.JournalpostSender;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@ApplicationScoped
public class KlargjørForVLTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(KlargjørForVLTjeneste.class);

    private JournalpostSender restKlient;
    private FagsakTjeneste fagsakRestKlient;
    private JournalpostSender tilbakekrevingRestKlient;

    @Inject
    public KlargjørForVLTjeneste(
            @Jersey("dokument") JournalpostSender restKlient,
            /* @Jersey */FagsakTjeneste fagsakRestKlient,
            @Jersey("tilbake") JournalpostSender tilbakekrevingRestKlient) {
        this.restKlient = restKlient;
        this.fagsakRestKlient = fagsakRestKlient;
        this.tilbakekrevingRestKlient = tilbakekrevingRestKlient;
    }

    public KlargjørForVLTjeneste() {
    }

    public void klargjørForVL(String xml, String saksnummer, String arkivId, DokumentTypeId dokumenttypeId,
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
        fagsakRestKlient.knyttSakOgJournalpost(new JournalpostKnyttningDto(saksnummer, arkivId));

        var journalpostMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString,
                dokumentTypeIdOffisiellKode, forsendelseMottatt, xml);
        journalpostMottakDto.setForsendelseId(forsendelseId);
        journalpostMottakDto.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
        journalpostMottakDto.setJournalForendeEnhet(journalFørendeEnhet);
        journalpostMottakDto.setEksternReferanseId(eksternReferanseId);
        restKlient.send(journalpostMottakDto);

        try {
            var tilbakeMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString,
                    dokumentTypeIdOffisiellKode, forsendelseMottatt, null);
            tilbakeMottakDto.setForsendelseId(forsendelseId);
            tilbakeMottakDto.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
            tilbakeMottakDto.setJournalForendeEnhet(journalFørendeEnhet);
            tilbakekrevingRestKlient.send(tilbakeMottakDto);
        } catch (Exception e) {
            LOG.warn("Feil ved sending av forsendelse til fptilbake, ukjent feil", e);
        }
    }

}
