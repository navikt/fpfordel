package no.nav.foreldrepenger.mottak.infotrygd;

import java.time.LocalDate;
import java.util.List;

public interface InfotrygdTjeneste {
    List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom);

}
