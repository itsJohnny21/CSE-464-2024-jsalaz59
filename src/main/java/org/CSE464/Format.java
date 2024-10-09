package org.CSE464;

/**
 * Enum representing the output formats for Graphviz.
 * 
 * <p>For more information, see the Graphviz documentation:</p>
 * <a href="https://graphviz.org/docs/outputs/">Graphviz Output Formats</a>
 */
public enum Format {
    BMP("-Tbmp"), DOT("-Tdot"), JEPG("-Tjpg"), JSON("-Tjson"), PDF("-Tpdf"), PICT("-Tpict"), PLAINTEXT("-Tplain"),
    PNG("-Tpng"), SVG("Tsvg");

    protected final String value;

    Format(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}