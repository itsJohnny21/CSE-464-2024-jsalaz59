package org.CSE464;

/**
 * Enum representing the output formats for Graphviz.
 * 
 * <p>For more information, see the Graphviz documentation:</p>
 * <a href="https://graphviz.org/docs/outputs/">Graphviz Output Formats</a>
 */
public enum Format {
    BMP("-Tbmp", ".bmp"), DOT("-Tdot", ".dot"), JPEG("-Tjpg", ".jpg"), JSON("-Tjson", ".json"), PDF("-Tpdf", ".pdf"),
    PICT("-Tpct", ".pct"), PLAINTEXT("-Tplain", ".txt"), PNG("-Tpng", ".png"), SVG("-Tsvg", ".svg"),
    RAWDOT("-Tdot", ".dot");
    ;

    protected final String value;
    protected final String extension;

    Format(String value, String extension) {
        this.value = value;
        this.extension = extension;
    }

    public String getValue() {
        return value;
    }

    public String getExtension() {
        return extension;
    }
}