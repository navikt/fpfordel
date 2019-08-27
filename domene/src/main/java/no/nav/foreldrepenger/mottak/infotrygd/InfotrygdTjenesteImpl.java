package no.nav.foreldrepenger.mottak.infotrygd;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Periode;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeRequest;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeResponse;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakConsumer;

@ApplicationScoped
public class InfotrygdTjenesteImpl implements InfotrygdTjeneste {
    static final String TJENESTE = "InfotrygdSak";
    private static final String INFOTRYGD_NEDE_EXCEPTION_TEXT = "Basene i Infotrygd er ikke tilgjengelige";
    private InfotrygdSakConsumer infotrygdSakConsumer;

    InfotrygdTjenesteImpl() {
        // CDI
    }

    @Inject
    public InfotrygdTjenesteImpl(InfotrygdSakConsumer infotrygdSakConsumer) {
        this.infotrygdSakConsumer = infotrygdSakConsumer;
    }

    @Override
    public List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom) {
        return mapInfotrygdResponseToInfotrygdSak(finnSakListeFull(fnr, fom));
    }

    private List<InfotrygdSak> mapInfotrygdResponseToInfotrygdSak(FinnSakListeResponse response) {
        List<InfotrygdSak> saker = new ArrayList<>();
        if (response != null) {
            saker.addAll(response.getSakListe().stream().map(this::mapInfotrygdSakTilDto).collect(Collectors.toList()));
            saker.addAll(response.getVedtakListe().stream().map(this::mapInfotrygdVedtakTilDto).collect(Collectors.toList()));
        }
        return saker;
    }

    private InfotrygdSak mapInfotrygdVedtakTilDto(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak vedtak) {
        String tema = vedtak.getTema() == null ? null : vedtak.getTema().getValue();
        String behandlingTema = vedtak.getBehandlingstema() == null ? null : vedtak.getBehandlingstema().getValue();
        LocalDate registrert = vedtak.getRegistrert().toGregorianCalendar().toZonedDateTime().toLocalDate();
        LocalDate iverksatt = vedtak.getIverksatt().toGregorianCalendar().toZonedDateTime().toLocalDate();
        return new InfotrygdSak(vedtak.getSakId(), tema, behandlingTema, iverksatt, registrert);
    }

    private InfotrygdSak mapInfotrygdSakTilDto(no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak sak) {
        String tema = sak.getTema() == null ? null : sak.getTema().getValue();
        String behandlingTema = sak.getBehandlingstema() == null ? null : sak.getBehandlingstema().getValue();
        LocalDate registrert = sak.getRegistrert().toGregorianCalendar().toZonedDateTime().toLocalDate();
        LocalDate iverksatt = null;
        if (sak.getIverksatt() != null) {
            iverksatt = sak.getIverksatt().toGregorianCalendar().toZonedDateTime().toLocalDate();
        }
        return new InfotrygdSak(sak.getSakId(), tema, behandlingTema, iverksatt, registrert);
    }

    private FinnSakListeResponse finnSakListeFull(String fnr, LocalDate fom) {
        FinnSakListeRequest request = new FinnSakListeRequest();
        Periode periode = new Periode();
        try {
            periode.setFom(DateUtil.convertToXMLGregorianCalendar(fom));
            periode.setTom(DateUtil.convertToXMLGregorianCalendar(LocalDate.of(9999, Month.DECEMBER, 31)));
            request.setPeriode(periode);
            request.setPersonident(fnr);
            return infotrygdSakConsumer.finnSakListe(request);
        } catch (FinnSakListePersonIkkeFunnet e) {
            throw InfotrygdFeil.FACTORY.personIkkeFunnet(TJENESTE, e).toException();
        } catch (FinnSakListeUgyldigInput e) {
            throw InfotrygdFeil.FACTORY.ugyldigInput(TJENESTE, e).toException();
        } catch (FinnSakListeSikkerhetsbegrensning e) {
            throw InfotrygdFeil.FACTORY.tjenesteUtilgjengeligSikkerhetsbegrensning(TJENESTE, e).toException();
        } catch (IntegrasjonException e) {
            if (e.getFeil().getFeilmelding().contains(INFOTRYGD_NEDE_EXCEPTION_TEXT)) {
                throw InfotrygdFeil.FACTORY.nedetid(TJENESTE, e).toException();
            } else {
                throw e;
            }
        }
    }
}
