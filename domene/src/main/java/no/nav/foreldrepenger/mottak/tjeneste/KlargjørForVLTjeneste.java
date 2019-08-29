package no.nav.foreldrepenger.mottak.tjeneste;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.foreldrepenger.mottak.klient.DokumentmottakRestKlient;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;

@ApplicationScoped
public class KlargjørForVLTjeneste {


    private DokumentmottakRestKlient restKlient;
    private FagsakRestKlient fagsakRestKlient;

    @Inject
    public KlargjørForVLTjeneste(DokumentmottakRestKlient restKlient, FagsakRestKlient fagsakRestKlient) {
        this.restKlient = restKlient;
        this.fagsakRestKlient = fagsakRestKlient;
    }

    public KlargjørForVLTjeneste() {
        //NOSONAR: gjett hvorfor
    }

    public void klargjørForVL(String xml, String saksnummer, String arkivId, DokumentTypeId dokumenttypeId, LocalDateTime forsendelseMottatt,
                              BehandlingTema behandlingsTema, UUID forsendelseId, DokumentKategori dokumentKategori, String journalFørendeEnhet) {
        String behandlingTemaString = behandlingsTema == null || BehandlingTema.UDEFINERT.equals(behandlingsTema) ? BehandlingTema.UDEFINERT.getKode() : behandlingsTema.getOffisiellKode();
        String dokumentTypeIdOffisiellKode = null;
        String dokumentKategoriOffisiellKode = null;
        if (dokumenttypeId != null) {
            dokumentTypeIdOffisiellKode = dokumenttypeId.getOffisiellKode();
        }
        if (dokumentKategori != null) {
            dokumentKategoriOffisiellKode = dokumentKategori.getOffisiellKode();
        }
        fagsakRestKlient.knyttSakOgJournalpost(new JournalpostKnyttningDto(saksnummer, arkivId));

        JournalpostMottakDto journalpostMottakDto = new JournalpostMottakDto(saksnummer, arkivId, behandlingTemaString, dokumentTypeIdOffisiellKode, forsendelseMottatt, xml);
        journalpostMottakDto.setForsendelseId(forsendelseId);
        journalpostMottakDto.setDokumentKategoriOffisiellKode(dokumentKategoriOffisiellKode);
        journalpostMottakDto.setJournalForendeEnhet(journalFørendeEnhet);
        restKlient.send(journalpostMottakDto);
    }

}
