package no.nav.foreldrepenger.mottak.felles;

import static java.util.stream.Collectors.joining;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.vedtak.felles.prosesstask.api.CommonTaskProperties;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

public class MottakMeldingDataWrapper {

    public static final String ARKIV_ID_KEY = "arkivId";
    public static final String AKTØR_ID_KEY = "aktoerId";
    public static final String SAKSNUMMER_KEY = CommonTaskProperties.SAKSNUMMER;
    public static final String KANAL_KEY = "kanal";
    public static final String TEMA_KEY = "tema";
    public static final String RETRY_KEY = "retry";
    public static final String BEHANDLINGSTEMA_KEY = "behandlingstema";
    public static final String DOKUMENTTYPE_ID_KEY = "dokumentTypeId";
    public static final String DOKUMENTKATEGORI_ID_KEY = "dokumentKategori";
    public static final String BARN_TERMINDATO_KEY = "barn.termindato";
    public static final String BARN_TERMINBEKREFTELSEDATO_KEY = "barn.terminbekreftelsedato";
    public static final String BARN_FODSELSDATO_KEY = "barn.fodselsdato";
    public static final String ADOPSJONSBARN_FODSELSDATOER_KEY = "adopsjonsbarn.fodselsdatoer";
    public static final String BARN_OMSORGSOVERTAKELSEDATO_KEY = "barn.omsorgsovertakelsedato";
    public static final String BARN_ANTALL_KEY = "barn.antall";
    public static final String STRUKTURERT_DOKUMENT = "strukturert.dokument";
    public static final String FORSENDELSE_MOTTATT_TIDSPUNKT_KEY = "forsendelse.mottatt.tidspunkt";
    public static final String JOURNAL_ENHET = "journalforende.enhet";
    public static final String FORSENDELSE_ID_KEY = "forsendelse.id";
    public static final String ANNEN_PART_ID_KEY = "annen.part.id";
    public static final String ANNEN_PART_HAR_RETT_KEY = "annen.part.har.rett";
    public static final String FØRSTE_UTTAKSDAG_KEY = "forste.uttaksdag";
    public static final String VIRKSOMHETSNUMMER = "virksomhetsnummer";
    public static final String ARBEIDSGIVER_AKTØR_ID = "arbeidsgiver.aktoerId";
    public static final String ARBEIDSFORHOLDSID = "arbeidsforholdsId";
    public static final String INNTEKTSMELDING_YTELSE = "im.ytelse";
    public static final String EKSTERN_REFERANSE = "eksternreferanse";

    // Inntektsmelding
    public static final String INNTEKSTMELDING_STARTDATO_KEY = "inntektsmelding.startdato";
    public static final String TYPE_ENDRING = "inntektsmelding.aarsak.til.innsending";

    private final ProsessTaskData prosessTaskData;

    public MottakMeldingDataWrapper(ProsessTaskData eksisterendeData) {
        this.prosessTaskData = eksisterendeData;
    }

    public ProsessTaskData getProsessTaskData() {
        return prosessTaskData;
    }

    public MottakMeldingDataWrapper nesteSteg(TaskType stegnavn) {
        return nesteSteg(stegnavn, LocalDateTime.now());
    }

    public MottakMeldingDataWrapper nesteSteg(TaskType stegnavn, LocalDateTime nesteKjøringEtter) {
        var nesteStegProsessTaskData = ProsessTaskData.forTaskType(stegnavn);
        nesteStegProsessTaskData.setNesteKjøringEtter(nesteKjøringEtter);

        String sekvensnummer = getProsessTaskData().getSekvens();
        if (sekvensnummer != null) {
            long sekvens = Long.parseLong(sekvensnummer);
            sekvensnummer = Long.toString(sekvens + 1);
            nesteStegProsessTaskData.setSekvens(sekvensnummer);
        }

        var neste = new MottakMeldingDataWrapper(nesteStegProsessTaskData);
        neste.copyData(this);
        return neste;
    }

    private void copyData(MottakMeldingDataWrapper fra) {
        this.addProperties(fra.getProsessTaskData().getProperties());
        this.setPayload(fra.getProsessTaskData().getPayloadAsString());
        this.getProsessTaskData().setGruppe(fra.getProsessTaskData().getGruppe());
    }

    private void addProperties(Properties newProps) {
        prosessTaskData.getProperties().putAll(newProps);
    }

    public Properties hentAlleProsessTaskVerdier() {
        return prosessTaskData.getProperties();
    }

    public Long getId() {
        return prosessTaskData.getId();
    }

