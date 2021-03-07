package no.nav.foreldrepenger.fordel.web.app.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.spi.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.exception.VLException;

class GeneralRestExceptionMapperTest {

    private GeneralRestExceptionMapper mapper;

    @BeforeEach
    public void setUp() throws Exception {
        mapper = new GeneralRestExceptionMapper();
    }

    @Test
    void skalMappeValideringsfeil() {
        FeltFeilDto feltFeilDto = new FeltFeilDto("Et feltnavn", "En feilmelding");
        Valideringsfeil valideringsfeil = new Valideringsfeil(List.of(feltFeilDto));

        Response response = mapper.toResponse(new ApplicationException(valideringsfeil));

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains(
                "Det oppstod en valideringsfeil på felt [Et feltnavn]");
        assertThat(feilDto.getFeltFeil()).hasSize(1);
        assertThat(feilDto.getFeltFeil().iterator().next()).isEqualTo(feltFeilDto);
    }

    @Test
    void skalMappeManglerTilgangFeil() {
        var manglerTilgangFeil = TestFeil.manglerTilgangFeil();

        Response response = mapper
                .toResponse(new ApplicationException(manglerTilgangFeil));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getType()).isEqualTo(FeilType.MANGLER_TILGANG_FEIL);
        assertThat(feilDto.getFeilmelding()).contains("ManglerTilgangFeilmeldingKode");
    }

    @Test
    void skalMappeFunksjonellFeil() {
        var funksjonellFeil = TestFeil.funksjonellFeil();

        Response response = mapper
                .toResponse(new ApplicationException(funksjonellFeil));

        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains("FUNK_FEIL");
        assertThat(feilDto.getFeilmelding()).contains("en funksjonell feilmelding");
        assertThat(feilDto.getFeilmelding()).contains("et løsningsforslag");
    }

    @Test
    void skalMappeVLException() {
        VLException vlException = TestFeil.tekniskFeil();

        Response response = mapper.toResponse(new ApplicationException(vlException));

        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains("TEK_FEIL");
        assertThat(feilDto.getFeilmelding()).contains("en teknisk feilmelding");
    }

    @Test
    void skalMappeGenerellFeil() {
        String feilmelding = "en helt generell feil";
        RuntimeException generellFeil = new RuntimeException(feilmelding);

        Response response = mapper.toResponse(new ApplicationException(generellFeil));

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getEntity()).isInstanceOf(FeilDto.class);
        FeilDto feilDto = (FeilDto) response.getEntity();

        assertThat(feilDto.getFeilmelding()).contains(feilmelding);
    }

    private static class TestFeil {

        static FunksjonellException funksjonellFeil() {
            return new FunksjonellException("FUNK_FEIL", "en funksjonell feilmelding", "et løsningsforslag");
        }

        static TekniskException tekniskFeil() {
            return new TekniskException("TEK_FEIL", "en teknisk feilmelding");
        }

        static ManglerTilgangException manglerTilgangFeil() {
            return new ManglerTilgangException("MANGLER_TILGANG_FEIL", "ManglerTilgangFeilmeldingKode");
        }
    }
}
