package org.CSE464;

import java.util.HashMap;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public abstract class DOTElement {
    protected static final String ID_REGEX = "^[a-zA-Z_][a-zA-Z_0-9]*$|^[-]?(\\.[0-9]+|[0-9]+(\\.[0-9]*)?)$|^\\\"(\\\"|[^\\\"])*\\\"$|^<([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>(.*?)<\\/\\4>$";
    protected final HashMap<String, String> attributes;
    protected final String ID;

    public DOTElement() {
        this.ID = null;
        this.attributes = new HashMap<>();
    }

    public DOTElement(String ID) {
        this.attributes = new HashMap<>();
        this.ID = ID;
    }

    public String getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    public void setAttribute(String attribute, String value) {
        if (!attribute.matches(ID_REGEX)) {
            throw new InvalidIDException(String.format(
                    "Error: Attempt to set attribute '%s' to '%s' failed. The attribute does not satisfy the ID regex.",
                    attribute, getClass()));
        }
        attributes.put(attribute, value);
    }

    public void removeAttribute(String attribute) {
        attributes.remove(attribute);
    }

    public void clearAttributes() {
        attributes.clear();
    }

    public String toDot() {
        String attrsString = "[" + attributes.entrySet().stream()
                .map(e -> String.format("%s=\"%s\"", e.getKey(), e.getValue())).collect(Collectors.joining(" ")) + "]";

        String dotString = String.format("%s %s;", ID, attrsString);
        return dotString;
    }
}
