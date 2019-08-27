package no.nav.foreldrepenger.fordel.konfig;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverk.KonfigVerdiGruppe;

/**
 * Henter konfigurerbare verdier som er håndtert i databasen.
 * <p>
 * Bruk normalt {@link KonfigVerdi} for å injisere en konfigurert verdi, og unngå å kalle her direkte, med mindre det er
 * spesielle behov ang. f.eks. dato-angivelse. Den tar også hensyn til hvorvidet det finnes system properties som
 * overstyrer databaseverdier.
 */
public interface KonfigVerdiRepository {

    /**
     * Hent en spesifikk kode verdi for en gitt gruppe og dato
     *
     * @param gruppe - spesifiser en gruppe, eventuelt {@link KonfigVerdiGruppe#INGEN_GRUPPE}
     * @param kode   - spesifikk kode
     * @param dato   - dato når verdien skal være gyldig for. Må alltid spesifiseres.
     * @return Optional med verdi hvis finnes.
     */
    Optional<KonfigVerdiEntitet> finnVerdiFor(KonfigVerdiGruppe gruppe, String kode, LocalDate dato);

    /**
     * Hent alle verdier gyldig for en gitt gruppe på en angitt dato.
     *
     * @param gruppe - spesifiser en gruppe, eventuelt {@link KonfigVerdiGruppe#INGEN_GRUPPE}
     * @param dato   - dato når verdien skal være gyldig for. Må alltid spesifiseres.
     * @return list av verdier gyldig for angitt gruppe og dato. Tom hvis ingen finnes.
     */
    List<KonfigVerdiEntitet> finnVerdierFor(KonfigVerdiGruppe gruppe, LocalDate dato);

    /**
     * Hent alle verdier gyldig for en angitt dato.
     *
     * @return list av alle {@link KonfigVerdiEntitet} gyldig.
     */
    List<KonfigVerdiEntitet> finnAlleVerdier(LocalDate dato);

}
