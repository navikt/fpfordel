package no.nav.foreldrepenger.mottak.journal.saf;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import no.nav.saf.Journalposttype;
import no.nav.saf.Journalstatus;
import no.nav.saf.Kanal;
import no.nav.saf.Tema;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.AvsenderMottaker;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Bruker;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Dokumentvariant;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Sak;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Tilleggsopplysning;

class SafMapper {

    private SafMapper() {

    }

    static List<Journalpost> mapJP(List<no.nav.saf.Journalpost> p) {
        return safeStream(p).map(SafMapper::map).toList();
    }

    static Journalpost map(no.nav.saf.Journalpost res) {
        return new Journalpost(res.getJournalpostId(), map(res.getJournalposttype()), map(res.getJournalstatus()), map(res.getDatoOpprettet()),
            res.getTittel(), map(res.getKanal()), map(res.getTema()), res.getBehandlingstema(), res.getJournalfoerendeEnhet(),
            res.getEksternReferanseId(), map(res.getBruker()), map(res.getAvsenderMottaker()), map(res.getSak()),
            mapTO(res.getTilleggsopplysninger()), map(res.getDokumenter()));
    }

    private static String map(Tema t) {
        return Optional.ofNullable(t).map(Tema::name).orElse(null);
    }

    private static String map(Kanal k) {
        return Optional.ofNullable(k).map(Kanal::name).orElse(null);
    }

    private static String map(Journalstatus s) {
        return Optional.ofNullable(s).map(Journalstatus::name).orElse(null);
    }

    private static String map(Journalposttype t) {
        return Optional.ofNullable(t).map(Journalposttype::name).orElse(null);
    }

    private static List<Tilleggsopplysning> mapTO(List<no.nav.saf.Tilleggsopplysning> tilleggsopplysninger) {
        return safeStream(tilleggsopplysninger).map(t -> new Tilleggsopplysning(t.getNokkel(), t.getVerdi())).toList();
    }

    private static List<DokumentInfo> map(List<no.nav.saf.DokumentInfo> dokumenter) {
        return safeStream(dokumenter).map(SafMapper::map).toList();
    }

    private static DokumentInfo map(no.nav.saf.DokumentInfo info) {
        return Optional.ofNullable(info)
            .map(d -> new DokumentInfo(d.getDokumentInfoId(), d.getTittel(), d.getBrevkode(), mapLV(d.getLogiskeVedlegg()),
                mapDV(d.getDokumentvarianter())))
            .orElse(null);
    }

    private static List<Dokumentvariant> mapDV(List<no.nav.saf.Dokumentvariant> dv) {
        return safeStream(dv).map(SafMapper::map).toList();
    }

    private static Dokumentvariant map(no.nav.saf.Dokumentvariant dv) {
        return Optional.ofNullable(dv).map(d -> new Dokumentvariant(map(d.getVariantformat()), null, new byte[]{})).orElse(null);
    }

    private static Dokumentvariant.Variantformat map(no.nav.saf.Variantformat vf) {
        try {
            return Optional.ofNullable(vf).map(v -> v.name()).map(Dokumentvariant.Variantformat::valueOf).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private static List<DokumentInfo.LogiskVedlegg> mapLV(List<no.nav.saf.LogiskVedlegg> v) {
        return safeStream(v).map(SafMapper::map).toList();
    }

    private static DokumentInfo.LogiskVedlegg map(no.nav.saf.LogiskVedlegg vedlegg) {
        return Optional.ofNullable(vedlegg).map(v -> v.getTittel()).map(DokumentInfo.LogiskVedlegg::new).orElse(null);
    }

    private static Sak map(no.nav.saf.Sak sak) {
        return Optional.ofNullable(sak).map(s -> new Sak(s.getFagsakId(), s.getFagsaksystem(), Sak.Sakstype.FAGSAK)).orElse(null);
    }

    private static Bruker map(no.nav.saf.Bruker bruker) {
        return Optional.ofNullable(bruker).map(b -> new Bruker(b.getId(), map(b.getType()))).orElse(null);
    }

    private static AvsenderMottaker map(no.nav.saf.AvsenderMottaker am) {
        return Optional.ofNullable(am).map(a -> new AvsenderMottaker(a.getId(), map(a.getType()), a.getNavn())).orElse(null);
    }

    private static AvsenderMottaker.AvsenderMottakerIdType map(no.nav.saf.AvsenderMottakerIdType type) {
        try {
            return Optional.ofNullable(type)
                .map(no.nav.saf.AvsenderMottakerIdType::name)
                .map(AvsenderMottaker.AvsenderMottakerIdType::valueOf)
                .orElse(null);
        } catch (Exception e) {
            return AvsenderMottaker.AvsenderMottakerIdType.UKJENT;
        }
    }

    private static LocalDateTime map(Date date) {
        return Optional.ofNullable(date).map(d -> LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault())).orElse(null);
    }

    private static Bruker.BrukerIdType map(no.nav.saf.BrukerIdType type) {

        try {
            return Optional.ofNullable(type).map(t -> t.name()).map(Bruker.BrukerIdType::valueOf).orElse(null);
        } catch (Exception e) {
            return Bruker.BrukerIdType.UKJENT;
        }
    }

    private static <T> Stream<T> safeStream(List<T> list) {
        return Optional.ofNullable(list).orElseGet(Collections::emptyList).stream();
    }
}
