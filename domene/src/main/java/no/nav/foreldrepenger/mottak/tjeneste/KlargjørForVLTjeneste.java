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
import no.nav.foreldrepenger.mottak.klient.DokumentmottakRestKlient;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.klient.TilbakekrevingRestKlient;

@ApplicationScoped
public class KlargjørForVLTjeneste {

    private final String TILBAKE = "fpfordel.tilbake.sendjpost";

    private static final Logger log = LoggerFactory.getLogger(KlargjørForVLTjeneste.class);

    private DokumentmottakRestKlient restKlient;
    private FagsakRestKlient fagsakRestKlient;
    private TilbakekrevingRestKlient tilbakekrevingRestKlient;

    @Inject
    public KlargjørForVLTjeneste(DokumentmottakRestKlient restKlient, FagsakRestKlient fagsakRestKlient,
            TilbakekrevingRestKlient tilbakekrevingRestKlient) {
        this.restKlient = restKlient;
        this.fagsakRestKlient = fagsakRestKlient;
        this.tilbakekrevingRestKlient = tilbakekrevingRestKlient;
    }

    public KlargjørForVLTjeneste() {
    }

    public void klargjørForVL(String xml, String saksnummer, String arkivId, DokumentTypeId dokumenttypeId,
            LocalDateTime forsendelseMottatt,
            BehandlingTema behandlingsTema, UUID forsendelseId, DokumentKategori dokumentKategori,
            String journalFørendeEnhet) {
        String behandlingTemaString = behandlingsTema == null || BehandlingTema.UDEFINERT.equals(behandlingsTema)
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

        JournalpostMottakDto journalpostMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString,
                dokumentTypeIdOffisiellKode, forsendelseMottatt, xml);
        journalpostMottakDto.setForsendelseId(forsendelseId);
        journalpostMottakDto.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
        journalpostMottakDto.setJournalForendeEnhet(journalFørendeEnhet);
        restKlient.send(journalpostMottakDto);

        try {
            JournalpostMottakDto tilbakeMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString,
                    dokumentTypeIdOffisiellKode, forsendelseMottatt, null);
            tilbakeMottakDto.setForsendelseId(forsendelseId);
            tilbakeMottakDto.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
            tilbakeMottakDto.setJournalForendeEnhet(journalFørendeEnhet);
            tilbakekrevingRestKlient.send(tilbakeMottakDto);
        } catch (Exception e) {
            log.warn("Feil ved sending av forsendelse til fptilbake, ukjent feil", e);
        }
    }

}
