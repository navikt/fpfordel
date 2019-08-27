package no.nav.foreldrepenger.fordel.web.app.rest;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import java.io.IOException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface DokumentforsendelseRestTjenesteFeil extends DeklarerteFeil {
    DokumentforsendelseRestTjenesteFeil FACTORY = FeilFactory.create(DokumentforsendelseRestTjenesteFeil.class);

    @TekniskFeil(feilkode = "FP-892453", feilmelding = "The first part must be the \"metadata\" part", logLevel = ERROR)
    Feil førsteInputPartSkalVæreMetadata();

    @TekniskFeil(feilkode = "FP-892454", feilmelding = "The \"metadata\" part should be %s", logLevel = ERROR)
    Feil metadataPartSkalHaMediaType(String mediaType);

    @TekniskFeil(feilkode = "FP-892455", feilmelding = "Mangler %s", logLevel = ERROR)
    Feil manglerHeaderAttributt(String attributt);

    @TekniskFeil(feilkode = "FP-892456", feilmelding = "Metadata inneholder flere filer enn det som er lastet opp", logLevel = ERROR)
    Feil forventetFlereFilerIForsendelsen();

    @TekniskFeil(feilkode = "FP-892446", feilmelding = "Metadata inneholder ikke informasjon om Content-ID=%s", logLevel = ERROR)
    Feil manglerInformasjonIMetadata(String contentId);

    @TekniskFeil(feilkode = "FP-892457", feilmelding = "Unknown part name", logLevel = WARN)
    Feil ukjentPartNavn();

    @TekniskFeil(feilkode = "FP-892458", feilmelding = "Klarte ikke å parse %s", logLevel = WARN)
    Feil kunneIkkeParseMetadata(String json, IOException e);

    @TekniskFeil(feilkode = "FP-892466", feilmelding = "Klarte ikke å lese inn dokumentet, name=%s", logLevel = WARN)
    Feil feiletUnderInnlesningAvInputPart(String name, IOException e);

    @TekniskFeil(feilkode = "FP-892467", feilmelding = "Klarte ikke å lese inn dokumentet, name=%s, Content-ID=%s", logLevel = WARN)
    Feil feiletUnderInnlesningAvInputPart(String name, String contentId, IOException e);

    @TekniskFeil(feilkode = "FP-892468", feilmelding = "Ulovlig mediatype %s", logLevel = ERROR)
    Feil ulovligFilType(String type);
    
    @TekniskFeil(feilkode = "FP-882558", feilmelding = "Vedlegg er ikke pdf, Content-ID=%s", logLevel = WARN)
    Feil vedleggErIkkePdf(String contentId);
}
