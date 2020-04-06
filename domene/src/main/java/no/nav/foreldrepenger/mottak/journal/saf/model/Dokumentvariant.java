package no.nav.foreldrepenger.mottak.journal.saf.model;

import java.util.Objects;

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
    private VariantFormat variantFormat;

    @JsonCreator
    public Dokumentvariant(@JsonProperty("variantformat") VariantFormat variantFormat) {
        this.variantFormat = variantFormat;
    }

    public VariantFormat getVariantFormat() {
        return variantFormat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dokumentvariant that = (Dokumentvariant) o;
        return variantFormat == that.variantFormat;
    }

    @Override
    public int hashCode() {
        return Objects.hash(variantFormat);
    }

    @Override
    public String toString() {
        return "Dokumentvariant{" +
                "variantFormat=" + variantFormat +
                '}';
    }
}
