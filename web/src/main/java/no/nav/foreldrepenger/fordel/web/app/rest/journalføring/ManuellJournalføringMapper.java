package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.klient.*;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManuellJournalføringMapper {
    private ManuellJournalføringMapper() {
        //sonar
    }

    private static final Logger LOG = LoggerFactory.getLogger(ManuellJournalføringMapper.class);

    static FamilihendelseTypeJFDto mapHendelseTypeJF(FamilieHendelseTypeDto familihendelseType) {
        return switch (familihendelseType) {
            case TERMIN -> FamilihendelseTypeJFDto.TERMIN;
            case FØDSEL -> FamilihendelseTypeJFDto.FØDSEL;
            case ADOPSJON -> FamilihendelseTypeJFDto.ADOPSJON;
            case OMSORG -> FamilihendelseTypeJFDto.OMSORG;
        };
    }

    static YtelseTypeDto mapYtelseTypeTilDto(FagsakYtelseTypeDto ytelseType) {
        if (null == ytelseType) {
            return null;
        }
        return switch (ytelseType) {
            case FORELDREPENGER -> YtelseTypeDto.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> YtelseTypeDto.SVANGERSKAPSPENGER;
            case ENGANGSTØNAD -> YtelseTypeDto.ENGANGSTØNAD;
        };
    }

    static FagsakYtelseTypeDto mapYtelseTypeFraDto(YtelseTypeDto ytelseTypeDto) {
        if (null == ytelseTypeDto) {
            return null;
        }
        return switch (ytelseTypeDto) {
            case FORELDREPENGER -> FagsakYtelseTypeDto.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> FagsakYtelseTypeDto.SVANGERSKAPSPENGER;
            case ENGANGSTØNAD -> FagsakYtelseTypeDto.ENGANGSTØNAD;
        };
    }

    static StatusDto mapFagsakStatusTilStatusDto(FagsakStatusDto fagsakStatusDto) {
        if (null == fagsakStatusDto) {
            return null;
        }
        return switch (fagsakStatusDto) {
            case UNDER_BEHANDLING -> StatusDto.UNDER_BEHANDLING;
            case LØPENDE -> StatusDto.LØPENDE;
            case AVSLUTTET -> StatusDto.AVSLUTTET;
        };
    }

    static JournalføringRestTjeneste.OppgavePrioritet mapPrioritet(Prioritet prioritet) {
        return switch (prioritet) {
            case HOY -> JournalføringRestTjeneste.OppgavePrioritet.HØY;
            case LAV -> JournalføringRestTjeneste.OppgavePrioritet.LAV;
            case NORM -> JournalføringRestTjeneste.OppgavePrioritet.NORM;
        };
    }

    static YtelseTypeDto mapTilYtelseType(String behandlingstema) {
        LOG.info("FPFORDEL JOURNALFØRING Oppgave med behandlingstema {}", behandlingstema);
        var behandlingTemaMappet = BehandlingTema.fraOffisiellKode(behandlingstema);
        LOG.info("FPFORDEL JOURNALFØRING Fant oppgave med behandlingTemaMappet {}", behandlingTemaMappet);

        return switch (behandlingTemaMappet) {
            case FORELDREPENGER, FORELDREPENGER_ADOPSJON, FORELDREPENGER_FØDSEL -> YtelseTypeDto.FORELDREPENGER;
            case SVANGERSKAPSPENGER -> YtelseTypeDto.SVANGERSKAPSPENGER;
            case ENGANGSSTØNAD, ENGANGSSTØNAD_ADOPSJON, ENGANGSSTØNAD_FØDSEL -> YtelseTypeDto.ENGANGSTØNAD;
            default -> null;
        };
    }

    static JournalpostDetaljerDto.SakJournalføringDto mapSakJournalføringDto(SakInfoDto sakInfoDto) {
        var familihendelseJFDto =
            sakInfoDto.familiehendelseInfoDto() != null ? new JournalpostDetaljerDto.SakJournalføringDto.FamilieHendelseJournalføringDto(
                sakInfoDto.familiehendelseInfoDto().familiehendelseDato(),
                ManuellJournalføringMapper.mapHendelseTypeJF(sakInfoDto.familiehendelseInfoDto().familihendelseType())) : null;

        return new JournalpostDetaljerDto.SakJournalføringDto(sakInfoDto.saksnummer().getSaksnummer(), mapYtelseTypeTilDto(sakInfoDto.ytelseType()),
            sakInfoDto.opprettetDato(), mapFagsakStatusTilStatusDto(sakInfoDto.status()), familihendelseJFDto, sakInfoDto.førsteUttaksdato());
    }
}
