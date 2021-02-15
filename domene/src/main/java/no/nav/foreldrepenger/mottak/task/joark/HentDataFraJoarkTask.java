package no.nav.foreldrepenger.mottak.task.joark;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
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
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
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

    private final PersonInformasjon aktørConsumer;
    private final ArkivTjeneste arkivTjeneste;

    @Inject
    public HentDataFraJoarkTask(ProsessTaskRepository prosessTaskRepository,
            PersonInformasjon aktørConsumer,
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

        var journalpost = arkivTjeneste.hentArkivJournalpost(dataWrapper.getArkivId());

        if (!Journalstatus.MOTTATT.equals(journalpost.getTilstand())) {
            LOG.info("FPFORDEL HentFraArkiv feil tilstand på journalpost {} med {}", journalpost.getJournalpostId(), journalpost.getTilstand());
            return null;
        }

        // Disse 2 + behandlingstema er normalt satt fra før
        dataWrapper.setTema(journalpost.getTema());

        if (dataWrapper.getEksternReferanseId().isEmpty()) {
            dataWrapper.setEksternReferanseId(journalpost.getEksternReferanseId());
        }

        dataWrapper.setForsendelseMottattTidspunkt(journalpost.getDatoOpprettet());
        dataWrapper.setDokumentTypeId(journalpost.getHovedtype());
        dataWrapper.setBehandlingTema(ArkivUtil.behandlingTemaFraDokumentType(dataWrapper.getBehandlingTema(), journalpost.getHovedtype()));
        dataWrapper.setDokumentKategori(ArkivUtil.utledKategoriFraDokumentType(journalpost.getHovedtype()));
        journalpost.getBrukerAktørId().ifPresent(dataWrapper::setAktørId);
        journalpost.getJournalfoerendeEnhet().ifPresent(dataWrapper::setJournalførendeEnhet);
        dataWrapper.setStrukturertDokument(journalpost.getInnholderStrukturertInformasjon());
        journalpost.getSaksnummer().ifPresent(s -> {
            LOG.info("FORDEL HentFraArkiv presatt saksnummer {} for journalpost {}", s, dataWrapper.getArkivId());
            dataWrapper.setSaksnummer(s);
            dataWrapper.setInnkommendeSaksnummer(s);
        });

        if (journalpost.getInnholderStrukturertInformasjon()) {
            if (!MeldingXmlParser.erXmlMedKjentNamespace(journalpost.getStrukturertPayload())) {
                var jptittel = journalpost.getOriginalJournalpost().tittel();
                // kast feil for ukjent innhold som antagelig er XML (og vi kanskje bør
                // håndtere). ignorer andre
                if (!journalpost.getStrukturertPayload().isBlank() &&
                        Objects.equals('<', journalpost.getStrukturertPayload().trim().charAt(0))) {
                    var doktittel = journalpost.getOriginalJournalpost().dokumenter().get(0).tittel();
                    var prefix = journalpost.getStrukturertPayload().substring(0, Math.min(40, journalpost.getStrukturertPayload().length()));
                    LOG.warn("Journalpost med ukjent strukturert innhold {} {} {}", jptittel, doktittel, prefix);
                    throw new IllegalStateException("Ukjent type strukturert dokument");
                }
                LOG.info("FPFORDEL journalpost med non-xml strukturert innhold {}", jptittel);
            } else {
                MottattStrukturertDokument<?> mottattDokument = MeldingXmlParser.unmarshallXml(journalpost.getStrukturertPayload());
                mottattDokument.kopierTilMottakWrapper(dataWrapper, aktørConsumer::hentAktørIdForPersonIdent);
                dataWrapper.setPayload(journalpost.getStrukturertPayload());
            }
        }
        if (dataWrapper.getForsendelseMottattTidspunkt().isEmpty()) {
            dataWrapper.setForsendelseMottattTidspunkt(LocalDateTime.now());
        }

        // Journalposter uten kanalreferanse er "klonet" av SBH og forsøkt journalført
        // fra Gosys. Håndteres manuelt hvis de kommer helt hit
        if (dataWrapper.getEksternReferanseId().isEmpty()) {
            LOG.info("FPFORDEL HentFraArkiv journalpost uten kanalreferane {} med {}", journalpost.getJournalpostId(), journalpost.getTilstand());
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }
        // Vesentlige mangler
        if (!Tema.FORELDRE_OG_SVANGERSKAPSPENGER.equals(dataWrapper.getTema())) {
            LOG.info("FPFORDEL HentFraArkiv feil tema for journalpost {} tema {}",
                    dataWrapper.getArkivId(), journalpost.getTema().getKode());
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }
        if (dataWrapper.getAktørId().isEmpty()) {
            var avsender = journalpost.getAvsenderIdent() == null ? "ikke satt"
                    : aktørConsumer.hentAktørIdForPersonIdent(journalpost.getAvsenderIdent()).orElse("finnes ikke");
            LOG.info("FPFORDEL HentFraArkiv manglende bruker for journalpost {} type {} avsender {}",
                    dataWrapper.getArkivId(), journalpost.getHovedtype(), avsender);
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }
        if (DokumentTypeId.UDEFINERT.equals(journalpost.getHovedtype())) {
            LOG.info("FPFORDEL HentFraArkiv udefinert dokumenttype journalpost {} tittel {}",
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