package no.nav.foreldrepenger.fordel.kodeverk;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "DokumentKategori")
@DiscriminatorValue(DokumentKategori.DISCRIMINATOR)
public class DokumentKategori extends Kodeliste {

    public static final String DISCRIMINATOR = "DOKUMENT_KATEGORI";

    public static final DokumentKategori UDEFINERT = new DokumentKategori("-"); //$NON-NLS-1$

    public static final DokumentKategori KLAGE_ELLER_ANKE = new DokumentKategori("KLGA"); //$NON-NLS-1$
    public static final DokumentKategori IKKE_TOLKBART_SKJEMA = new DokumentKategori("ITSKJ"); //$NON-NLS-1$
    public static final DokumentKategori SØKNAD = new DokumentKategori("SOKN"); //$NON-NLS-1$
    public static final DokumentKategori ELEKTRONISK_SKJEMA = new DokumentKategori("ESKJ"); //$NON-NLS-1$

    DokumentKategori() {
        // Hibernate trenger en
    }

    private DokumentKategori(String kode) {
        super(kode, DISCRIMINATOR);
    }
}
