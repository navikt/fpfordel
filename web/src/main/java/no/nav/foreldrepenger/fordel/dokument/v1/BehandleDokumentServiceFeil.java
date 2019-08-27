package no.nav.foreldrepenger.fordel.dokument.v1;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.FunksjonellFeil;

public interface BehandleDokumentServiceFeil extends DeklarerteFeil {

    BehandleDokumentServiceFeil FACTORY = FeilFactory.create(BehandleDokumentServiceFeil.class);

    @FunksjonellFeil(feilkode = "FP-963070", feilmelding = "Kan ikke journalføre på saksnummer: %s", løsningsforslag = "Journalføre dokument på annen sak i VL", logLevel = LogLevel.WARN)
    Feil finnerIkkeFagsak(String saksnummer);

    @FunksjonellFeil(feilkode = "FP-963074", feilmelding = "Klager må journalføres på sak med tidligere behandling", løsningsforslag = "Journalføre klagen på sak med avsluttet behandling", logLevel = LogLevel.WARN)
    Feil sakUtenAvsluttetBehandling();

    @FunksjonellFeil(feilkode = "FP-963075", feilmelding = "Inntektsmelding årsak samsvarer ikke med sakens type - kan ikke journalføre", løsningsforslag = "Be om ny Inntektsmelding for Foreldrepenger", logLevel = LogLevel.WARN)
    Feil imFeilType();

    @FunksjonellFeil(feilkode = "FP-963076", feilmelding = "Inntektsmelding mangler startdato - kan ikke journalføre", løsningsforslag = "Be om ny Inntektsmelding med startdato", logLevel = LogLevel.WARN)
    Feil imUtenStartdato();

    @FunksjonellFeil(feilkode = "FP-963077", feilmelding = "For tidlig uttak", løsningsforslag = "Søknad om uttak med oppstart i 2018 skal journalføres mot sak i Infotrygd", logLevel = LogLevel.WARN)
    Feil forTidligUttak();

    @FunksjonellFeil(feilkode = "FP-424242", feilmelding = "Sak %s har åpen behandling med søknad", løsningsforslag = "Ferdigstill den åpne behandlingen før en ny søknad journalføres på saken", logLevel = LogLevel.WARN)
    Feil kanIkkeJournalføreSvpSøknadPåÅpenBehandling(String saksnummer);
}
