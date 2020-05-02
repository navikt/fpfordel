package no.nav.foreldrepenger.mottak.journal;

import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringFerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostOppdateringIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovUgyldigInput;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

//TOD (HUMLE): Splitt i flere klasser, en for hver av bruksområdene, da kan også metodenavnene forkortes
public interface JournalFeil extends DeklarerteFeil {

    JournalFeil FACTORY = FeilFactory.create(JournalFeil.class);

    @ManglerTilgangFeil(feilkode = "FP-751834", feilmelding = "Mangler tilgang til å utføre '%s' mot Journalsystemet", logLevel = LogLevel.ERROR)
    Feil journalUtilgjengeligSikkerhetsbegrensning(String operasjon, Exception e);

    @IntegrasjonFeil(feilkode = "FP-195433", feilmelding = "Journalpost ikke funnet", logLevel = LogLevel.WARN)
    Feil hentJournalpostIkkeFunnet(HentJournalpostJournalpostIkkeFunnet e);

    @IntegrasjonFeil(feilkode = "FP-107540", feilmelding = "Journalpost ikke inngående", logLevel = LogLevel.WARN)
    Feil journalpostIkkeInngaaende(HentJournalpostJournalpostIkkeInngaaende e);

    @IntegrasjonFeil(feilkode = "FP-976237", feilmelding = "Utled journalføringsbehov ugyldig input", logLevel = LogLevel.WARN)
    Feil utledJournalfoeringsbehovUgyldigInput(UtledJournalfoeringsbehovUgyldigInput e);

    @IntegrasjonFeil(feilkode = "FP-204098", feilmelding = "Utled journalføringsbehov journalpost kan ikke behandles", logLevel = LogLevel.WARN)
    Feil utledJournalfoeringsbehovJournalpostKanIkkeBehandles(UtledJournalfoeringsbehovJournalpostKanIkkeBehandles e);

    @IntegrasjonFeil(feilkode = "FP-856651", feilmelding = "Utled journalføringsbehov journalpost ikke funnet", logLevel = LogLevel.WARN)
    Feil utledJournalfoeringsbehovJournalpostIkkeFunnet(UtledJournalfoeringsbehovJournalpostIkkeFunnet e);

    @IntegrasjonFeil(feilkode = "FP-729774", feilmelding = "Utled journalføringsbehov journalpost ikke inngaaende", logLevel = LogLevel.WARN)
    Feil utledJournalfoeringsbehovJournalpostIkkeInngaaende(UtledJournalfoeringsbehovJournalpostIkkeInngaaende e);

    @IntegrasjonFeil(feilkode = "FP-453958", feilmelding = "Journalføring ferdigstilling ikke mulig", logLevel = LogLevel.WARN)
    Feil journalfoeringFerdigstillingIkkeMulig(FerdigstillJournalfoeringFerdigstillingIkkeMulig e);

    @IntegrasjonFeil(feilkode = "FP-346800", feilmelding = "Ferdigstill journalføring journalpost ikke inngaaende", logLevel = LogLevel.WARN)
    Feil ferdigstillJournalfoeringJournalpostIkkeInngaaende(FerdigstillJournalfoeringJournalpostIkkeInngaaende e);

    @IntegrasjonFeil(feilkode = "FP-345598", feilmelding = "Ferdigstill journalføring ugyldig input", logLevel = LogLevel.WARN)
    Feil ferdigstillJournalfoeringUgyldigInput(FerdigstillJournalfoeringUgyldigInput e);

    @ManglerTilgangFeil(feilkode = "FP-009810", feilmelding = "Ferdigstill journalføring Sikkerhetsbegrensning", logLevel = LogLevel.WARN)
    Feil ferdigstillJournalfoeringSikkerhetsbegrensning(FerdigstillJournalfoeringSikkerhetsbegrensning e);

    @IntegrasjonFeil(feilkode = "FP-737540", feilmelding = "Ferdigstill journalføring objekt ikke funnet", logLevel = LogLevel.WARN)
    Feil ferdigstillJournalfoeringObjektIkkeFunnet(FerdigstillJournalfoeringObjektIkkeFunnet e);

    @IntegrasjonFeil(feilkode = "FP-179511", feilmelding = "Oppdater journalpost oppdatering ikke mulig", logLevel = LogLevel.WARN)
    Feil oppdaterJournalpostOppdateringIkkeMulig(OppdaterJournalpostOppdateringIkkeMulig e);

    @IntegrasjonFeil(feilkode = "FP-510264", feilmelding = "Oppdater journalpost ugyldig input", logLevel = LogLevel.WARN)
    Feil oppdaterJournalpostUgyldigInput(OppdaterJournalpostUgyldigInput e);

    @IntegrasjonFeil(feilkode = "FP-410513", feilmelding = "Oppdater journalpost, journalpost ikke inngaaende", logLevel = LogLevel.WARN)
    Feil oppdaterJournalpostJournalpostIkkeInngaaende(OppdaterJournalpostJournalpostIkkeInngaaende e);

    @IntegrasjonFeil(feilkode = "FP-936094", feilmelding = "Oppdater journalpost objekt ikke funnet", logLevel = LogLevel.WARN)
    Feil oppdaterJournalpostObjektIkkeFunnet(OppdaterJournalpostObjektIkkeFunnet e);

    @TekniskFeil(feilkode = "FP-871463", feilmelding = "Kunne ikke opprette tittel for forsendelseinformasjon for forsendelse: %s", logLevel = LogLevel.WARN)
    Feil kunneIkkeUtledeForsendelseTittel(String forsendelseId);
}
