package no.nav.foreldrepenger.mottak.journal;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.dok.tjenester.mottainngaaendeforsendelse.ForsendelseInformasjon;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRequest;
import no.nav.dok.tjenester.mottainngaaendeforsendelse.MottaInngaaendeForsendelseResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.kodeverdi.VariantFormat;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringFerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostOppdateringIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.ArkivSak;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Avsender;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Dokumentinformasjon;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.JournalpostMangler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovResponse;
import no.nav.tjeneste.virksomhet.journal.v2.binding.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v2.binding.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v2.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.journal.v2.meldinger.HentDokumentResponse;
import no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal.BehandleInngaaendeJournalConsumer;
import no.nav.vedtak.felles.integrasjon.inngaaendejournal.InngaaendeJournalConsumer;
import no.nav.vedtak.felles.integrasjon.journal.v2.JournalConsumer;
import no.nav.vedtak.felles.integrasjon.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRestKlient;

@Dependent
public class JournalTjeneste {

    private static final String AKTØR_ID_KEY = "aktoer";
    private static final String IDENT_KEY = "ident";
    private static final String PERSON_KEY = "person";
    private static final String MOTTAK_KANAL_NAV_NO = MottakKanal.SELVBETJENING.getKode();

    private final JournalConsumer journalConsumer;
    private final InngaaendeJournalConsumer inngaaendeJournalConsumer;
    private final BehandleInngaaendeJournalConsumer behandleInngaaendeJournalConsumer;
    private final MottaInngaaendeForsendelseRestKlient mottaInngaaendeForsendelseKlient;


    private JournalTjenesteUtil journalTjenesteUtil;

    @Inject
    public JournalTjeneste(JournalConsumer journalConsumer,
            InngaaendeJournalConsumer inngaaendeJournalConsumer,
            BehandleInngaaendeJournalConsumer behandleInngaaendeJournalConsumer,
            MottaInngaaendeForsendelseRestKlient mottaInngaaendeForsendelseKlient) {
        this.journalConsumer = journalConsumer;
        this.inngaaendeJournalConsumer = inngaaendeJournalConsumer;
        this.behandleInngaaendeJournalConsumer = behandleInngaaendeJournalConsumer;
        this.mottaInngaaendeForsendelseKlient = mottaInngaaendeForsendelseKlient;
        this.journalTjenesteUtil = new JournalTjenesteUtil();
    }

    public JournalDokument hentDokument(JournalMetadata journalMetadata) {
        if (journalMetadata == null) {
            throw new IllegalArgumentException("Inputdata er ikke satt");
        }
        HentDokumentRequest journalConsumerRequest = new HentDokumentRequest();
        journalConsumerRequest.setDokumentId(journalMetadata.getDokumentId());
        journalConsumerRequest.setJournalpostId(journalMetadata.getJournalpostId());
        no.nav.tjeneste.virksomhet.journal.v2.informasjon.Variantformater variantformater = new no.nav.tjeneste.virksomhet.journal.v2.informasjon.Variantformater();
        String variantFormatOffisiellKode = journalMetadata.getVariantFormat() == null
                || VariantFormat.UDEFINERT.equals(journalMetadata.getVariantFormat())
                        ? null
                        : journalMetadata.getVariantFormat().getKode();
        variantformater.setValue(variantFormatOffisiellKode);
        journalConsumerRequest.setVariantformat(variantformater);

        byte[] response;
        try {
            HentDokumentResponse hentDokumentResponse = journalConsumer.hentDokument(journalConsumerRequest);
            response = hentDokumentResponse.getDokument();
        } catch (HentDokumentDokumentIkkeFunnet e) {
            throw JournalFeil.FACTORY.hentDokumentIkkeFunnet(e).toException();
        } catch (HentDokumentSikkerhetsbegrensning e) {
            throw JournalFeil.FACTORY.journalUtilgjengeligSikkerhetsbegrensning("Hent dokument", e).toException();
        }
        return new JournalDokument(journalMetadata, new String(response, StandardCharsets.UTF_8));
    }

    public List<JournalMetadata> hentMetadata(String journalpostId) {
        HentJournalpostRequest request = new HentJournalpostRequest();
        request.setJournalpostId(journalpostId);

        HentJournalpostResponse response;
        try {
            response = inngaaendeJournalConsumer.hentJournalpost(request);
        } catch (HentJournalpostJournalpostIkkeFunnet e) {
            throw JournalFeil.FACTORY.hentJournalpostIkkeFunnet(e).toException();
        } catch (HentJournalpostSikkerhetsbegrensning e) {
            throw JournalFeil.FACTORY.journalUtilgjengeligSikkerhetsbegrensning("Hent metadata", e).toException();
        } catch (HentJournalpostUgyldigInput e) {
            throw JournalFeil.FACTORY.journalpostUgyldigInput(e).toException();
        } catch (HentJournalpostJournalpostIkkeInngaaende e) {
            throw JournalFeil.FACTORY.journalpostIkkeInngaaende(e).toException();
        }

        return journalTjenesteUtil.konverterTilMetadata(journalpostId, response);
    }

