package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DOTElement_Test {
    public static Graph g;

    @BeforeEach
    public void resetGraph() {
        g = new Graph();
    }

    @Test
    public void DOTElement_Can_Set_Attribute() {
        DOTElement e = g.addEdge("n1", "n2");
        assertEquals(0, e.getAttributes().size());

        String attribute = "label";
        String value = "3";
        e.setAttribute(attribute, value);

        assertEquals(1, e.getAttributes().size());
        assertTrue(e.getAttributes().containsKey(attribute));
        assertTrue(e.getAttributes().containsValue(value));
    }

    @Test
    public void DOTElement_Can_Get_Attribute() {
        DOTElement e = g.addEdge("n1", "n2");
        String attribute = "arrowsize";
        String value = "21";
        e.setAttribute(attribute, value);

        assertEquals(e.getAttribute(attribute), value);
    }

    @Test
    public void DOTElement_Can_Not_Set_Bad_Attribute() {

        DOTElement e = g.addEdge("n1", "n2");

        assertThrows(InvalidIDException.class, () -> {
            e.setAttribute("1A", "some value");
        });

        assertThrows(InvalidIDException.class, () -> {
            e.setAttribute("1\"A", "some value");
        });

        assertThrows(InvalidIDException.class, () -> {
            e.setAttribute("1!A", "some value");
        });
    }

    @Test
    public void DOTElement_Can_Not_Set_Empty_Value_For_Attribute() {

        DOTElement e = g.addEdge("n1", "n2");
        String attribute = "1A";
        String value = "";

        assertThrows(InvalidIDException.class, () -> {
            e.setAttribute(attribute, value);
        });
    }

    @Test
    public void DOTElement_To_String_Shows_ID_And_Attributes() {
        DOTElement e = g.addEdge("n1", "n2");

        HashMap<String, String> attrs = new HashMap<>();
        attrs.put("label", "weight=21");
        attrs.put("color", "blue");
        attrs.put("storkewidth", "21");
        attrs.put("opacity", "30");
        attrs.put("font", "roboto");

        for (Entry<String, String> entry : attrs.entrySet()) {
            String attribute = entry.getKey();
            String value = entry.getValue();
            e.setAttribute(attribute, value);

            assertNotNull(e.toDot());
            assertTrue(e.toDot().contains(attribute));
            assertTrue(e.toDot().contains(value));
        }
    }

    @Test
    public void DOTElement_Does_Nothing_When_Removing_Nonexistant_Attribute() {
        DOTElement n1 = g.addNode("1");
        assertDoesNotThrow(() -> {
            n1.removeAttribute("DNE");
        });

        assertEquals(n1.getAttribute("DNE"), null);
    }

    @Test
    public void DOTElement_Can_Remove_Attribute() {
        DOTElement n1 = g.addNode("n1");

        HashMap<String, String> attrs = new HashMap<>();
        attrs.put("label", "weight=21");
        attrs.put("color", "blue");
        attrs.put("storkewidth", "21");
        attrs.put("opacity", "30");
        attrs.put("font", "roboto");

        for (Entry<String, String> entry : attrs.entrySet()) {
            String attribute = entry.getKey();
            String value = entry.getValue();
            n1.setAttribute(attribute, value);
        }

        n1.removeAttribute("font");
        assertEquals(attrs.size() - 1, n1.getAttributes().size());
        assertFalse(n1.getAttributes().containsKey("font"));
    }

    @Test
    public void DOTElement_Can_Clear_Attributes() {
        DOTElement n1 = g.addNode("n1");

        HashMap<String, String> attrs = new HashMap<>();
        attrs.put("label", "weight=21");
        attrs.put("color", "blue");
        attrs.put("storkewidth", "21");
        attrs.put("opacity", "30");
        attrs.put("font", "roboto");

        for (Entry<String, String> entry : attrs.entrySet()) {
            String attribute = entry.getKey();
            String value = entry.getValue();
            n1.setAttribute(attribute, value);
        }

        n1.clearAttributes();
        assertEquals(0, n1.getAttributes().size());

        for (String attr : attrs.keySet()) {
            assertNull(n1.getAttribute(attr));
        }
    }

}