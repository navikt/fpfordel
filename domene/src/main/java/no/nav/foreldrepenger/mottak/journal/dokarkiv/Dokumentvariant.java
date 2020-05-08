package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Dokumentvariant {

    @JsonProperty("variantformat")
    private VariantFormat variantFormat;
    @JsonProperty("filtype")
    private String filtype;
    @JsonProperty("fysiskDokument")
    private String fysiskDokument;

    @JsonCreator
    public Dokumentvariant(@JsonProperty("variantformat") VariantFormat variantFormat,
                           @JsonProperty("filtype") String filtype,
                           @JsonProperty("fysiskDokument") String fysiskDokument) {
        this.variantFormat = variantFormat;
        this.filtype = filtype;
        this.fysiskDokument = fysiskDokument;
    }

    public VariantFormat getVariantFormat() {
        return variantFormat;
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
                "variantFormat=" + variantFormat +
                ", filtype='" + filtype + '\'' +
                '}';
    }
}