    public JournalPostMangler utledJournalføringsbehov(String journalpostId) {
        UtledJournalfoeringsbehovRequest request = new UtledJournalfoeringsbehovRequest();
        request.setJournalpostId(journalpostId);
        try {
            UtledJournalfoeringsbehovResponse utledJournalfoeringsbehovResponse = inngaaendeJournalConsumer
                    .utledJournalfoeringsbehov(request);
            JournalpostMangler journalfoeringsbehov = utledJournalfoeringsbehovResponse.getJournalfoeringsbehov();
            return journalTjenesteUtil.konverterTilJournalmangler(journalfoeringsbehov);
        } catch (UtledJournalfoeringsbehovSikkerhetsbegrensning e) {
            throw JournalFeil.FACTORY.journalUtilgjengeligSikkerhetsbegrensning("Utled journalføringsbehov", e)
                    .toException();
        } catch (UtledJournalfoeringsbehovUgyldigInput e) {
            throw JournalFeil.FACTORY.utledJournalfoeringsbehovUgyldigInput(e).toException();
        } catch (UtledJournalfoeringsbehovJournalpostKanIkkeBehandles e) {
            throw JournalFeil.FACTORY.utledJournalfoeringsbehovJournalpostKanIkkeBehandles(e).toException();
        } catch (UtledJournalfoeringsbehovJournalpostIkkeFunnet e) {
            throw JournalFeil.FACTORY.utledJournalfoeringsbehovJournalpostIkkeFunnet(e).toException();
        } catch (UtledJournalfoeringsbehovJournalpostIkkeInngaaende e) {
            throw JournalFeil.FACTORY.utledJournalfoeringsbehovJournalpostIkkeInngaaende(e).toException();
        }
    }

    public void ferdigstillJournalføring(String journalpostId, String enhetId) {
        FerdigstillJournalfoeringRequest request = new FerdigstillJournalfoeringRequest();
        request.setJournalpostId(journalpostId);
        request.setEnhetId(enhetId);

        try {
            behandleInngaaendeJournalConsumer.ferdigstillJournalfoering(request);
        } catch (FerdigstillJournalfoeringFerdigstillingIkkeMulig e) {
            throw JournalFeil.FACTORY.journalfoeringFerdigstillingIkkeMulig(e).toException();
        } catch (FerdigstillJournalfoeringJournalpostIkkeInngaaende e) {
            throw JournalFeil.FACTORY.ferdigstillJournalfoeringJournalpostIkkeInngaaende(e).toException();
        } catch (FerdigstillJournalfoeringUgyldigInput e) {
            throw JournalFeil.FACTORY.ferdigstillJournalfoeringUgyldigInput(e).toException();
        } catch (FerdigstillJournalfoeringSikkerhetsbegrensning e) {
            throw JournalFeil.FACTORY.ferdigstillJournalfoeringSikkerhetsbegrensning(e).toException();
        } catch (FerdigstillJournalfoeringObjektIkkeFunnet e) {
            throw JournalFeil.FACTORY.ferdigstillJournalfoeringObjektIkkeFunnet(e).toException();
        }
    }

    public void oppdaterJournalpost(JournalPost journalPost) {
        if (journalPost == null) {
            throw new IllegalArgumentException("Journalpost er null");
        }

        OppdaterJournalpostRequest request = new OppdaterJournalpostRequest();
        InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
        inngaaendeJournalpost.setJournalpostId(journalPost.getJournalpostId());

        if (journalPost.getArkivSakId() != null) {
            ArkivSak arkivSak = new ArkivSak();
            arkivSak.setArkivSakId(journalPost.getArkivSakId());
            if (journalPost.getArkivSakSystem().isPresent()) {
                arkivSak.setArkivSakSystem(journalPost.getArkivSakSystem().get()); // NOSONAR
            }
            inngaaendeJournalpost.setArkivSak(arkivSak);
        }

        if (journalPost.getFnr() != null) {
            Person person = new Person();
            person.setIdent(journalPost.getFnr());
            inngaaendeJournalpost.setBruker(person);
        }

        if (journalPost.getAvsenderFnr() != null || journalPost.getAvsenderNavn() != null) {
            Avsender avsender = new Avsender();
            avsender.setAvsenderId(journalPost.getAvsenderFnr());
            avsender.setAvsenderNavn(journalPost.getAvsenderFnr() != null ? journalPost.getAvsenderFnr() : "NN");
            inngaaendeJournalpost.setAvsender(avsender);
        }

        if (journalPost.getHovedDokumentTittel() != null) {
            Dokumentinformasjon info = new Dokumentinformasjon();
            info.setDokumentId(journalPost.getHovedDokumentId());
            info.setTittel(journalPost.getHovedDokumentTittel());
            inngaaendeJournalpost.setHoveddokument(info);
        }

        inngaaendeJournalpost.setInnhold(journalPost.getInnhold());

        request.setInngaaendeJournalpost(inngaaendeJournalpost);

        try {
            behandleInngaaendeJournalConsumer.oppdaterJournalpost(request);
        } catch (OppdaterJournalpostSikkerhetsbegrensning e) {
            throw JournalFeil.FACTORY.journalUtilgjengeligSikkerhetsbegrensning("Oppdater journalpost", e)
                    .toException();
        } catch (OppdaterJournalpostOppdateringIkkeMulig e) {
            throw JournalFeil.FACTORY.oppdaterJournalpostOppdateringIkkeMulig(e).toException();
        } catch (OppdaterJournalpostUgyldigInput e) {
            throw JournalFeil.FACTORY.oppdaterJournalpostUgyldigInput(e).toException();
        } catch (OppdaterJournalpostJournalpostIkkeInngaaende e) {
            throw JournalFeil.FACTORY.oppdaterJournalpostJournalpostIkkeInngaaende(e).toException();
        } catch (OppdaterJournalpostObjektIkkeFunnet e) {
            throw JournalFeil.FACTORY.oppdaterJournalpostObjektIkkeFunnet(e).toException();
        }
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
