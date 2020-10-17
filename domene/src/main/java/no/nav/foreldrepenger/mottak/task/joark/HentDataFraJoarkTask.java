package no.nav.foreldrepenger.mottak.task.joark;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.AktørTjeneste;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.StringUtils;

/**
 * <p>
 * ProssessTask som håndterer uthenting av saksinformasjon fra
 * Journalarkivet(joark).
 * </p>
 */
@Dependent
@ProsessTask(HentDataFraJoarkTask.TASKNAME)
public class HentDataFraJoarkTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.hentFraJoark";

    private static final Logger LOG = LoggerFactory.getLogger(HentDataFraJoarkTask.class);

    private final AktørTjeneste aktørConsumer;
    private final ArkivTjeneste arkivTjeneste;

    @Inject
    public HentDataFraJoarkTask(ProsessTaskRepository prosessTaskRepository,
                                AktørTjeneste aktørConsumer,
            ArkivTjeneste arkivTjeneste) {
        super(prosessTaskRepository);
        this.aktørConsumer = aktørConsumer;
        this.arkivTjeneste = arkivTjeneste;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (StringUtils.nullOrEmpty(dataWrapper.getArkivId())) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.ARKIV_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper dataWrapper) {
        if (!OpprettGSakOppgaveTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())
                && dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {

        ArkivJournalpost journalpost = arkivTjeneste.hentArkivJournalpost(dataWrapper.getArkivId());

        if (!Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            LOG.info("FPFORDEL HERK feil tilstand på journalpost {} med {}", journalpost.getJournalpostId(), journalpost.getTilstand());
            return null;
        }

        // Disse 2 + behandlingstema er normalt satt fra før
        dataWrapper.setTema(journalpost.getTema());
        dataWrapper.setEksternReferanseId(journalpost.getEksternReferanseId());

        dataWrapper.setForsendelseMottattTidspunkt(journalpost.getDatoOpprettet());
        dataWrapper.setDokumentTypeId(journalpost.getHovedtype());
        dataWrapper.setBehandlingTema(ArkivUtil.behandlingTemaFraDokumentType(dataWrapper.getBehandlingTema(), journalpost.getHovedtype()));
        dataWrapper.setDokumentKategori(ArkivUtil.utledKategoriFraDokumentType(journalpost.getHovedtype()));
        journalpost.getBrukerAktørId().ifPresent(dataWrapper::setAktørId);
        journalpost.getJournalfoerendeEnhet().ifPresent(dataWrapper::setJournalførendeEnhet);
        dataWrapper.setStrukturertDokument(journalpost.getInnholderStrukturertInformasjon());
        journalpost.getSaksnummer().ifPresent(s -> {
            dataWrapper.setSaksnummer(s);
            dataWrapper.setInnkommendeSaksnummer(s);
        });

        if (journalpost.getInnholderStrukturertInformasjon()) {
            MottattStrukturertDokument<?> mottattDokument = MeldingXmlParser.unmarshallXml(journalpost.getStrukturertPayload());
            mottattDokument.kopierTilMottakWrapper(dataWrapper, aktørConsumer::hentAktørIdForPersonIdent);
            dataWrapper.setPayload(journalpost.getStrukturertPayload());
        }
        if (dataWrapper.getForsendelseMottattTidspunkt().isEmpty()) {
            dataWrapper.setForsendelseMottattTidspunkt(LocalDateTime.now());
        }

        // Vesentlige mangler
        if (!Tema.FORELDRE_OG_SVANGERSKAPSPENGER.equals(dataWrapper.getTema())) {
            LOG.warn("FPFORDEL HERK feil tema for journalpost {} tema {}",
                    dataWrapper.getArkivId(), journalpost.getTema().getKode());
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }
        if (dataWrapper.getAktørId().isEmpty()) {
            var avsender = journalpost.getAvsenderIdent() == null ? "ikke satt"
                    : aktørConsumer.hentAktørIdForPersonIdent(journalpost.getAvsenderIdent()).orElse("finnes ikke");
            LOG.info("FPFORDEL HERK manglende bruker for journalpost {} type {} avsender {}",
                    dataWrapper.getArkivId(), journalpost.getHovedtype(), avsender);
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }
        if (DokumentTypeId.UDEFINERT.equals(journalpost.getHovedtype())) {
            LOG.info("FPFORDEL HERK udefinert dokumenttype journalpost {} tittel {}",
                    dataWrapper.getArkivId(), journalpost.getTittel());
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }

        LOG.info("FPFORDEL INNGÅENDE journalpost {} kanal {} tilstand {} hovedtype {} alle typer {}",
                dataWrapper.getArkivId(), journalpost.getKanal(), journalpost.getTilstand(),
                journalpost.getHovedtype(), journalpost.getAlleTyper());

        if (DokumentTypeId.erInntektsmelding(journalpost.getHovedtype())) {
            return håndterInntektsmelding(dataWrapper);
        }

        if (!arkivTjeneste.oppdaterRettMangler(journalpost, dataWrapper.getAktørId().get(), dataWrapper.getBehandlingTema(),
                dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))) {
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }

        return dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME);
    }

    private MottakMeldingDataWrapper håndterInntektsmelding(MottakMeldingDataWrapper dataWrapper) {
        Optional<String> imYtelse = dataWrapper.getInntektsmeldingYtelse();
        if (imYtelse.isEmpty()) {
            throw MottakMeldingFeil.FACTORY.manglerYtelsePåInntektsmelding().toException();
        }
        BehandlingTema behandlingTemaFraIM = BehandlingTema.fraTermNavn(imYtelse.get());

        // Mangler bruker
        arkivTjeneste.oppdaterBehandlingstemaBruker(dataWrapper.getArkivId(), behandlingTemaFraIM.getOffisiellKode(),
                dataWrapper.getAktørId().orElseThrow(() -> new IllegalStateException("Utviklerfeil: aktørid skal være satt")));

        dataWrapper.setBehandlingTema(behandlingTemaFraIM);

        if (BehandlingTema.gjelderForeldrepenger(behandlingTemaFraIM)) {
            return kreverStartdatoForInntektsmeldingenManuellBehandling(dataWrapper)
                    ? dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME)
                    : dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME);
        } else if (BehandlingTema.gjelderSvangerskapspenger(behandlingTemaFraIM)) {
            return sjekkOmInntektsmeldingGjelderMann(dataWrapper)
                    ? dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME)
                    : dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME);
        } else {
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }
    }

    private boolean sjekkOmInntektsmeldingGjelderMann(MottakMeldingDataWrapper dataWrapper) {
        String aktørId = dataWrapper.getAktørId().orElseThrow(() -> new IllegalStateException("Utviklerfeil"));
        String fnrBruker = aktørConsumer.hentPersonIdentForAktørId(aktørId)
                .orElseThrow(() -> MottakMeldingFeil.FACTORY
                        .fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()).toException());
        return (Character.getNumericValue(fnrBruker.charAt(8)) % 2) != 0;
    }

    private static boolean kreverStartdatoForInntektsmeldingenManuellBehandling(MottakMeldingDataWrapper dataWrapper) {
        LocalDate startDato = dataWrapper.getInntektsmeldingStartDato().orElse(Tid.TIDENES_BEGYNNELSE);
        return startDato.isBefore(KonfigVerdier.ENDRING_BEREGNING_DATO);
    }

}