package no.nav.foreldrepenger.mottak.behandlendeenhet;

class GeoTilknytning {
    private String tilknytning;
    private String diskresjonskode;

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
}
