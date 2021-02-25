package no.nav.foreldrepenger.mottak.person;

public record GeoTilknytning(String tilknytning, String diskresjonskode) {

    public static GeoTilknytning INGEN = new GeoTilknytning(null, null);
}
