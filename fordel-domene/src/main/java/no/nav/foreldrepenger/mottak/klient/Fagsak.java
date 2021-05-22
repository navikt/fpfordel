package no.nav.foreldrepenger.mottak.klient;

import java.util.Optional;

import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;

public interface Fagsak {

    Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto);

    SaksnummerDto opprettSak(OpprettSakDto opprettSakDto);

    void knyttSakOgJournalpost(JournalpostKnyttningDto journalpostKnyttningDto);

    VurderFagsystemResultat vurderFagsystem(MottakMeldingDataWrapper w);

}
