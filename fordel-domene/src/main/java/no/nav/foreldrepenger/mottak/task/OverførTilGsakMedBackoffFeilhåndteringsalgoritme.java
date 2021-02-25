package no.nav.foreldrepenger.mottak.task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.impl.feilhåndtering.BackoffFeilhåndteringStrategi;

@Dependent
public class OverførTilGsakMedBackoffFeilhåndteringsalgoritme extends OverførTilGsakFeilhåndteringsalgoritme {

    @Inject
    public OverførTilGsakMedBackoffFeilhåndteringsalgoritme(ProsessTaskRepository prosessTaskRepository) {
        super(new BackoffFeilhåndteringStrategi(), prosessTaskRepository);
    }

    @Override
    public String kode() {
        return "TIL_GSAK_BACKOFF";
    }

}
