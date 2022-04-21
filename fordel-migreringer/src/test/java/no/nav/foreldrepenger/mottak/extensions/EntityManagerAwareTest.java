package no.nav.foreldrepenger.mottak.extensions;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(JpaExtension.class)
public abstract class EntityManagerAwareTest extends no.nav.vedtak.felles.testutilities.db.EntityManagerAwareTest {
}
