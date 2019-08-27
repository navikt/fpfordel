package no.nav.foreldrepenger.fordel.kodeverk;

import java.util.List;
import java.util.Optional;

/**
 * Få tilgang til kodeverk.
 */
public interface KodeverkRepository {

    /**
     * Finn instans av Kodeliste innslag for angitt kode verdi.
     */
    <V extends Kodeliste> V finn(Class<V> cls, String kode);

    /**
     * Finn instans av Kodeliste innslag for angitt Kodeliste.
     * For oppslag av fulle instanser fra de ufullstendige i hver konkrete subklasse av Kodeliste.
     */
    <V extends Kodeliste> V finn(Class<V> cls, V kodelisteKonstant);

    /**
     * Finn instans av Kodeliste innslag for angitt offisiell kode verdi.
     */
    <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode);

    /**
     * Finn instans av Kodeliste innslag for angitt offisiell kode verdi, eller en default value hvis offisiell kode ikke git treff.
     */
    <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode, V defaultValue);

    /**
     * Finn instans av Kodeliste innslag for angitt offisielt termnavn, eller en default value hvis offisielt termnavn ikke gir treff.
     */
    <V extends Kodeliste> V finnForKodeverkEiersTermNavn(Class<V> cls, String termNavn, V defaultValue);


    /** Finn instans av Kodeliste innslag for angitt offisiell kode verdi. */
    <V extends Kodeliste> List<V> finnForKodeverkEiersKoder(Class<V> cls, String... offisiellKoder);

    /** Hent alle innslag for en gitt kodeliste og gitte koder. */
    <V extends Kodeliste> List<V> finnListe(Class<V> cls, List<String> koder);

    /** Hent alle innslag for en gitt Kodeliste. */
    <V extends Kodeliste> List<V> hentAlle(Class<V> cls);

    /** Finn kode, return er optional empty hvis ikke finnes. */
    <V extends Kodeliste> Optional<V> finnOptional(Class<V> cls, String kode);
}