    public Optional<String> getÅrsakTilInnsending() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(TYPE_ENDRING));
    }

    public void setÅrsakTilInnsending(String endringstype) {
        prosessTaskData.setProperty(TYPE_ENDRING, endringstype);
    }

    public BehandlingTema getBehandlingTema() {
        return BehandlingTema.fraKode(prosessTaskData.getPropertyValue(BEHANDLINGSTEMA_KEY));
    }

    public void setBehandlingTema(BehandlingTema behandlingTema) {
        prosessTaskData.setProperty(BEHANDLINGSTEMA_KEY, behandlingTema.getKode());
    }

    public Tema getTema() {
        return Tema.fraKode(prosessTaskData.getPropertyValue(TEMA_KEY));
    }

    public void setTema(Tema tema) {
        prosessTaskData.setProperty(TEMA_KEY, tema.getKode());
    }

    public boolean getHarTema() {
        return prosessTaskData.getPropertyValue(TEMA_KEY) != null;
    }


    public Optional<String> getKanal() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(KANAL_KEY));
    }

    public void setKanal(String kanal) {
        prosessTaskData.setProperty(KANAL_KEY, kanal);
    }


    public String getArkivId() {
        return prosessTaskData.getPropertyValue(ARKIV_ID_KEY);
    }

    public void setArkivId(String arkivId) {
        prosessTaskData.setProperty(ARKIV_ID_KEY, arkivId);
    }

    public Optional<String> getSaksnummer() {
        return Optional.ofNullable(prosessTaskData.getSaksnummer());
    }

    public void setSaksnummer(String saksnummer) {
        prosessTaskData.setSaksnummer(saksnummer);
    }

    public Optional<DokumentTypeId> getDokumentTypeId() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(DOKUMENTTYPE_ID_KEY)).map(DokumentTypeId::fraKodeDefaultUdefinert);
    }

    public void setDokumentTypeId(DokumentTypeId dokumentTypeId) {
        prosessTaskData.setProperty(DOKUMENTTYPE_ID_KEY, dokumentTypeId.getKode());
    }

    public Optional<DokumentKategori> getDokumentKategori() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(DOKUMENTKATEGORI_ID_KEY)).map(DokumentKategori::fraKodeDefaultUdefinert);
    }

    public void setDokumentKategori(DokumentKategori dokumentKategori) {
        prosessTaskData.setProperty(DOKUMENTKATEGORI_ID_KEY, dokumentKategori.getKode());
    }

    public final LocalDate getForsendelseMottatt() {
        return getForsendelseMottattTidspunkt().map(LocalDateTime::toLocalDate).orElse(null);
    }

    public Optional<LocalDateTime> getForsendelseMottattTidspunkt() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(FORSENDELSE_MOTTATT_TIDSPUNKT_KEY))
            .map(p -> LocalDateTime.parse(p, DateTimeFormatter.ISO_DATE_TIME));
    }

    public void setForsendelseMottattTidspunkt(LocalDateTime forsendelseMottattTidspunkt) {
        prosessTaskData.setProperty(FORSENDELSE_MOTTATT_TIDSPUNKT_KEY, forsendelseMottattTidspunkt.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    public Optional<String> getRetryingTask() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(RETRY_KEY));
    }

    public void setRetryingTask(String suffix) {
        prosessTaskData.setProperty(RETRY_KEY, suffix);
    }

    public Optional<String> getJournalførendeEnhet() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(JOURNAL_ENHET));
    }

    public void setJournalførendeEnhet(String enhet) {
        prosessTaskData.setProperty(JOURNAL_ENHET, enhet);
    }

    public Optional<String> getEksternReferanseId() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(EKSTERN_REFERANSE));
    }

    public void setEksternReferanseId(String enhet) {
        prosessTaskData.setProperty(EKSTERN_REFERANSE, enhet);
    }

    public void setPayload(String payload) {
        prosessTaskData.setPayload(payload);
    }

    public Optional<String> getPayloadAsString() {
        return Optional.ofNullable(prosessTaskData.getPayloadAsString());
    }

    public Optional<String> getAktørId() {
        return Optional.ofNullable(prosessTaskData.getAktørId());
    }

    public void setAktørId(String aktørId) {
        prosessTaskData.setAktørId(aktørId);
    }

    public Optional<LocalDate> getBarnTermindato() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(BARN_TERMINDATO_KEY)).map(LocalDate::parse);
    }

    public void setBarnTermindato(LocalDate dato) {
        prosessTaskData.setProperty(BARN_TERMINDATO_KEY, dato.toString());
    }

    public Optional<LocalDate> getBarnTerminbekreftelsedato() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(BARN_TERMINBEKREFTELSEDATO_KEY)).map(LocalDate::parse);
    }

    public void setBarnTerminbekreftelsedato(LocalDate dato) {
        prosessTaskData.setProperty(BARN_TERMINBEKREFTELSEDATO_KEY, dato.toString());
    }

    /**
     * Relevant ved adopsjon
     *
     * @return Liste over fødselsdatoer for adopsjonsbarn
     */
    public List<LocalDate> getAdopsjonsbarnFodselsdatoer() {
        return Arrays.stream(
                Optional.ofNullable(prosessTaskData.getPropertyValue(ADOPSJONSBARN_FODSELSDATOER_KEY)).map(p -> p.split(";")).orElse(new String[0]))
            .map(LocalDate::parse)
            .toList();
    }

    public void setAdopsjonsbarnFodselsdatoer(List<LocalDate> datoer) {
        if ((datoer != null) && !datoer.isEmpty()) {
            String datoList = datoer.stream().map(LocalDate::toString).collect(joining(";"));
            prosessTaskData.setProperty(ADOPSJONSBARN_FODSELSDATOER_KEY, datoList.isEmpty() ? null : datoList);
        }
    }

    /**
     * Relevant ved fødsel
     *
     * @return Fødselsdato som LocalDate
     */
    public Optional<LocalDate> getBarnFodselsdato() {
        String property = prosessTaskData.getPropertyValue(BARN_FODSELSDATO_KEY);
        if (property != null) {
            if (property.contains(";")) {
                throw new IllegalStateException("Inneholder flere bursdager.");
            }
            return Optional.of(LocalDate.parse(property));
        }
        return Optional.empty();
    }

    public void setBarnFodselsdato(LocalDate dato) {
        prosessTaskData.setProperty(BARN_FODSELSDATO_KEY, dato.toString());
    }

    public Optional<LocalDate> getOmsorgsovertakelsedato() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(BARN_OMSORGSOVERTAKELSEDATO_KEY)).map(LocalDate::parse);
    }

    public void setOmsorgsovertakelsedato(LocalDate dato) {
        prosessTaskData.setProperty(BARN_OMSORGSOVERTAKELSEDATO_KEY, dato.toString());
    }

    public Optional<Integer> getAntallBarn() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(BARN_ANTALL_KEY)).map(Integer::parseInt);
    }

    public void setAntallBarn(int antallBarn) {
        prosessTaskData.setProperty(BARN_ANTALL_KEY, String.valueOf(antallBarn));
    }

    public Optional<Boolean> erStrukturertDokument() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(STRUKTURERT_DOKUMENT)).map(Boolean::parseBoolean);
    }

    public void setStrukturertDokument(Boolean erStrukturertDokument) {
        prosessTaskData.setProperty(STRUKTURERT_DOKUMENT, String.valueOf(erStrukturertDokument));
    }

    public void setInntekstmeldingStartdato(LocalDate dato) {
        prosessTaskData.setProperty(INNTEKSTMELDING_STARTDATO_KEY, dato != null ? dato.toString() : null);
    }

    public Optional<LocalDate> getInntektsmeldingStartDato() {
        String property = prosessTaskData.getProperties().getProperty(INNTEKSTMELDING_STARTDATO_KEY);
        if (property != null) {
            if (property.contains(";")) {
                throw new IllegalStateException("Inneholder flere startdatoer.");
            }
            return Optional.of(LocalDate.parse(property));
        }
        return Optional.empty();
    }

    public Optional<UUID> getForsendelseId() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(FORSENDELSE_ID_KEY)).map(UUID::fromString);
    }

    public void setForsendelseId(UUID forsendelseId) {
        prosessTaskData.setProperty(FORSENDELSE_ID_KEY, forsendelseId.toString());
    }

    public Optional<String> getAnnenPartId() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(ANNEN_PART_ID_KEY));
    }

    public void setAnnenPartId(String annenPartId) {
        prosessTaskData.setProperty(ANNEN_PART_ID_KEY, annenPartId);
    }

    public Optional<Boolean> getAnnenPartHarRett() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(ANNEN_PART_HAR_RETT_KEY)).map(Boolean::parseBoolean);
    }

    public void setAnnenPartHarRett(Boolean annenPartHarRett) {
        prosessTaskData.setProperty(ANNEN_PART_HAR_RETT_KEY, String.valueOf(annenPartHarRett));
    }

    public Optional<LocalDate> getFørsteUttaksdag() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(FØRSTE_UTTAKSDAG_KEY)).map(LocalDate::parse);
    }

    public void setFørsteUttakssdag(LocalDate dato) {
        Optional.ofNullable(dato).map(LocalDate::toString).ifPresent(d -> prosessTaskData.setProperty(FØRSTE_UTTAKSDAG_KEY, d));
    }

    public Optional<String> getVirksomhetsnummer() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(VIRKSOMHETSNUMMER));
    }

    public void setVirksomhetsnummer(String virksomhetsnummer) {
        prosessTaskData.setProperty(VIRKSOMHETSNUMMER, virksomhetsnummer);
    }

    public Optional<String> getArbeidsgiverAktørId() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(ARBEIDSGIVER_AKTØR_ID));
    }

    public void setArbeidsgiverAktørId(String aktørId) {
        prosessTaskData.setProperty(ARBEIDSGIVER_AKTØR_ID, aktørId);
    }

    public Optional<String> getArbeidsforholdsid() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(ARBEIDSFORHOLDSID));
    }

    public void setArbeidsforholdsid(String arbeidsforholdsId) {
        prosessTaskData.setProperty(ARBEIDSFORHOLDSID, arbeidsforholdsId);
    }

    public Optional<String> getInntektsmeldingYtelse() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(INNTEKTSMELDING_YTELSE));
    }

    public void setInntektsmeldingYtelse(String ytelse) {
        prosessTaskData.setProperty(INNTEKTSMELDING_YTELSE, ytelse);
    }
}
