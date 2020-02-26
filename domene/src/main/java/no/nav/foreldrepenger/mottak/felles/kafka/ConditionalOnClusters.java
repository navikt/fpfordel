package no.nav.foreldrepenger.mottak.felles.kafka;

import no.nav.vedtak.util.env.Cluster;

public @interface ConditionalOnClusters {

    Cluster[] clusters();

}
