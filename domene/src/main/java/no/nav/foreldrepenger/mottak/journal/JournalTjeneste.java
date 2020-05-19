package no.nav.foreldrepenger.mottak.journal;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.dok.tjenester.mottainngaaendeforsendelse.ForsendelseInformasjon;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRequest;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.vedtak.felles.integrasjon.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRestKlient;

@Dependent
public class JournalTjeneste {

    private static final String AKTØR_ID_KEY = "aktoer";
    private static final String IDENT_KEY = "ident";
    private static final String PERSON_KEY = "person";
    private static final String MOTTAK_KANAL_NAV_NO = MottakKanal.SELVBETJENING.getKode();

    private final MottaInngaaendeForsendelseRestKlient mottaInngaaendeForsendelseKlient;


    private JournalTjenesteUtil journalTjenesteUtil;

    @Inject
    public JournalTjeneste(MottaInngaaendeForsendelseRestKlient mottaInngaaendeForsendelseKlient) {
        this.mottaInngaaendeForsendelseKlient = mottaInngaaendeForsendelseKlient;
        this.journalTjenesteUtil = new JournalTjenesteUtil();
    }

    public DokumentforsendelseResponse journalførDokumentforsendelse(
            DokumentforsendelseRequest dokumentforsendelseRequest) {
        String forsendelseId = dokumentforsendelseRequest.getForsendelseId();
        MottaInngaaendeForsendelseRequest request = new MottaInngaaendeForsendelseRequest();
        request.setForsokEndeligJF(dokumentforsendelseRequest.getForsøkEndeligJF());

        LocalDateTime ldt = dokumentforsendelseRequest.getForsendelseMottatt();
        Date mottatt = Date.from(ldt.toInstant(ZoneOffset.UTC));

        ForsendelseInformasjon forsendelseInformasjon = new ForsendelseInformasjon();
        no.nav.dok.tjenester.mottainngaaendeforsendelse.Aktoer bruker = new no.nav.dok.tjenester.mottainngaaendeforsendelse.Aktoer();
        bruker.setAdditionalProperty(AKTØR_ID_KEY, lagAktørStruktur(dokumentforsendelseRequest.getBruker()));
        forsendelseInformasjon.setBruker(bruker);

        no.nav.dok.tjenester.mottainngaaendeforsendelse.Aktoer avsender = new no.nav.dok.tjenester.mottainngaaendeforsendelse.Aktoer();
        avsender.setAdditionalProperty(AKTØR_ID_KEY, lagAktørStruktur(
                dokumentforsendelseRequest.getAvsender().orElse(dokumentforsendelseRequest.getBruker())));
        forsendelseInformasjon.setAvsender(avsender);

        forsendelseInformasjon.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode());
        if (dokumentforsendelseRequest.getRetrySuffix().isPresent()) {
            forsendelseInformasjon
                    .setKanalReferanseId(forsendelseId + "-" + dokumentforsendelseRequest.getRetrySuffix().get());
        } else {
            forsendelseInformasjon.setKanalReferanseId(forsendelseId);
        }
        forsendelseInformasjon.setForsendelseMottatt(mottatt);
        forsendelseInformasjon.setForsendelseInnsendt(mottatt);
        forsendelseInformasjon.setMottaksKanal(MOTTAK_KANAL_NAV_NO);
        forsendelseInformasjon.setTittel(dokumentforsendelseRequest.getTittel());
        if (forsendelseInformasjon.getTittel() == null && !dokumentforsendelseRequest.getHoveddokument().isEmpty()) {
            forsendelseInformasjon.setTittel(
                    journalTjenesteUtil.tittelFraDokument(dokumentforsendelseRequest.getHoveddokument().get(0),
                            dokumentforsendelseRequest.getForsøkEndeligJF(), true));
        } else {
            JournalFeil.FACTORY.kunneIkkeUtledeForsendelseTittel(forsendelseId);
        }

        String saksnummer = dokumentforsendelseRequest.getSaksnummer();
        if (saksnummer != null) {
            no.nav.dok.tjenester.mottainngaaendeforsendelse.ArkivSak arkivSak = new no.nav.dok.tjenester.mottainngaaendeforsendelse.ArkivSak();
            arkivSak.setArkivSakSystem(no.nav.dok.tjenester.mottainngaaendeforsendelse.ArkivSak.ArkivSakSystem
                    .fromValue(Fagsystem.GOSYS.getKode()));
            arkivSak.setArkivSakId(saksnummer);
            forsendelseInformasjon.setArkivSak(arkivSak);
        }

        request.setForsendelseInformasjon(forsendelseInformasjon);

        if (!dokumentforsendelseRequest.getHoveddokument().isEmpty()) {
            request.setDokumentInfoHoveddokument(journalTjenesteUtil
                    .konverterTilDokumentInfoHoveddokument(dokumentforsendelseRequest.getHoveddokument()));
        }

        Boolean harHoveddokument = !dokumentforsendelseRequest.getHoveddokument().isEmpty();
        request.setDokumentInfoVedlegg(journalTjenesteUtil.konverterTilDokumentInfoVedlegg(
                dokumentforsendelseRequest.getVedlegg(), harHoveddokument,
                dokumentforsendelseRequest.getForsøkEndeligJF()));

        MottaInngaaendeForsendelseResponse response = mottaInngaaendeForsendelseKlient.journalførForsendelse(request);
        return journalTjenesteUtil.konverterTilDokumentforsendelseResponse(response);
    }

    private Map<String, Map<String, String>> lagAktørStruktur(String aktørId) {
        Map<String, String> identStrukt = new HashMap<>();
        identStrukt.put(IDENT_KEY, aktørId);
        Map<String, Map<String, String>> struktur = new HashMap<>();
        struktur.put(PERSON_KEY, identStrukt);
        return struktur;
    }
}
