package no.nav.foreldrepenger.mottak.tjeneste;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.finn.unleash.Unleash;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
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
    private Unleash unleash;

    @Inject
    public KlargjørForVLTjeneste(DokumentmottakRestKlient restKlient, FagsakRestKlient fagsakRestKlient,
            TilbakekrevingRestKlient tilbakekrevingRestKlient, Unleash unleash) {
        this.restKlient = restKlient;
        this.fagsakRestKlient = fagsakRestKlient;
        this.tilbakekrevingRestKlient = tilbakekrevingRestKlient;
        this.unleash = unleash;
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

        if (unleash != null && unleash.isEnabled(TILBAKE, false)) {
            JournalpostMottakDto tilbakeMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString,
                    dokumentTypeIdOffisiellKode, forsendelseMottatt, null);
            tilbakeMottakDto.setForsendelseId(forsendelseId);
            tilbakeMottakDto.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
            tilbakeMottakDto.setJournalForendeEnhet(journalFørendeEnhet);
            tilbakekrevingRestKlient.send(tilbakeMottakDto);
        }
    }

}
