package no.nav.foreldrepenger.mottak.person;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;
import no.nav.vedtak.felles.integrasjon.pdl.Tema;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class AktørTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(AktørTjeneste.class);

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(8, TimeUnit.HOURS);

    private LRUCache<String, Optional<String>> cacheAktørIdTilIdent;
    private LRUCache<String, Optional<String>> cacheIdentTilAktørId;

    private AktørConsumer aktørConsumer;
    private PdlKlient pdlKlient;

    AktørTjeneste() {
        // CDI
    }

    @Inject
    public AktørTjeneste(PdlKlient pdlKlient,
                         AktørConsumer aktørConsumer) {
        this.aktørConsumer = aktørConsumer;
        this.pdlKlient = pdlKlient;
        this.cacheAktørIdTilIdent = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
        this.cacheIdentTilAktørId = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public Optional<String> hentAktørIdForPersonIdent(String personIdent) {
        Optional<String> fraCache = cacheIdentTilAktørId.get(personIdent);
        if (fraCache != null) { //NOSONAR trenger null-sjekk selv om bruker optional. Null betyr "finnes ikke i cache". Optional.empty betyr "finnes ikke i TPS"
            return fraCache;
        }
        Optional<String> aktørId = aktørConsumer.hentAktørIdForPersonIdent(personIdent);
        aktørId.ifPresent(a -> hentAktørIdFraPDL(personIdent, a));
        cacheIdentTilAktørId.put(personIdent, aktørId);
        return aktørId;
    }

    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        Optional<String> fraCache = cacheAktørIdTilIdent.get(aktørId);
        if (fraCache != null) { //NOSONAR trenger null-sjekk selv om bruker optional. Null betyr "finnes ikke i cache". Optional.empty betyr "finnes ikke i TPS"
            return fraCache;
        }
        Optional<String> ident = aktørConsumer.hentPersonIdentForAktørId(aktørId);
        ident.ifPresent(i -> hentPersonIdentFraPDL(aktørId, i));
        cacheAktørIdTilIdent.put(aktørId, ident);
        return ident;
    }

    public void hentAktørIdFraPDL(String fnr, String aktørFraConsumer) {
        try {
            var request = new HentIdenterQueryRequest();
            request.setIdent(fnr);
            request.setGrupper(List.of(IdentGruppe.AKTORID));
            request.setHistorikk(Boolean.FALSE);
            var projection = new IdentlisteResponseProjection()
                    .identer(new IdentInformasjonResponseProjection().ident());
            var identliste = pdlKlient.hentIdenter(request, projection, Tema.FOR);
            int antall = identliste.getIdenter().size();
            var aktørId = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).orElse(null);
            if (antall == 1 && Objects.equals(aktørFraConsumer, aktørId)) {
                LOG.info("FPFORDEL PDL AKTØRID: like aktørid");
            } else if (antall != 1 && Objects.equals(aktørFraConsumer, aktørId)) {
                LOG.info("FPFORDEL PDL AKTØRID: ulikt antall aktørid {}", antall);
            } else {
                LOG.info("FPFORDEL PDL AKTØRID: ulike aktørid TPS {} og PDL {} antall {}", aktørFraConsumer, aktørId, antall);
            }
        } catch (Exception e) {
            LOG.info("FPFORDEL PDL AKTØRID hentaktørid error", e);
        }
    }

    public void hentPersonIdentFraPDL(String aktørId, String identFraConsumer) {
        try {
            var request = new HentIdenterQueryRequest();
            request.setIdent(aktørId);
            request.setGrupper(List.of(IdentGruppe.FOLKEREGISTERIDENT));
            request.setHistorikk(Boolean.FALSE);
            var projection = new IdentlisteResponseProjection()
                    .identer(new IdentInformasjonResponseProjection().ident());
            var identliste = pdlKlient.hentIdenter(request, projection, Tema.FOR);
            int antall = identliste.getIdenter().size();
            var fnr = identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent).orElse(null);
            if (antall == 1 && Objects.equals(identFraConsumer, fnr)) {
                LOG.info("FPFORDEL PDL AKTØRID: like identer");
            } else if (antall != 1 && Objects.equals(identFraConsumer, fnr)) {
                LOG.info("FPFORDEL PDL AKTØRID: ulikt antall identer {}", antall);
            } else {
                LOG.info("FPFORDEL PDL AKTØRID: ulike identer TPS {} og PDL {} antall {}", identFraConsumer, fnr, antall);
            }
        } catch (Exception e) {
            LOG.info("FPFORDEL PDL AKTØRID hentident error", e);
        }
    }

}
