package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import static no.nav.vedtak.feil.LogLevel.WARN;

import javax.ws.rs.core.MediaType;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface DokumentforsendelseTjenesteFeil extends DeklarerteFeil {
    DokumentforsendelseTjenesteFeil FACTORY = FeilFactory.create(DokumentforsendelseTjenesteFeil.class);

    @TekniskFeil(feilkode = "FP-728553", feilmelding = "Saksnummer er påkrevd ved ettersendelser", logLevel = WARN)
    Feil saksnummerPåkrevdVedEttersendelser();

    @TekniskFeil(feilkode = "FP-728555", feilmelding = "Hoveddokumentet skal alltid sendes som to dokumenter med %s: %s og %s", logLevel = WARN)
    Feil hoveddokumentSkalSendesSomToDokumenter(String content_type, String dokumenttype1, MediaType dokumenttype2);
}
