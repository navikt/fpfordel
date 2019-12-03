package no.nav.foreldrepenger.fordel.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "DokumentKategori")
@DiscriminatorValue(DokumentKategori.DISCRIMINATOR)
public class DokumentKategori extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_KATEGORI";

    public static final DokumentKategori UDEFINERT = new DokumentKategori("-");

    public static final DokumentKategori KLAGE_ELLER_ANKE = new DokumentKategori("KLGA");
    public static final DokumentKategori IKKE_TOLKBART_SKJEMA = new DokumentKategori("ITSKJ");
    public static final DokumentKategori SØKNAD = new DokumentKategori("SOKN");
    public static final DokumentKategori ELEKTRONISK_SKJEMA = new DokumentKategori("ESKJ");

    DokumentKategori() {
    }

    private DokumentKategori(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
