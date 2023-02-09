package no.nav.foreldrepenger.fordel.kodeverdi;

public enum YtelseType {

    ENGANGSTØNAD("ES"),
    FORELDREPENGER("FP"),
    SVANGERSKAPSPENGER("SVP"),
    ;

    private String kode;

    YtelseType(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
