package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.CSE464.Graph.Edge;
import org.CSE464.Graph.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Graph_Edge_Test {
    public static Graph g;

    @BeforeEach
    public void resetGraph() {
        g = new Graph();
    }

    @Test
    public void Edge_Class_Only_Has_Private_Constructors() {
        Constructor<?>[] constructors = Edge.class.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
        assertEquals(constructors.length, 1);
    }

    @Test
    public void Graph_Correctly_Creates_Edge_ID() {
        Edge e = g.addEdge("n1", "n2");

        Node n1 = e.getFromNode();
        Node n2 = e.getToNode();

        String expectedEdgeID = String.format("%s -> %s", n1.getID(), n2.getID());
        String actualEdgeID = e.getID();
        assertEquals(expectedEdgeID, actualEdgeID);
    }

    @Test
    public void Graph_Can_Create_Edge_Between_Two_Nodes() {
        String nodeID1 = "A";
        String nodeID2 = "B";

        Node n1 = g.addNode(nodeID1);
        Node n2 = g.addNode(nodeID2);
        Edge edge = g.addEdge(nodeID1, nodeID2);

        assertNotNull(edge);
        assertEquals(edge.getFromNode(), n1);
        assertEquals(edge.getToNode(), n2);
    }

    @Test
    public void Graph_Automatically_Creates_Nodes_If_Nodes_Do_Not_Exist_When_Creating_Edges() {
        String nodeID1 = "A";
        String nodeID2 = "B";

        Edge edge = g.addEdge(nodeID1, nodeID2);
        int expectedNumberOfNodes = 2;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
        assertEquals(edge.getToNode().getID(), nodeID2);
        assertEquals(edge.getFromNode().getID(), nodeID1);
        assertTrue(edge.getToNode().getFrom().containsKey(nodeID1));
        assertTrue(edge.getFromNode().getTo().containsKey(nodeID2));
    }

    @Test
    public void Edge_Can_Be_Created_Directly_From_Nodes_Using_To() {
        String nodeID1 = "n1";
        String nodeID2 = "n2";

        Node n1 = g.addNode(nodeID1);
        Node n2 = g.addNode(nodeID2);
        Edge edge = n1.connectTo(n2);
        assertNotNull(edge);

        assertEquals(edge.getFromNode(), n1);
        assertEquals(edge.getToNode(), n2);
    }

    @Test
    public void Edge_Can_Be_Created_Directly_From_Nodes_Using_From() {
        String nodeID1 = "n1";
        String nodeID2 = "n2";

        Node n1 = g.addNode(nodeID1);
        Node n2 = g.addNode(nodeID2);
        Edge edge = n2.connectFrom(n1);

        assertNotNull(edge);
        assertEquals(edge.getFromNode(), n1);
        assertEquals(edge.getToNode(), n2);
    }

    @Test
    public void Edges_Referred_To_By_Nodes_Are_Correct() {
        String nodeID1 = "n1";
        String nodeID2 = "n2";

        Node n1 = g.addNode(nodeID1);
        Node n2 = g.addNode(nodeID2);
        Edge edge = n1.connectTo(n2);

        assertEquals(n1.to(n2), edge);
        assertEquals(n2.from(n1), edge);
    }

    @Test
    public void Graph_Remembers_Edge_Directions() {
        Set<String> edgeDirections = Set.of("root -> n1", "root -> n2", "root -> n3", "n1 -> n5");

        for (String edgeDirection : edgeDirections) {
            String[] nodeIDs = edgeDirection.split(" -> ");
            String fromNodeID = nodeIDs[0];
            String toNodeID = nodeIDs[1];

            g.addEdge(fromNodeID, toNodeID);
        }

        assertEquals(edgeDirections, g.getEdgeDirections());
    }

    @Test
    public void Graph_Can_Check_If_Edge_Exists() {
        assertFalse(g.edgeExists("n1", "n2"));

        g.addEdge("n1", "n2");
        assertTrue(g.edgeExists("n1", "n2"));

        g.removeEdge("n1", "n2");
        assertFalse(g.edgeExists("n1", "n2"));
    }

    @Test
    public void Graph_Can_Not_Create_An_Already_Existing_Edge() {
        String nodeID1 = "n1";
        String nodeID2 = "n2";

        Node n1 = g.addNode(nodeID1);
        Node n2 = g.addNode(nodeID2);
        g.addEdge(nodeID1, nodeID2);

        assertThrows(EdgeAlreadyExistsException.class, () -> {
            g.addEdge(nodeID1, nodeID2);
        });

        assertThrows(EdgeAlreadyExistsException.class, () -> {
            g.addEdge(nodeID1, nodeID2);
            n1.connectTo(n2);
            n1.connectFrom(n2);
        });
    }

    @Test
    public void Graph_Can_Create_Self_Loop_For_A_Node() {
        String nodeID = "n1";
        Edge edge = g.addEdge(nodeID, nodeID);

        int expectedNumberOfNodes = 1;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        int expectedNumberOfEdges = 1;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);
        assertEquals(edge.getToNode(), edge.getFromNode());
    }

    @Test
    public void Graph_Can_Create_Distinct_Two_Way_Edges() {
        String nodeID1 = "n1";
        String nodeID2 = "n2";
        Edge edge = g.addEdge(nodeID1, nodeID2);
        Edge edge2 = g.addEdge(nodeID2, nodeID1);

        assertNotEquals(edge, edge2);

        int expectedNumberOfNodes = 2;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        int expectedNumberOfEdges = 2;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);
        assertEquals(edge.getToNode(), edge2.getFromNode());
        assertEquals(edge.getFromNode(), edge2.getToNode());
    }

    @Test
    public void Graph_Can_Create_A_Large_Amount_Of_Edges() {
        int numEdges = 999;
        Node root = g.addNode("root");
        for (int i = 0; i < numEdges; i++) {
            String nodeID1 = String.format("node_%d", i);
            g.addEdge(root.getID(), nodeID1);
        }

        int expectedNumberOfEdges = numEdges;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);
        assertEquals(numEdges, root.getTo().size());
        assertEquals(0, root.getFrom().size());
    }

    @Test
    public void Graph_Can_Get_An_Existant_Edge() {
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        Edge e = g.addEdge(n1.getID(), n2.getID());
        Edge e2 = g.getEdge(n1.getID(), n2.getID());

        assertEquals(e, e2);
        assertEquals(e2.getFromNode(), n1);
        assertEquals(e2.getToNode(), n2);

        Node n3 = g.addNode("n3");
        n1.connectTo(n3);
        Edge e3 = g.getEdge("n1", "n3");

        assertEquals(e3.getToNode(), n3);
        assertEquals(e3.getFromNode(), n1);

        int expectedNumberOfEdges = 2;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);
    }

    @Test
    public void Graph_Can_Remove_Existing_Edge() {
        Edge e = g.addEdge("n1", "n2");
        int expectedNumberOfEdges = 1;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        g.removeEdge("n1", "n2");
        expectedNumberOfEdges = 0;
        actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        Node n1 = e.getFromNode();
        Node n2 = e.getToNode();

        assertEquals(0, n1.getTo().size());
        assertEquals(0, n2.getFrom().size());

        assertFalse(n1.getTo().containsKey("n2"));
    }

    @Test
    public void Graph_Can_Not_Remove_A_Non_Existant_Edge() {
        assertThrows(EdgeDoesNotExistException.class, () -> {
            g.removeEdge("n1", "n2");
        });

        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");
        assertThrows(EdgeDoesNotExistException.class, () -> {
            g.removeEdge("n1", "n2");
        });

        n1.connectTo(n1);
        n2.connectFrom(n2);
        assertThrows(EdgeDoesNotExistException.class, () -> {
            g.removeEdge("n1", "n2");
        });

        n2.connectTo(n1);
        assertThrows(EdgeDoesNotExistException.class, () -> {
            g.removeEdge("n1", "n2");
        });
    }

    @Test
    public void Edge_Can_Be_Removed_Directly_From_A_Node() {
        g.addNode("n1").connectTo(g.addNode("n2"));

        Node n1 = g.getNode("n1");
        Node n2 = g.getNode("n2");

        int expectedNumberOfEdges = 1;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        n2.disconnectFrom(n1);
        expectedNumberOfEdges = 0;
        actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        assertEquals(0, n1.getTo().size());
        assertFalse(n1.getTo().containsKey(n2.getID()));

        assertEquals(0, n2.getFrom().size());
        assertFalse(n2.getFrom().containsKey(n1.getID()));
    }

    @Test
    public void Edge_Can_Not_Be_Removed_If_Edge_Does_Not_Exist() {
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        assertThrows(EdgeDoesNotExistException.class, () -> {
            n1.disconnectTo(n2);
        });
    }

    @Test
    public void Edge_Can_Be_Removed_From_Graph_Directly() {
        int numEdges = 10;
        while (g.getNumberOfEdges() < numEdges) {
            String nodeID = String.format("%d", g.getNumberOfEdges());
            g.addEdge(nodeID, nodeID);
        }

        Edge firstEdge = g.getEdge("0", "0");
        firstEdge.removeFromGraph();

        int expectedNumberOfEdges = 9;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);
    }

    @Test
    public void Graph_Still_Contains_Nodes_After_Removing_Edge() {
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        n1.connectTo(n2);
        g.removeEdge("n1", "n2");

        n1.connectTo(n1);
        g.removeEdge("n1", "n1");

        assertTrue(g.nodeExists("n1"));
        assertTrue(g.nodeExists("n2"));
        assertEquals(n1.getGraph(), g);
        assertEquals(n2.getGraph(), g);
    }

    @Test
    public void Edge_Attributes_Persist_After_Removing_Itself_From_Graph() {
        Edge e = g.addEdge("n1", "n2");

        e.setAttribute("label", "node A");
        e.setAttribute("color", "blue");
        e.setAttribute("shape", "star");
        e.setAttribute("opacity", "30");
        e.setAttribute("font", "roboto");

        HashMap<String, String> beforeRemoval = e.getAttributes();

        g.removeEdge("n1", "n2");
        assertEquals(beforeRemoval, e.getAttributes());
    }

    @Test
    public void All_Edge_Attributes_Work() {
        Edge e = g.addEdge("n1", "n2");
        for (Edge.Attribute attribute : Edge.Attribute.values()) {
            assertDoesNotThrow(() -> {
                e.setAttribute(attribute, "some value");
                e.getAttribute(attribute);
                e.removeAttribute(attribute);
            });
            e.setAttribute(attribute.getValue(), "some value");
        }

        assertEquals(Edge.Attribute.values().length, e.getAttributes().size());
    }

    @Test
    public void Edge_Can_Not_Set_To_And_From_Nodes() throws NoSuchFieldException {
        Field fromNodeField = Edge.class.getDeclaredField("fromNode");
        Field toNodeField = Edge.class.getDeclaredField("toNode");

        assertTrue(Modifier.isFinal(fromNodeField.getModifiers()));
        assertTrue(Modifier.isFinal(toNodeField.getModifiers()));
    }

    @Test
    public void Edge_To_String_Shows_ID_And_Attributes() {
        Edge e = g.addEdge("n1", "n2");

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

            assertTrue(e.toString().contains(attribute));
            assertTrue(e.toString().contains(value));
        }
    }
}