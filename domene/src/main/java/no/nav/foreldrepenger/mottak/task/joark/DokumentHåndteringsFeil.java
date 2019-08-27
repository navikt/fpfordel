package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;

public interface DokumentHåndteringsFeil extends DeklarerteFeil {
    DokumentHåndteringsFeil FACTORY = FeilFactory.create(DokumentHåndteringsFeil.class);

    @IntegrasjonFeil(feilkode = "FP-113493", feilmelding = "Finner ikke aktørID for bruker på dokument metadata", logLevel = WARN)
    Feil finnerIkkeAktørId();
}
