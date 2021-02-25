package no.nav.foreldrepenger.mottak.task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.feilhåndtering.ÅpningstidForsinkelseStrategi;

@Dependent
public class OverførTilGsakMedÅpningstidFeilhåndteringsalgoritme extends OverførTilGsakFeilhåndteringsalgoritme {

    @Inject
    public OverførTilGsakMedÅpningstidFeilhåndteringsalgoritme(ProsessTaskRepository prosessTaskRepository) {
        super(new ÅpningstidForsinkelseStrategi(), prosessTaskRepository);
    }

    @Override
    public String kode() {
        return "TIL_GSAK_ÅPNINGSTID";
    }

}
