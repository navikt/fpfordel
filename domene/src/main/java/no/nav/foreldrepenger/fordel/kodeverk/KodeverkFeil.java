package no.nav.foreldrepenger.fordel.kodeverk;

import javax.persistence.NoResultException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface KodeverkFeil extends DeklarerteFeil {

    KodeverkFeil FEILFACTORY = FeilFactory.create(KodeverkFeil.class);

    @TekniskFeil(feilkode = "FP-314678", feilmelding = "Kan ikke finne kodeverk for type '%s', kode '%s'", logLevel = LogLevel.WARN)
    Feil kanIkkeFinneKodeverk(String klassetype, String kode, NoResultException e);

    @TekniskFeil(feilkode = "FP-314679", feilmelding = "Kan ikke finne kodeverk for type '%s', kode '%s'", logLevel = LogLevel.WARN)
    Feil kanIkkeFinneKodeverkForOffisiellKode(String klassetype, String kode, NoResultException e);

    @TekniskFeil(feilkode = "FP-314680", feilmelding = "Kan ikke finne kodeverk for type '%s', kode '%s'", logLevel = LogLevel.WARN)
    Feil kanIkkeFinneKodeverkForKoder(String klassetype, String kode, NoResultException e);

}
