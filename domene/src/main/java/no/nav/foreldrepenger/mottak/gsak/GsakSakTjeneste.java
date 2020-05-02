package no.nav.foreldrepenger.mottak.gsak;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakUgyldigInput;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakRequest;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakResponse;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.sak.SakConsumer;
import no.nav.vedtak.felles.integrasjon.sak.SakSelftestConsumer;

@ApplicationScoped
public class GsakSakTjeneste {

    private SakConsumer sakConsumer;
    private SakSelftestConsumer sakSelftestConsumer;
    private SakRestKlient restKlient;

    public GsakSakTjeneste() {
    }

    @Inject
    public GsakSakTjeneste(SakConsumer sakConsumer,
                           SakSelftestConsumer sakSelftestConsumer,
                           SakRestKlient restKlient) {
        this.sakConsumer = sakConsumer;
        this.sakSelftestConsumer = sakSelftestConsumer;
        this.restKlient = restKlient;
    }

    public void ping() {
        sakSelftestConsumer.ping();
    }

    public List<GsakSak> finnSaker(String fnr) {

        FinnSakRequest request = new FinnSakRequest();
        Aktoer aktoer = new Person();
        aktoer.setIdent(fnr);
        request.setBruker(aktoer);

        FinnSakResponse response;
        try {
            response = sakConsumer.finnSak(request);
        } catch (FinnSakForMangeForekomster e) {
            throw GsakSakFeil.FACTORY.forMangeSakerFunnet(e).toException();
        } catch (FinnSakUgyldigInput e) {
            throw GsakSakFeil.FACTORY.ugyldigInput(e).toException();
        }

        return transformer(fnr, response.getSakListe());
    }

    public List<GsakSak> finnSakerRest(String aktørId) {
        return restKlient.finnSakListe(aktørId, Fagsystem.INFOTRYGD.getKode(), Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode());
    }

    private List<GsakSak> transformer(String fnr, List<Sak> sakListe) {
        return sakListe
                .stream()
                .map(sak -> tilGsak(fnr, sak))
                .collect(Collectors.toList());
    }

    private GsakSak tilGsak(String fnr, Sak sak) {
        Tema tema = Tema.fraOffisiellKode(sak.getFagomraade().getValue());
        String fagsystemOffisiellKode = sak.getFagsystem().getValue();
        Fagsystem fagsystem = Fagsystem.fraKodeDefaultUdefinert(fagsystemOffisiellKode);
        LocalDate sistEndret = DateUtil.convertToLocalDate(sak.getEndringstidspunkt());
        if (sistEndret == null) {
            sistEndret = DateUtil.convertToLocalDate(sak.getOpprettelsetidspunkt());
        }
        return new GsakSak(fnr, sak.getSakId(), tema, fagsystem, sistEndret);
    }

    private interface GsakSakFeil extends DeklarerteFeil {

        GsakSakTjeneste.GsakSakFeil FACTORY = FeilFactory.create(GsakSakTjeneste.GsakSakFeil.class);

        @TekniskFeil(feilkode = "FP-974567", feilmelding = "for mange saker funnet.", logLevel = LogLevel.ERROR)
        Feil forMangeSakerFunnet(FinnSakForMangeForekomster e);

        @TekniskFeil(feilkode = "FP-350721", feilmelding = "ugyldig input.", logLevel = LogLevel.ERROR)
        Feil ugyldigInput(FinnSakUgyldigInput e);

    }

}
