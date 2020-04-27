package no.nav.foreldrepenger.mottak.queue;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.XMLForsendelsesinformasjon;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ApplicationScoped
@ActivateRequestContext
@Transactional
public class ProsesstaskMeldingsfordeler implements MeldingsFordeler {

    private static final Logger LOG = LoggerFactory.getLogger(ProsesstaskMeldingsfordeler.class);

    private ProsessTaskRepository prosessTaskRepository;
    private DokumentRepository dokumentRepository;

    ProsesstaskMeldingsfordeler() {
    }

    @Inject
    public ProsesstaskMeldingsfordeler(ProsessTaskRepository prosessTaskRepository,
            DokumentRepository dokumentRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void execute(String correlationId, XMLForsendelsesinformasjon forsendelsesinfo) {
        final String arkivId = forsendelsesinfo.getArkivId();
        final Tema tema = Tema.fraOffisiellKode(forsendelsesinfo.getTema().getValue());

        // I påvente av MMA-4323
        if (Tema.OMS.equals(tema)) {
            LOG.info("FPFORDEL ignorerer journalpost {} med tema OMS", arkivId);
            return;
        }

        LOG.info("FPFORDEL Mottatt og ignorert MQ journalpost {}", arkivId);

    }
}
