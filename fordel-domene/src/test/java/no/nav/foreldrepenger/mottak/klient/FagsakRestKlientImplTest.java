package no.nav.foreldrepenger.mottak.klient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.dokumentforsendelse.BehandleDokumentforsendelseTask;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(MockitoExtension.class)
public class FagsakRestKlientImplTest {

    @Mock
    private OidcRestClient oidcRestClient;

    private FagsakTjeneste fagsakRestKlient;

    @Test
    public void testInternMapping() {
        LocalDate fødselsdato = LocalDate.now().plusDays(7);
        List<LocalDate> adopsjonsBarnFødselsdatoer = Arrays.asList(LocalDate.now().minusYears(1), LocalDate.now().minusYears(2));
        LocalDate termindato = LocalDate.now();
        LocalDate omsorgsovertagelsesDato = LocalDate.now().plusDays(1);
        String saksnummer = "666";
        String aktørId = "9000000000009";
        String årsakInntektsmelding = "Endring";
        String annenPartId = "9000000000009";
        String arbeidsforholdsid = "56654679";
        String virksomhetsnummer = "999999999";
        LocalDateTime nå = LocalDateTime.now();

        fagsakRestKlient = new LegacyFagsakRestKlient(oidcRestClient, URI.create("http://fpsak"));
        var captor = ArgumentCaptor.forClass(VurderFagsystemDto.class);
        when(oidcRestClient.post(any(), captor.capture(), any())).thenReturn(null);

        var data = new ProsessTaskData(BehandleDokumentforsendelseTask.TASKNAME);
        data.setSekvens("1");
        var w = new MottakMeldingDataWrapper(data);
        w.setAktørId(aktørId);
        w.setBarnTermindato(termindato);
        w.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        w.setAdopsjonsbarnFodselsdatoer(adopsjonsBarnFødselsdatoer);
        w.setBarnTermindato(termindato);
        w.setBarnFodselsdato(fødselsdato);
        w.setBehandlingTema(BehandlingTema.FORELDREPENGER_ADOPSJON);
        w.setOmsorgsovertakelsedato(omsorgsovertagelsesDato);
        w.setÅrsakTilInnsending(årsakInntektsmelding);
        w.setAnnenPartId(annenPartId);
        w.setSaksnummer(saksnummer);
        w.setArbeidsforholdsid(arbeidsforholdsid);
        w.setVirksomhetsnummer(virksomhetsnummer);
        w.setInntekstmeldingStartdato(fødselsdato);
        w.setForsendelseMottattTidspunkt(nå);

        fagsakRestKlient.vurderFagsystem(w);
        var dto = captor.getValue();

        assertThat(dto.getAktørId()).isEqualTo(aktørId);
        assertThat(dto.getSaksnummer()).isEqualTo(Optional.of(saksnummer));
        assertThat(dto.getBarnTermindato()).isEqualTo(Optional.of(termindato));
        assertThat(dto.getBarnFodselsdato()).isEqualTo(Optional.of(fødselsdato));
        assertThat(dto.getAdopsjonsBarnFodselsdatoer()).isEqualTo(adopsjonsBarnFødselsdatoer);
        assertThat(dto.getOmsorgsovertakelsedato()).isEqualTo(Optional.of(omsorgsovertagelsesDato));
        assertThat(dto.getÅrsakInnsendingInntektsmelding()).isEqualTo(Optional.of(årsakInntektsmelding));
        assertThat(dto.getAnnenPart()).isEqualTo(Optional.of(annenPartId));
        assertThat(dto.getBehandlingstemaOffisiellKode()).isEqualTo(BehandlingTema.FORELDREPENGER_ADOPSJON.getOffisiellKode());
        assertThat(dto.getVirksomhetsnummer()).isEqualTo(Optional.of(virksomhetsnummer));
        assertThat(dto.getArbeidsforholdsid()).isEqualTo(Optional.of(arbeidsforholdsid));
        assertThat(dto.getStartDatoForeldrepengerInntektsmelding()).isEqualTo(Optional.of(fødselsdato));
        assertThat(dto.getForsendelseMottattTidspunkt()).isEqualTo(Optional.of(nå));
    }

}
