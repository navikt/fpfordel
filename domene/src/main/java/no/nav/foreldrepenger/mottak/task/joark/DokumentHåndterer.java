package no.nav.foreldrepenger.mottak.task.joark;

import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;

abstract class DokumentHåndterer {
    abstract MottakMeldingDataWrapper håndterDokument(MottakMeldingDataWrapper dataWrapper);
}
