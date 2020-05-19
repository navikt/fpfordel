package no.nav.foreldrepenger.mottak.journal;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

//TOD (HUMLE): Splitt i flere klasser, en for hver av bruksområdene, da kan også metodenavnene forkortes
public interface JournalFeil extends DeklarerteFeil {

    JournalFeil FACTORY = FeilFactory.create(JournalFeil.class);

    @TekniskFeil(feilkode = "FP-871463", feilmelding = "Kunne ikke opprette tittel for forsendelseinformasjon for forsendelse: %s", logLevel = LogLevel.WARN)
    Feil kunneIkkeUtledeForsendelseTittel(String forsendelseId);
}
