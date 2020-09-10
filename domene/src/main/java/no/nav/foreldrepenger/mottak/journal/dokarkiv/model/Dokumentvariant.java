package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Dokumentvariant {

    @JsonProperty("variantformat")
    private Variantformat variantformat;
    @JsonProperty("filtype")
    private String filtype;
    @JsonProperty("fysiskDokument")
    private String fysiskDokument;

    @JsonCreator
    public Dokumentvariant(@JsonProperty("variantformat") Variantformat variantformat,
            @JsonProperty("filtype") String filtype,
            @JsonProperty("fysiskDokument") String fysiskDokument) {
        this.variantformat = variantformat;
        this.filtype = filtype;
        this.fysiskDokument = fysiskDokument;
    }

    public Variantformat getVariantformat() {
        return variantformat;
    }

    public String getFiltype() {
        return filtype;
    }

    public String getFysiskDokument() {
        return fysiskDokument;
    }

    @Override
    public String toString() {
        return "Dokumentvariant{" +
                "variantFormat=" + variantformat +
                ", filtype='" + filtype + '\'' +
                '}';
    }
}
