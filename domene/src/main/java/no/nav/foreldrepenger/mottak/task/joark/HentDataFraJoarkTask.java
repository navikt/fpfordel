package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste.erStrukturertDokument;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.konfig.KonfigVerdier;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.konfig.Tid;
import no.nav.vedtak.util.FPDateUtil;
import no.nav.vedtak.util.StringUtils;

/**
 * <p>
 * ProssessTask som håndterer uthenting av saksinformasjon fra
 * Journalarkivet(joark).
 * </p>
 * <p>
 * Avhengig av integerasjonen mot Journalarkivet for uthenting av metadata og
 * søknads-xml.
 * </p>
 */
@Dependent
@ProsessTask(HentDataFraJoarkTask.TASKNAME)
public class HentDataFraJoarkTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.hentFraJoark";

    private final AktørConsumer aktørConsumer;
    private final JoarkDokumentHåndterer joarkDokumentHåndterer;
    private final LocalDate fastsattInntektsmeldingStartdatoFristForManuellBehandling = KonfigVerdier.ENDRING_BEREGNING_DATO;

    @Inject
    public HentDataFraJoarkTask(ProsessTaskRepository prosessTaskRepository,
            AktørConsumer aktørConsumer, JoarkDokumentHåndterer joarkDokumentHåndterer) {
        super(prosessTaskRepository);
        this.aktørConsumer = aktørConsumer;
        this.joarkDokumentHåndterer = joarkDokumentHåndterer;
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
        final List<JournalMetadata> hoveddokumenter = joarkDokumentHåndterer
                .hentJoarkDokumentMetadata(dataWrapper.getArkivId());
        if (hoveddokumenter.isEmpty()) {
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }

        // Legg til task-parametere fra innkommende journalpost
        DokumentTypeId dokumentTypeId = HentDataFraJoarkTjeneste.hentDokumentTypeId(hoveddokumenter);
        dataWrapper.setDokumentTypeId(dokumentTypeId);
        HentDataFraJoarkTjeneste.hentDokumentKategori(hoveddokumenter).ifPresent(dataWrapper::setDokumentKategori);
        dataWrapper.setBehandlingTema(HentDataFraJoarkTjeneste.korrigerBehandlingTemaFraDokumentType(
                dataWrapper.getTema(), dataWrapper.getBehandlingTema(), dokumentTypeId));
        dataWrapper.setForsendelseMottattTidspunkt(
                HentDataFraJoarkTjeneste.hentForsendelseMottattTidspunkt(hoveddokumenter));
        HentDataFraJoarkTjeneste.hentJournalførendeEnhet(hoveddokumenter)
                .ifPresent(dataWrapper::setJournalførendeEnhet);
        joarkDokumentHåndterer.hentGyldigAktørFraMetadata(hoveddokumenter).ifPresent(dataWrapper::setAktørId);
        dataWrapper.setStrukturertDokument(erStrukturertDokument(hoveddokumenter));

        if (erStrukturertDokument(hoveddokumenter)) {
            JournalDokument journalDokument = joarkDokumentHåndterer
                    .hentJournalDokument(hoveddokumenter);
            MottattStrukturertDokument<?> mottattDokument = joarkDokumentHåndterer
                    .unmarshallXMLDokument(journalDokument.getXml());
            mottattDokument.kopierTilMottakWrapper(dataWrapper, joarkDokumentHåndterer::hentGyldigAktørFraPersonident);
            dataWrapper.setPayload(journalDokument.getXml());
            if (dataWrapper.getForsendelseMottattTidspunkt().isEmpty()) {
                dataWrapper.setForsendelseMottattTidspunkt(FPDateUtil.nå());
            }
        }

        if (dataWrapper.getAktørId().isEmpty() || !Tema.FORELDRE_OG_SVANGERSKAPSPENGER.equals(dataWrapper.getTema())) {
            return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }

        if (DokumentTypeId.erInntektsmelding(dokumentTypeId)) {
            return håndterInntektsmelding(dataWrapper);
        }

        // Videre håndtering
        return dataWrapper.nesteSteg(HentOgVurderVLSakTask.TASKNAME);
    }

    private MottakMeldingDataWrapper håndterInntektsmelding(MottakMeldingDataWrapper dataWrapper) {
        Optional<String> imYtelse = dataWrapper.getInntektsmeldingYtelse();
        if (imYtelse.isEmpty()) {
            throw MottakMeldingFeil.FACTORY.manglerYtelsePåInntektsmelding().toException();
        }
        BehandlingTema behandlingTemaFraIM = BehandlingTema.fraTermNavn(imYtelse.get());

        dataWrapper.setInntektsmeldingYtelse(imYtelse.get());
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
        return Character.getNumericValue(fnrBruker.charAt(8)) % 2 != 0;
    }

    private boolean kreverStartdatoForInntektsmeldingenManuellBehandling(MottakMeldingDataWrapper dataWrapper) {
        LocalDate startDato = dataWrapper.getInntektsmeldingStartDato().orElse(Tid.TIDENES_BEGYNNELSE);
        return startDato.isBefore(fastsattInntektsmeldingStartdatoFristForManuellBehandling);
    }
}