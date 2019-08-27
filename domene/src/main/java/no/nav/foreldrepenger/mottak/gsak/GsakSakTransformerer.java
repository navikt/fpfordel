package no.nav.foreldrepenger.mottak.gsak;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Sak;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;

@ApplicationScoped
class GsakSakTransformerer {

    private KodeverkRepository kodeverkRepository;

    @Inject
    GsakSakTransformerer(KodeverkRepository kodeverkRepository) {
        this.kodeverkRepository = kodeverkRepository;
    }

    GsakSakTransformerer() {
        // for CDI
    }

    List<GsakSak> transformer(String fnr, List<Sak> sakListe) {
        return sakListe
                .stream()
                .map(sak -> tilGsak(fnr, sak))
                .collect(Collectors.toList());
    }

    GsakSak tilGsak(String fnr, Sak sak) {
        String temaOffisiellKode = sak.getFagomraade().getValue();
        Tema tema = kodeverkRepository.finnForKodeverkEiersKode(Tema.class, temaOffisiellKode, Tema.UDEFINERT);
        String fagsystemOffisiellKode = sak.getFagsystem().getValue();
        Fagsystem fagsystem = kodeverkRepository.finnForKodeverkEiersKode(Fagsystem.class, fagsystemOffisiellKode, Fagsystem.UDEFINERT);
        LocalDate sistEndret = DateUtil.convertToLocalDate(sak.getEndringstidspunkt());
        if (sistEndret == null) {
            sistEndret = DateUtil.convertToLocalDate(sak.getOpprettelsetidspunkt());
        }
        return new GsakSak(fnr, sak.getSakId(), tema, fagsystem, sistEndret);
    }
}
