package no.nav.foreldrepenger.mottak.behandlendeenhet;

class GeoTilknytning {
    private final String tilknytning;
    private final String diskresjonskode;

    GeoTilknytning(String geografiskTilknytning, String diskresjonskode) {
        this.tilknytning = geografiskTilknytning;
        this.diskresjonskode = diskresjonskode;
    }

    String getTilknytning() {
        return tilknytning;
    }

    String getDiskresjonskode() {
        return diskresjonskode;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[tilknytning=" + tilknytning + ", diskresjonskode=" + diskresjonskode
                + "]";
    }
}
