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
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.foreldrepenger.mottak.tjeneste.VurderVLSaker;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.Tid;

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
    private final VurderVLSaker vurderVLSaker;

    @Inject
    public HentDataFraJoarkTask(ProsessTaskRepository prosessTaskRepository,
            VurderVLSaker vurderVLSaker,
            PersonInformasjon aktørConsumer,
            ArkivTjeneste arkivTjeneste) {
        super(prosessTaskRepository);
        this.vurderVLSaker = vurderVLSaker;
        this.aktørConsumer = aktørConsumer;
        this.arkivTjeneste = arkivTjeneste;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getArkivId() == null || dataWrapper.getArkivId().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.ARKIV_ID_KEY, dataWrapper.getId());
        }
    }

    @Override
    public void postcondition(MottakMeldingDataWrapper dataWrapper) {
        if (!OpprettGSakOppgaveTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())
                && dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId());
        }
        if (TilJournalføringTask.TASKNAME.equals(dataWrapper.getProsessTaskData().getTaskType())
                && dataWrapper.getSaksnummer().isEmpty()) {
            throw MottakMeldingFeil.prosesstaskPostconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId());
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
        finnAktørId(journalpost).ifPresent(dataWrapper::setAktørId);
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
                    LOG.warn("FPFORDEL journalpost med ukjent xml innhold {} {} {}", jptittel, doktittel, prefix);
                } else {
                    LOG.info("FPFORDEL journalpost med non-xml strukturert innhold {}", jptittel);
                }
            } else {
                try {
                    MottattStrukturertDokument<?> mottattDokument = MeldingXmlParser.unmarshallXml(journalpost.getStrukturertPayload());
                    mottattDokument.kopierTilMottakWrapper(dataWrapper, aktørConsumer::hentAktørIdForPersonIdent);
                    dataWrapper.setPayload(journalpost.getStrukturertPayload());
                } catch (VLException vle) {
                    // Mottatt journalpost har annet saksnummer enn den i endringssøknaden....
                    // Skyldes spesiell bruk av Gosys. Lag oppgave i dette tilfelle, godta i BehandleDokumentService
                    if (MottakMeldingFeil.ENDRINGSSØKNAD_AVVIK_SAKSNUMMER.equals(vle.getKode())) {
                        dataWrapper.setSaksnummer(null);
                        return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
                    }
                }
            }
        }
        if (dataWrapper.getForsendelseMottattTidspunkt().isEmpty()) {
            dataWrapper.setForsendelseMottattTidspunkt(LocalDateTime.now());
        }

        // Journalposter uten kanalreferanse er vanligvis slike som er "klonet" av SBH og forsøkt journalført fra Gosys.
        // Håndteres manuelt hvis de kommer helt hit - mulig de skal slippes videre
        if (dataWrapper.getEksternReferanseId().isEmpty() && dataWrapper.getInnkommendeSaksnummer().isEmpty()
                && !MottakKanal.SELVBETJENING.getKode().equals(journalpost.getKanal())) {
            LOG.info("FPFORDEL HentFraArkiv journalpost uten kanalreferanse journalpost {} kanal {} dokumenttype {}",
                    journalpost.getJournalpostId(), journalpost.getKanal(), journalpost.getHovedtype());
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
            oppdaterInntektsmelding(dataWrapper);
            if (kreverInntektsmeldingManuellVurdering(dataWrapper)) {
                return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
            }
        } else if (!arkivTjeneste.oppdaterRettMangler(journalpost, dataWrapper.getAktørId().get(), dataWrapper.getBehandlingTema(),
                dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT))) {
            LOG.info("FPFORDEL HentFraArkiv kunne ikke rette opp mangler journalpost {} kanal {} hovedtype {} alle typer {}",
                    dataWrapper.getArkivId(), journalpost.getKanal(), journalpost.getHovedtype(), journalpost.getAlleTyper());
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }

        var destinasjon = vurderVLSaker.bestemDestinasjon(dataWrapper);
        LOG.info("FPFORDEL HentFraArkiv destinasjon {} journalpost {} kanal {} dokumenttype {} saksnummer {}",
                destinasjon, dataWrapper.getArkivId(), journalpost.getKanal(), journalpost.getHovedtype(), journalpost.getSaksnummer());
        if (ForsendelseStatus.GOSYS.equals(destinasjon.system())) {
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        } else {
            if (destinasjon.saksnummer() == null && !vurderVLSaker.kanOppretteSak(dataWrapper)) {
                return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
            }
            var saksnummer = Optional.ofNullable(destinasjon.saksnummer())
                    .orElseGet(() -> vurderVLSaker.opprettSak(dataWrapper));
            dataWrapper.setSaksnummer(saksnummer);
            return dataWrapper.nesteSteg(TilJournalføringTask.TASKNAME);
        }
    }

    private void oppdaterInntektsmelding(MottakMeldingDataWrapper dataWrapper) {
        Optional<String> imYtelse = dataWrapper.getInntektsmeldingYtelse();
        if (imYtelse.isEmpty()) {
            throw MottakMeldingFeil.manglerYtelsePåInntektsmelding();
        }
        BehandlingTema behandlingTemaFraIM = BehandlingTema.fraTermNavn(imYtelse.get());

        // Mangler alltid bruker
        arkivTjeneste.oppdaterBehandlingstemaBruker(dataWrapper.getArkivId(), DokumentTypeId.INNTEKTSMELDING,
                behandlingTemaFraIM.getOffisiellKode(), dataWrapper.getAktørId().orElseThrow(() -> new IllegalStateException("Utviklerfeil: aktørid skal være satt")));

        dataWrapper.setBehandlingTema(behandlingTemaFraIM);
    }

    private boolean kreverInntektsmeldingManuellVurdering(MottakMeldingDataWrapper dataWrapper) {
        if (BehandlingTema.gjelderForeldrepenger(dataWrapper.getBehandlingTema())) {
            return kreverStartdatoForInntektsmeldingenManuellBehandling(dataWrapper);
        } else if (BehandlingTema.gjelderSvangerskapspenger(dataWrapper.getBehandlingTema())) {
            return sjekkOmInntektsmeldingGjelderMann(dataWrapper);
        } else {
           return true;
        }
    }

    private boolean sjekkOmInntektsmeldingGjelderMann(MottakMeldingDataWrapper dataWrapper) {
        String aktørId = dataWrapper.getAktørId().orElseThrow(() -> new IllegalStateException("Utviklerfeil"));
        String fnrBruker = aktørConsumer.hentPersonIdentForAktørId(aktørId)
                .orElseThrow(() -> MottakMeldingFeil
                        .fantIkkePersonidentForAktørId(TASKNAME, dataWrapper.getId()));
        return (Character.getNumericValue(fnrBruker.charAt(8)) % 2) != 0;
    }

    private static boolean kreverStartdatoForInntektsmeldingenManuellBehandling(MottakMeldingDataWrapper dataWrapper) {
        LocalDate startDato = dataWrapper.getInntektsmeldingStartDato().orElse(Tid.TIDENES_BEGYNNELSE);
        return startDato.isBefore(KonfigVerdier.ENDRING_BEREGNING_DATO);
    }

    private Optional<String> finnAktørId(ArkivJournalpost journalpost) {
        if (journalpost.getBrukerAktørId().isPresent()) return journalpost.getBrukerAktørId();
        if (journalpost.getAvsenderIdent() != null && journalpost.getOriginalJournalpost().avsenderMottaker().getIdHvisFNR().isPresent())
            return aktørConsumer.hentAktørIdForPersonIdent(journalpost.getAvsenderIdent());
        return Optional.empty();
    }

}