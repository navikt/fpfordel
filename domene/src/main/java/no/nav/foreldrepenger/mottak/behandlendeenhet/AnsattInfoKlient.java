package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import no.nav.vedtak.felles.integrasjon.ansatt.AbstractAnsattInfoKlient;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPTILGANG)
public class AnsattInfoKlient extends AbstractAnsattInfoKlient {
    private static final Logger LOG = LoggerFactory.getLogger(AnsattInfoKlient.class);

    private static final long CACHE_ELEMENT_LIVE_TIME_MS = TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS);
    private static final LRUCache<UUID, Set<AnsattGruppe>> GRUPPE_CACHE = new LRUCache<>(1000, CACHE_ELEMENT_LIVE_TIME_MS);


    public boolean medlemAvAnsattGruppe(AnsattGruppe gruppe) {
        var ansattOid = getCurrentAnsattOid();
        return medlemAvGrupper(ansattOid).contains(gruppe);
    }

    private Set<AnsattGruppe> medlemAvGrupper(UUID ansattOid) {
        if (ansattOid == null) {
            return Set.of();
        }
        var grupper = GRUPPE_CACHE.get(ansattOid);
        if (grupper == null) {
            var før = System.currentTimeMillis();
            grupper = super.alleGrupper(ansattOid);
            LOG.info("PROFIL Grupper. Tilgang bruker profil oppslag: {}ms. ", System.currentTimeMillis() - før);
            GRUPPE_CACHE.put(ansattOid, grupper);
        }
        return grupper;
    }

    private UUID getCurrentAnsattOid() {
        return KontekstHolder.getKontekst() instanceof RequestKontekst rk ? rk.getOid() : null;
    }

}
