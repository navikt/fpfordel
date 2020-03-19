package no.nav.foreldrepenger.mottak.felles;

import java.sql.Clob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class MottakMeldingDataWrapper {

    public static final String ARKIV_ID_KEY = "arkivId";
    public static final String AKTØR_ID_KEY = "aktoerId";
    public static final String SAKSNUMMER_KEY = "saksnummer";
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
    public static final String AVSENDER_ID_KEY = "avsender.id";
    public static final String ANNEN_PART_ID_KEY = "annen.part.id";
    public static final String ANNEN_PART_HAR_RETT_KEY = "annen.part.har.rett";
    public static final String FØRSTE_UTTAKSDAG_KEY = "forste.uttaksdag";
    public static final String VIRKSOMHETSNUMMER = "virksomhetsnummer";
    public static final String ARBEIDSGIVER_AKTØR_ID = "arbeidsgiver.aktoerId";
    public static final String ARBEIDSFORHOLDSID = "arbeidsforholdsId";
    public static final String INNTEKTSMELDING_YTELSE = "im.ytelse";

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

    public MottakMeldingDataWrapper nesteSteg(String stegnavn) {
        return nesteSteg(stegnavn, true, LocalDateTime.now());
    }

    public MottakMeldingDataWrapper nesteSteg(String stegnavn, boolean økSekvens, LocalDateTime nesteKjøringEtter) {
        ProsessTaskData nesteStegProsessTaskData = new ProsessTaskData(stegnavn);
        nesteStegProsessTaskData.setNesteKjøringEtter(nesteKjøringEtter);

        String sekvensnummer = getProsessTaskData().getSekvens();
        if (økSekvens) {
            long sekvens = Long.parseLong(sekvensnummer);
            sekvensnummer = Long.toString(sekvens + 1);
        }
        nesteStegProsessTaskData.setSekvens(sekvensnummer);

        MottakMeldingDataWrapper neste = new MottakMeldingDataWrapper(nesteStegProsessTaskData);
        neste.copyData(this);
        return neste;
    }

    public MottakMeldingDataWrapper nesteSteg(String stegnavn, LocalDateTime nesteKjøring) {
        return nesteSteg(stegnavn,true, nesteKjøring);
    }

    private void copyData(MottakMeldingDataWrapper fra) {
        this.addProperties(fra.getProsessTaskData().getProperties());
        this.setPayload(fra.getProsessTaskData().getPayload());
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

    public boolean getHarTema() {
        return prosessTaskData.getPropertyValue(TEMA_KEY) != null;
    }

    public void setTema(Tema tema) {
        prosessTaskData.setProperty(TEMA_KEY, tema.getKode());
    }

    public String getArkivId() {
        return prosessTaskData.getPropertyValue(ARKIV_ID_KEY);
    }

    public void setArkivId(String arkivId) {
        prosessTaskData.setProperty(ARKIV_ID_KEY, arkivId);
    }

    public Optional<String> getSaksnummer() {
        return Optional.ofNullable(prosessTaskData.getPropertyValue(SAKSNUMMER_KEY));
    }

    public void setSaksnummer(String saksnummer) {
        prosessTaskData.setProperty(SAKSNUMMER_KEY, saksnummer);
    }

    public Optional<DokumentTypeId> getDokumentTypeId() {
        String prop = prosessTaskData.getPropertyValue(DOKUMENTTYPE_ID_KEY);
        return prop == null ? Optional.empty() : Optional.of(DokumentTypeId.fraKodeDefaultUdefinert(prop));
    }

    public void setDokumentTypeId(DokumentTypeId dokumentTypeId) {
        prosessTaskData.setProperty(DOKUMENTTYPE_ID_KEY, dokumentTypeId.getKode());
    }

    public Optional<DokumentKategori> getDokumentKategori() {
        String prop = prosessTaskData.getPropertyValue(DOKUMENTKATEGORI_ID_KEY);
        return prop == null ? Optional.empty() : Optional.of(DokumentKategori.fraKodeDefaultUdefinert(prop));
    }

    public void setDokumentKategori(DokumentKategori dokumentKategori) {
        prosessTaskData.setProperty(DOKUMENTKATEGORI_ID_KEY, dokumentKategori.getKode());
    }

    public final LocalDate getForsendelseMottatt() {
        Optional<LocalDateTime> localDateTime = getForsendelseMottattTidspunkt();
        return localDateTime.map(LocalDateTime::toLocalDate).orElse(null);
    }

    public Optional<LocalDateTime> getForsendelseMottattTidspunkt() {
        final String property = prosessTaskData.getProperties().getProperty(FORSENDELSE_MOTTATT_TIDSPUNKT_KEY);
        final LocalDateTime localDateTime = property != null ? LocalDateTime.parse(property, DateTimeFormatter.ISO_DATE_TIME) : null;
        return Optional.ofNullable(localDateTime);
    }

    public void setForsendelseMottattTidspunkt(LocalDateTime forsendelseMottattTidspunkt) {
        prosessTaskData.setProperty(FORSENDELSE_MOTTATT_TIDSPUNKT_KEY, forsendelseMottattTidspunkt.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    public Optional<String> getRetryingTask() {
        return Optional.ofNullable(prosessTaskData.getProperties().getProperty(RETRY_KEY));
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

    public void setPayload(String payload) {
        prosessTaskData.setPayload(payload);
    }

    public void setPayload(Clob payload) {
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
        final String property = prosessTaskData.getProperties().getProperty(BARN_TERMINDATO_KEY);
        LocalDate localDate = property != null ? LocalDate.parse(property) : null;
        return Optional.ofNullable(localDate);
    }

    public void setBarnTermindato(LocalDate dato) {
        prosessTaskData.setProperty(BARN_TERMINDATO_KEY, dato.toString());
    }

    public Optional<LocalDate> getBarnTerminbekreftelsedato() {
        final String property = prosessTaskData.getProperties().getProperty(BARN_TERMINBEKREFTELSEDATO_KEY);
        LocalDate localDate = property != null ? LocalDate.parse(property) : null;
        return Optional.ofNullable(localDate);
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
        String property = prosessTaskData.getProperties().getProperty(ADOPSJONSBARN_FODSELSDATOER_KEY);
        if (property != null) {
            return Arrays.stream(property.split(";")).map(LocalDate::parse).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public void setAdopsjonsbarnFodselsdatoer(List<LocalDate> datoer) {
        if (datoer != null && !datoer.isEmpty()) {
            String datoList = datoer.stream()
                    .map(LocalDate::toString)
                    .collect(Collectors.joining(";"));
            prosessTaskData.setProperty(ADOPSJONSBARN_FODSELSDATOER_KEY, datoList.isEmpty() ? null : datoList);
        }
    }

    /**
     * Relevant ved fødsel
     *
     * @return Fødselsdato som LocalDate
     */
    public Optional<LocalDate> getBarnFodselsdato() {
        String property = prosessTaskData.getProperties().getProperty(BARN_FODSELSDATO_KEY);
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
        final String property = prosessTaskData.getProperties().getProperty(BARN_OMSORGSOVERTAKELSEDATO_KEY);
        LocalDate localDate = property != null ? LocalDate.parse(property) : null;
        return Optional.ofNullable(localDate);
    }

    public void setOmsorgsovertakelsedato(LocalDate dato) {
        prosessTaskData.setProperty(BARN_OMSORGSOVERTAKELSEDATO_KEY, dato.toString());
    }

    public Optional<Integer> getAntallBarn() {
        final String property = prosessTaskData.getProperties().getProperty(BARN_ANTALL_KEY);
        Integer integer = property != null ? Integer.parseInt(property) : null;
        return Optional.ofNullable(integer);
    }

    public void setAntallBarn(int antallBarn) {
        prosessTaskData.setProperty(BARN_ANTALL_KEY, Integer.toString(antallBarn));
    }

    public Optional<Boolean> erStrukturertDokument() {
        final String property = prosessTaskData.getPropertyValue(STRUKTURERT_DOKUMENT);
        Boolean bool = property != null ? Boolean.parseBoolean(property) : null;
        return Optional.ofNullable(bool);
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
        String forsendelseId = prosessTaskData.getPropertyValue(FORSENDELSE_ID_KEY);
        return forsendelseId == null ? Optional.empty() : Optional.of(UUID.fromString(forsendelseId));
    }

    public void setForsendelseId(UUID forsendelseId) {
        prosessTaskData.setProperty(FORSENDELSE_ID_KEY, forsendelseId.toString());
    }

    public Optional<String> getAvsenderId() {
        return Optional.ofNullable(prosessTaskData.getProperties().getProperty(AVSENDER_ID_KEY));
    }

    public void setAvsenderId(String avsenderId) {
        prosessTaskData.setProperty(AVSENDER_ID_KEY, avsenderId);
    }

    public Optional<String> getAnnenPartId() {
        return Optional.ofNullable(prosessTaskData.getProperties().getProperty(ANNEN_PART_ID_KEY));
    }

    public void setAnnenPartId(String annenPartId) {
        prosessTaskData.setProperty(ANNEN_PART_ID_KEY, annenPartId);
    }

    public Optional<Boolean> getAnnenPartHarRett() {
        final String property = prosessTaskData.getPropertyValue(ANNEN_PART_HAR_RETT_KEY);
        Boolean bool = property != null ? Boolean.parseBoolean(property) : null;
        return Optional.ofNullable(bool);
    }

    public void setAnnenPartHarRett(Boolean annenPartHarRett) {
        prosessTaskData.setProperty(ANNEN_PART_HAR_RETT_KEY, String.valueOf(annenPartHarRett));
    }

    public Optional<LocalDate> getFørsteUttaksdag() {
        final String property = prosessTaskData.getProperties().getProperty(FØRSTE_UTTAKSDAG_KEY);
        final LocalDate localDate = property != null ? LocalDate.parse(property) : null;
        return Optional.ofNullable(localDate);
    }

    public void setFørsteUttakssdag(LocalDate dato) {
        prosessTaskData.setProperty(FØRSTE_UTTAKSDAG_KEY, dato != null ? dato.toString() : null);
    }

    public Optional<String> getVirksomhetsnummer() {
        final String virksomhetsnummer = prosessTaskData.getProperties().getProperty(VIRKSOMHETSNUMMER);
        return Optional.ofNullable(virksomhetsnummer);
    }

    public void setVirksomhetsnummer(String virksomhetsnummer) {
        prosessTaskData.setProperty(VIRKSOMHETSNUMMER, virksomhetsnummer);
    }

    public Optional<String> getArbeidsgiverAktørId() {
        final String arbeidsgiverAktørId = prosessTaskData.getProperties().getProperty(ARBEIDSGIVER_AKTØR_ID);
        return Optional.ofNullable(arbeidsgiverAktørId);
    }

    public void setArbeidsgiverAktørId(String aktørId) {
        prosessTaskData.setProperty(ARBEIDSGIVER_AKTØR_ID, aktørId);
    }

    public Optional<String> getArbeidsforholdsid() {
        final String arbeidsforholdsId = prosessTaskData.getProperties().getProperty(ARBEIDSFORHOLDSID);
        return Optional.ofNullable(arbeidsforholdsId);
    }

    public void setArbeidsforholdsid(String arbeidsforholdsId) {
        prosessTaskData.setProperty(ARBEIDSFORHOLDSID, arbeidsforholdsId);
    }

    public Optional<String> getInntektsmeldingYtelse() {
        final String ytelse = prosessTaskData.getProperties().getProperty(INNTEKTSMELDING_YTELSE);
        return Optional.ofNullable(ytelse);
    }

    public void setInntektsmeldingYtelse(String ytelse) {
        prosessTaskData.setProperty(INNTEKTSMELDING_YTELSE, ytelse);
    }

}
