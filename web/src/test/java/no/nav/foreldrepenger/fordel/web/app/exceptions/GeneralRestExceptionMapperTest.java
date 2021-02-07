package no.nav.foreldrepenger.fordel.web.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

@SuppressWarnings("resource")
public class GeneralRestExceptionMapperTest {

    private GeneralRestExceptionMapper generalRestExceptionMapper;

    @BeforeEach
    public void setUp() throws Exception {
        generalRestExceptionMapper = new GeneralRestExceptionMapper();
    }

    @Test
    public void skalMappeValideringsfeil() {
        FeltFeilDto feltFeilDto = new FeltFeilDto("Et feltnavn", "En feilmelding");
        Valideringsfeil valideringsfeil = new Valideringsfeil(Collections.singleton(feltFeilDto));

        Response response = generalRestExceptionMapper.toResponse(new ApplicationException(valideringsfeil));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).isEqualTo(
                "Det oppstod en valideringsfeil på felt [Et feltnavn]. Vennligst kontroller at alle feltverdier er korrekte.");
        assertThat(feilDto.getFeltFeil()).hasSize(1);
        assertThat(feilDto.getFeltFeil().iterator().next()).isEqualTo(feltFeilDto);
    }

    @Test
    public void skalMappeManglerTilgangFeil() {
        Feil manglerTilgangFeil = TestFeil.FACTORY.manglerTilgangFeil();

        Response response = generalRestExceptionMapper
                .toResponse(new ApplicationException(manglerTilgangFeil.toException()));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getType()).isEqualTo(FeilType.MANGLER_TILGANG_FEIL);
        assertThat(feilDto.getFeilmelding()).isEqualTo("ManglerTilgangFeilmeldingKode");
    }

    @Test
    public void skalMappeFunksjonellFeil() {
        Feil funksjonellFeil = TestFeil.FACTORY.funksjonellFeil();

        Response response = generalRestExceptionMapper
                .toResponse(new ApplicationException(funksjonellFeil.toException()));

        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains("FUNK_FEIL");
        assertThat(feilDto.getFeilmelding()).contains("en funksjonell feilmelding");
        assertThat(feilDto.getFeilmelding()).contains("et løsningsforslag");
    }

    @Test
    public void skalMappeVLException() {
        VLException vlException = TestFeil.FACTORY.tekniskFeil().toException();

        Response response = generalRestExceptionMapper.toResponse(new ApplicationException(vlException));

        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains("TEK_FEIL");
        assertThat(feilDto.getFeilmelding()).contains("en teknisk feilmelding");
    }

    @Test
    public void skalMappeGenerellFeil() {
        String feilmelding = "en helt generell feil";
        RuntimeException generellFeil = new RuntimeException(feilmelding);

        Response response = generalRestExceptionMapper.toResponse(new ApplicationException(generellFeil));

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains(feilmelding);
    }

    interface TestFeil extends DeklarerteFeil {
        TestFeil FACTORY = FeilFactory.create(TestFeil.class); // NOSONAR ok med konstant i interface her

        @FunksjonellFeil(feilkode = "FUNK_FEIL", feilmelding = "en funksjonell feilmelding", løsningsforslag = "et løsningsforslag", logLevel = LogLevel.WARN)
        Feil funksjonellFeil();

        @TekniskFeil(feilkode = "TEK_FEIL", feilmelding = "en teknisk feilmelding", logLevel = LogLevel.WARN)
        Feil tekniskFeil();

        @ManglerTilgangFeil(feilkode = "MANGLER_TILGANG_FEIL", feilmelding = "ManglerTilgangFeilmeldingKode", logLevel = LogLevel.WARN)
        Feil manglerTilgangFeil();
    }
}
