package no.nav.foreldrepenger.fordel.web.server.abac;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.sikkerhet.abac.LegacyTokenProvider;
import no.nav.vedtak.sikkerhet.abac.AbacAuditlogger;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.PepImpl;

@Default
@Alternative
@Priority(Interceptor.Priority.APPLICATION + 1)
public class AppPepImpl extends PepImpl {

    AppPepImpl() {
    }

    @Inject
    public AppPepImpl(LokalPdpKlientImpl pdpKlient,
                      PdpRequestBuilder pdpRequestBuilder,
                      AbacAuditlogger sporingslogg,
                      @KonfigVerdi(value = "pip.users", required = false) String pipUsers) {
        super(pdpKlient, new LegacyTokenProvider() ,pdpRequestBuilder, sporingslogg, pipUsers);
    }

}
