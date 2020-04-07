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

    public void leggTilJournalMangel(JournalMangelType mangelType, String dokumentId, boolean harJournalfoeringsbehov) {
        journalMangler.add(new JournalMangel(mangelType, dokumentId, harJournalfoeringsbehov));
    }

    public boolean harMangler() {
        return journalMangler.stream().anyMatch(JournalMangel::harJournalfoeringsbehov);
    }

    public List<JournalMangelType> getMangelTyper() {
        return journalMangler.stream().filter(JournalMangel::harJournalfoeringsbehov).map(JournalMangel::getMangeltype)
                .collect(Collectors.toList());
    }

    public List<JournalMangel> getMangler() {
        return journalMangler.stream().filter(JournalMangel::harJournalfoeringsbehov).collect(Collectors.toList());
    }

    public Optional<JournalMangel> getMangel(JournalMangelType mangelType) {
        return journalMangler.stream().filter(m -> m.getMangeltype() == mangelType)
                .filter(JournalMangel::harJournalfoeringsbehov).findFirst();
    }

    public void rettetMangel(JournalMangel mangel) {
        getMangel(mangel.getMangeltype()).orElseThrow(IllegalArgumentException::new).rettetMangel();
    }

    public static class JournalMangel {
        private JournalMangelType mangeltype;
        private String dokumentId;
        private boolean harJournalfoeringsbehov;

        public JournalMangel(JournalMangelType mangeltype, boolean harJournalfoeringsbehov) {
            this.mangeltype = mangeltype;
            this.harJournalfoeringsbehov = harJournalfoeringsbehov;
        }

        public JournalMangel(JournalMangelType mangeltype, String dokumentId, boolean harJournalfoeringsbehov) {
            this.mangeltype = mangeltype;
            this.dokumentId = dokumentId;
            this.harJournalfoeringsbehov = harJournalfoeringsbehov;
        }

        public JournalMangelType getMangeltype() {
            return mangeltype;
        }

        public String getDokumentId() {
            return dokumentId;
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
