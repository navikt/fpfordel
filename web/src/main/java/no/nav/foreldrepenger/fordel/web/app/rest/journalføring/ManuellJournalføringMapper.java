package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import no.nav.foreldrepenger.mottak.klient.FagsakYtelseTypeDto;

import no.nav.foreldrepenger.mottak.klient.FagsakStatusDto;

import no.nav.foreldrepenger.mottak.klient.SakInfoDto;
import no.nav.foreldrepenger.mottak.klient.StatusDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.klient.FamilieHendelseTypeDto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;

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

    static YtelseTypeDto mapYtelseTypeTilDto(FagsakYtelseTypeDto fagsakYtelseTypeDto) {
        if (null == fagsakYtelseTypeDto) {
            return null;
        }
        return switch (fagsakYtelseTypeDto) {
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

    static String tekstFraBeskrivelse(String beskrivelse) {
        if (beskrivelse == null) {
            return "Journalføring";
        }
        //Når vi oppretter gosys oppgave avsluttes teksten med (dd.mm.yyyy)
        int i = beskrivelse.length();
        if (beskrivelse.charAt(i - 1) == ')') {
            i = i - 12;
        }

        while (i > 0 && !(Character.isDigit(beskrivelse.charAt(i - 1)) || beskrivelse.charAt(i - 1) == ',' || beskrivelse.charAt(i - 1) == '*'
            || beskrivelse.charAt(i - 1) == '>')) {
            i--;
        }

        if (i < beskrivelse.length() && beskrivelse.charAt(i) == ' ') {
            i++;
        }

        if (i == beskrivelse.length()) {
            return beskrivelse;
        }
        //I tilfelle vi tar bort for mye
        if (beskrivelse.substring(i).length() < 2) {
            var i2 = beskrivelse.length();
            while (i2 > 0 && (beskrivelse.charAt(i2 - 1) != ',')) {
                i2--;
            }
            return beskrivelse.substring(i2);
        }
        return beskrivelse.substring(i);
    }

    static ManuellJournalføringRestTjeneste.OppgavePrioritet mapPrioritet(Prioritet prioritet) {
        return switch (prioritet) {
            case HOY -> ManuellJournalføringRestTjeneste.OppgavePrioritet.HØY;
            case LAV -> ManuellJournalføringRestTjeneste.OppgavePrioritet.LAV;
            case NORM -> ManuellJournalføringRestTjeneste.OppgavePrioritet.NORM;
        };
    }
    static boolean harJournalpostMangler(String beskrivelse) {
        return beskrivelse.startsWith("Journalføring");
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
        var familihendelseJFDto = sakInfoDto.familiehendelseInfoDto() != null ?
            new JournalpostDetaljerDto.SakJournalføringDto.FamilieHendelseJournalføringDto(sakInfoDto.familiehendelseInfoDto().familiehendelseDato(), ManuellJournalføringMapper.mapHendelseTypeJF(sakInfoDto.familiehendelseInfoDto().familihendelseType())) : null;
        return new JournalpostDetaljerDto.SakJournalføringDto(sakInfoDto.saksnummer().getSaksnummer(), mapYtelseTypeTilDto(sakInfoDto.fagsakYtelseTypeDto()), sakInfoDto.opprettetDato(), mapFagsakStatusTilStatusDto(sakInfoDto.status()), familihendelseJFDto,sakInfoDto.førsteUttaksdato());
    }
}
