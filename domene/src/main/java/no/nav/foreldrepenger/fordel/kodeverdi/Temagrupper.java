package no.nav.foreldrepenger.fordel.kodeverdi;

public enum Temagrupper implements Kodeverdi {

    FAMILIEYTELSER("FMLI");

    private String kode;

    private Temagrupper(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }
}
