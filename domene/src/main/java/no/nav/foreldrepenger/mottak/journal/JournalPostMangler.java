package no.nav.foreldrepenger.mottak.journal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JournalPostMangler {

    private List<JournalMangel> journalMangler = new ArrayList<>();

    public void leggTilJournalMangel(JournalMangelType mangelType, boolean harJournalfoeringsbehov) {
        journalMangler.add(new JournalMangel(mangelType, harJournalfoeringsbehov));
    }

    public boolean harMangler() {
        return journalMangler.stream().anyMatch(JournalMangel::harJournalfoeringsbehov);
    }

    public List<JournalMangelType> getMangler() {
        return journalMangler.stream().filter(JournalMangel::harJournalfoeringsbehov).map(JournalMangel::getMangeltype)
                .collect(Collectors.toList());
    }

    public Optional<JournalMangel> getMangel(JournalMangelType mangelType) {
        return journalMangler.stream().filter(m -> m.getMangeltype() == mangelType)
                .filter(JournalMangel::harJournalfoeringsbehov).findFirst();
    }

    public void rettetMangel(JournalMangelType mangelType) {
        getMangel(mangelType).orElseThrow(() -> new IllegalArgumentException()).rettetMangel();
    }

    static class JournalMangel {
        private JournalMangelType mangeltype;
        private boolean harJournalfoeringsbehov;

        JournalMangel(JournalMangelType mangeltype, boolean harJournalfoeringsbehov) {
            this.mangeltype = mangeltype;
            this.harJournalfoeringsbehov = harJournalfoeringsbehov;
        }

        JournalMangelType getMangeltype() {
            return mangeltype;
        }

        boolean harJournalfoeringsbehov() {
            return harJournalfoeringsbehov;
        }

        void rettetMangel() {
            this.harJournalfoeringsbehov = false;
        }
    }

    public enum JournalMangelType {
        AVSENDERID,
        AVSENDERNAVN,
        ARKIVSAK,
        INNHOLD,
        TEMA,
        BRUKER,
        HOVEDOK_KATEGORI,
        HOVEDOK_TITTEL,
        VEDLEGG_KATEGORI,
        VEDLEGG_TITTEL
    }
}
