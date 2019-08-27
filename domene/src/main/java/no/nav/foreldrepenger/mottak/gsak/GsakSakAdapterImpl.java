package no.nav.foreldrepenger.mottak.gsak;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSakAdapter;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakUgyldigInput;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakRequest;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakResponse;
import no.nav.vedtak.felles.integrasjon.sak.SakConsumer;
import no.nav.vedtak.felles.integrasjon.sak.SakSelftestConsumer;

@ApplicationScoped
class GsakSakAdapterImpl implements GsakSakAdapter {

    private SakConsumer sakConsumer;
    private SakSelftestConsumer sakSelftestConsumer;
    private GsakSakTransformerer oversetter;

    public GsakSakAdapterImpl() {
    }

    @Inject
    public GsakSakAdapterImpl(SakConsumer sakConsumer,
                              SakSelftestConsumer sakSelftestConsumer,
                              GsakSakTransformerer oversetter) {
        this.sakConsumer = sakConsumer;
        this.sakSelftestConsumer = sakSelftestConsumer;
        this.oversetter = oversetter;
    }

    @Override
    public void ping() {
        sakSelftestConsumer.ping();
    }

    @Override
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

        return oversetter.transformer(fnr, response.getSakListe());
    }
}
