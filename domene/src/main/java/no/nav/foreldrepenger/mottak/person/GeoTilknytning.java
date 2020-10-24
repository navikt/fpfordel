package no.nav.foreldrepenger.mottak.person;

public class GeoTilknytning {
    private final String tilknytning;
    private final String diskresjonskode;

    public GeoTilknytning(String geografiskTilknytning, String diskresjonskode) {
        this.tilknytning = geografiskTilknytning;
        this.diskresjonskode = diskresjonskode;
    }

    public String getTilknytning() {
        return tilknytning;
    }

    public String getDiskresjonskode() {
        return diskresjonskode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[tilknytning=" + tilknytning + ", diskresjonskode=" + diskresjonskode
                + "]";
    }
}
