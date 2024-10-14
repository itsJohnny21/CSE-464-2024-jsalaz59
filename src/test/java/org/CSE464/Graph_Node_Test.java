package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.CSE464.Graph.Edge;
import org.CSE464.Graph.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Graph_Node_Test {
    public static Graph g;

    @BeforeEach
    public void resetGraph() {
        g = new Graph();
    }

    @Test
    public void Node_Class_Only_Has_Private_Constructors() {
        Constructor<?>[] constructors = Node.class.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
        assertEquals(constructors.length, 1);
    }

    @Test
    public void Graph_Can_Create_Node_With_ID_A() {

        String nodeID = "A";
        Node node = g.addNode(nodeID);

        assertNotNull(node);
        assertEquals(node.getID(), nodeID);
    }

    @Test
    public void Graph_Can_Create_Node_With_ID__() {

        String nodeID = "_";
        Node node = g.addNode(nodeID);

        assertNotNull(node);
        assertEquals(node.getID(), nodeID);
    }

    @Test
    public void Graph_Can_Create_Node_With_Numbers_And_Letters_In_ID() {

        String nodeID = "A123";
        Node node = g.addNode(nodeID);

        assertNotNull(node);
        assertEquals(node.getID(), nodeID);
    }

    @Test
    public void Graph_Can_Create_Node_With_Number_As_ID() {

        String nodeID = "123";
        Node node = g.addNode(nodeID);

        assertNotNull(node);
        assertEquals(node.getID(), nodeID);
    }

    @Test
    public void Graph_Can_Create_Node_With_HTML_As_ID() {

        String html = "<a href='https://github.com/itsJohnny21/CSE-464-2024-jsalaz59'>Johnny</a>";

        String nodeID = html;
        System.out.println(html);
        Node node = g.addNode(nodeID);

        assertNotNull(node);
        assertEquals(node.getID(), nodeID);
    }

    @Test
    public void Graph_Can_Create_Node_With_Quoted_String_As_ID() {

        String html = "\"no pain no gain\"";

        String nodeID = html;
        System.out.println(html);
        Node node = g.addNode(nodeID);

        assertNotNull(node);
        assertEquals(node.getID(), nodeID);
    }

    @Test
    public void Graph_Can_Create_Node_With_Numbers_And_Letters_And_Underscore_In_ID() {

        String nodeID = "_A123";
        Node node = g.addNode(nodeID);

        assertNotNull(node);
        assertEquals(node.getID(), nodeID);
    }

    @Test
    public void Graph_Can_Not_Create_Node_With_ID_Beginning_With_A_Number_Followed_By_A_Valid_Character() {

        assertThrows(InvalidIDException.class, () -> {
            String nodeID = "1A";
            g.addNode(nodeID);
        });
    }

    @Test
    public void Graph_Can_Not_Create_Node_With_ID_As_Empty_String() {

        assertThrows(InvalidIDException.class, () -> {
            String nodeID = "";
            g.addNode(nodeID);
        });
    }

    @Test
    public void Graph_Can_Not_Create_Node_With_ID_As_Space() {

        assertThrows(InvalidIDException.class, () -> {
            String nodeID = " ";
            g.addNode(nodeID);
        });
    }

    @Test
    public void Graph_Can_Not_Create_Node_With_Already_Existing_ID() {

        g.addNode("n1");

        assertThrows(NodeAlreadyExistsException.class, () -> {
            g.addNode("n1");
        });
    }

    @Test
    public void Graph_Can_Not_Create_Node_With_Invalid_Characters() {

        String badCharacters = "+*&^$@!-:'<>{}[]()\";?/|\\";

        for (char c : badCharacters.toCharArray()) {
            assertThrows(InvalidIDException.class, () -> {
                g.addNode(String.valueOf(c));
            });
        }
    }

    @Test
    public void Graph_Counts_One_Node_After_Adding_One_Node() {

        g.addNode("n1");

        int expectedNumberOfNodes = 1;
        int actualNumberOfNodes = g.getNumberOfNodes();

        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
    }

    @Test
    public void Graph_Counts_Three_Nodes_After_Adding_Three_Nodes() {

        g.addNode("n1");
        g.addNode("_abc");
        g.addNode("_");

        int expectedNumberOfNodes = 3;
        int actualNumberOfNodes = g.getNumberOfNodes();

        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
    }

    @Test
    public void Graph_Counts_Zero_Nodes_After_Bad_Node_With_Invalid_ID() {

        assertThrows(InvalidIDException.class, () -> {
            g.addNode("1A");
        });

        int expectedNumberOfNodes = 0;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
    }

    @Test
    public void Graph_Can_Add_Multiple_Nodes_At_Once() {
        String[] nodeIDs = new String[] { "n1", "n2", "n3" };
        g.addNodes(nodeIDs);

        int expectedNumberOfNodes = nodeIDs.length;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        Set<String> uniqueNodeIDs = new HashSet<>();
        for (Node node : g.getNodes().values()) {
            String nodeIDsGrouped = String.join(" ", nodeIDs);
            assertTrue(nodeIDsGrouped.contains(node.getID()));
            uniqueNodeIDs.add(node.getID());
        }

        assertEquals(Set.of(nodeIDs), uniqueNodeIDs);
    }

    @Test
    public void Graph_Will_Not_Add_A_Single_Node_If_Node_Already_Exists_When_Adding_Multiple_Nodes() {
        String[] nodeIDs = new String[] { "n1", "3badID", "n3" };

        assertThrows(InvalidIDException.class, () -> {
            g.addNodes(nodeIDs);
        });

        int expectedNumberOfNodes = 0;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
    }

    @Test
    public void Graph_Will_Not_Add_A_Single_Node_If_Invalid_ID_Is_Proivided_When_Adding_Multiple_Nodes() {
        g.addNode("i_exist_already");
        String[] nodeIDs = new String[] { "n1", "i_exist_already", "n3" };

        assertThrows(NodeAlreadyExistsException.class, () -> {
            g.addNodes(nodeIDs);
        });

        int expectedNumberOfNodes = 1;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
    }

    @Test
    public void Graph_Can_Check_If_Node_Exists() {

        g.addNode("n1");

        assertTrue(g.nodeExists("n1"));
        assertFalse(g.nodeExists("n2"));
    }

    @Test
    public void Graph_Can_Get_An_Existing_Node() {

        String nodeID = "n1";
        g.addNode(nodeID);

        Node n1 = g.getNode(nodeID);

        assertNotNull(n1);
        assertEquals(nodeID, n1.getID());
        assertTrue(g.nodeExists(n1.getID()));
    }

    @Test
    public void Graph_Can_Not_Get_An_Non_Existant_Node() {

        String nodeID = "n1";

        assertThrows(NodeDoesNotExistException.class, () -> {
            g.getNode(nodeID);
        });
    }

    @Test
    public void Graph_Can_Remove_Existing_Node() {

        Node n1 = g.addNode("n1");
        g.removeNode("n1");

        int expectedNumberOfNodes = 0;
        int actualNumberOfNodes = g.getNumberOfNodes();

        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
        assertFalse(g.nodeExists("n1"));
        assertEquals(null, n1.getGraph());
    }

    @Test
    public void Node_Can_Directly_Remove_Itself_From_Graph() {

        Node n1 = g.addNode("n1");
        g.removeNode("n1");

        int expectedNumberOfNodes = 0;
        int actualNumberOfNodes = g.getNumberOfNodes();

        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
        assertEquals(null, n1.getGraph());
    }

    @Test
    public void Graph_Can_Not_Remove_Non_Existant_Node() {

        assertThrows(NodeDoesNotExistException.class, () -> {
            g.removeNode("n1");
        });
    }

    @Test
    public void Graph_Can_Create_An_Edge_Between_Two_Nodes() {

        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        Edge edge = g.addEdge("n1", "n2");

        int expectedNumberOfEdges = 1;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        Node expectedFromNode = edge.getFromNode();
        assertEquals(n1, expectedFromNode);

        Node expectedToNode = edge.getToNode();
        assertEquals(n2, expectedToNode);
    }

    @Test
    public void Graph_Can_Create_A_Self_Loop_Edge() {
        Edge edge = g.addEdge("n1", "n1");

        int expectedNumberOfNodes = 1;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        int expectedNumberOfEdges = 1;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        assertEquals(edge.getFromNode(), edge.getToNode());

        Node n1 = edge.getFromNode();
        assertEquals(n1.getTo().size(), 1);
        assertEquals(n1.getFrom().size(), 1);

        assertTrue(n1.getFrom().containsKey(n1.getID()));
        assertTrue(n1.getTo().containsKey(n1.getID()));
    }

    @Test
    public void Graph_Remembers_Correct_Node_IDs() {
        Set<String> expectedNodeNames = Set.of("n1", "n2", "n3");
        for (String nodeID : expectedNodeNames) {
            g.addNode(nodeID);
        }

        assertEquals(expectedNodeNames, g.getNodeNames());
    }

    @Test
    public void Node_Can_Connect_To_Itself() {
        Node n1 = g.addNode("n1");
        Edge edge = n1.connectTo(n1);

        int expectedNumberOfNodes = 1;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        int expectedNumberOfEdges = 1;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        assertEquals(edge.getFromNode(), edge.getToNode());

        assertEquals(n1.getTo().size(), 1);
        assertEquals(n1.getFrom().size(), 1);

        assertTrue(n1.getFrom().containsKey(n1.getID()));
        assertTrue(n1.getTo().containsKey(n1.getID()));
    }

    @Test
    public void Node_With_Self_Loop_Can_Disconnect_From_Itself() {
        Node n1 = g.addNode("n1");
        n1.connectTo(n1);
        n1.disconnectTo(n1);

        int expectedNumberOfNodes = 1;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        int expectedNumberOfEdges = 0;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        assertEquals(n1.getTo().size(), 0);
        assertEquals(n1.getFrom().size(), 0);

        assertFalse(n1.getFrom().containsKey(n1.getID()));
        assertFalse(n1.getTo().containsKey(n1.getID()));
    }

    @Test
    public void Node_Without_Self_Loop_Can_Not_Disconnect_From_Itself() {
        Node n1 = g.addNode("n1");

        assertThrows(EdgeDoesNotExistException.class, () -> {
            n1.disconnectTo(n1);
        });
    }

    @Test
    public void Automatically_Creates_Node_When_Connecting_Existant_Node_To_Non_Existant_Node() {

        g.addNode("n1");
        String secondNodeID = "n2";
        g.addEdge("n1", secondNodeID);

        int expectedNumberOfNodes = 2;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        int expectedNumberOfEdges = 1;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        Node n2 = g.getNode(secondNodeID);
        assertEquals(secondNodeID, n2.getID());
    }

    @Test
    public void Automatically_Creates_Node_When_Connecting_Non_Existant_Node_To_Non_Existant_Node() {

        String nodeID1 = "n1";
        String nodeID2 = "n2";
        g.addEdge(nodeID1, nodeID2);

        int expectedNumberOfNodes = 2;
        assertEquals(expectedNumberOfNodes, g.getNumberOfNodes());

        int expectedNumberOfEdges = 1;
        assertEquals(expectedNumberOfEdges, g.getNumberOfEdges());

        Node n1 = g.getNode(nodeID1);
        assertNotNull(n1);

        Node n2 = g.getNode(nodeID2);
        assertNotNull(n2);

        assertEquals(nodeID1, n1.getID());
        assertEquals(nodeID2, n2.getID());

        assertTrue(n1.getTo().containsKey(n2.getID()));
        assertEquals(n1.getFrom().size(), 0);

        assertTrue(n2.getFrom().containsKey(n1.getID()));
        assertEquals(n2.getTo().size(), 0);
    }

    @Test
    public void Node_Can_Directly_Connect_To_Other_Node() {

        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");
        n1.connectTo(n2);

        int expectedNumberOfEdges = 1;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);
    }

    @Test
    public void Nodes_Keep_Correct_Count_Of_To_And_From_Nodes() {

        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        assertEquals(n1.getTo().size(), 0);
        assertEquals(n1.getFrom().size(), 0);

        assertEquals(n2.getTo().size(), 0);
        assertEquals(n2.getFrom().size(), 0);

        n1.connectTo(n2);
        assertEquals(n1.getTo().size(), 1);
        assertEquals(n1.getFrom().size(), 0);

        assertEquals(n2.getTo().size(), 0);
        assertEquals(n2.getFrom().size(), 1);
    }

    @Test
    public void Connected_Nodes_Can_Directly_Refer_To_Each_Other() {

        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");
        n1.connectTo(n2);

        assertEquals(n1.getTo().get("n2"), n2);
        assertEquals(n2.getFrom().get("n1"), n1);
    }

    @Test
    public void Graph_Can_Remove_NodeA_Which_Is_Connected_To_NodeB_And_To_For_NodeB_Is_Updated() {

        Node A = g.addNode("A");
        Node B = g.addNode("B");
        A.connectTo(B);

        assertTrue(A.getTo().containsKey("B"));

        g.removeNode("B");
        assertFalse(A.getTo().containsKey("B"));
        assertEquals(A.getTo().size(), 0);

        assertFalse(B.getFrom().containsKey("A"));
        assertEquals(B.getFrom().size(), 0);
    }

    @Test
    public void Node_Can_Connect_To_Multiple_Nodes_Functional_Programming_Style() {
        Node A = g.addNode("A");
        Node B = g.addNode("B");
        Node C = g.addNode("C");
        Node D = g.addNode("D");
        Node E = g.addNode("E");
        Node F = g.addNode("F");

        A.connectTo(B).getToNode().connectTo(C).getToNode().connectTo(D).getToNode().connectTo(E).getToNode()
                .connectTo(F);

        int expectedNumberOfEdges = 5;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);
    }

    @Test
    public void Nodes_Can_Refer_To_Their_Edges() {

        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");
        Edge edge = g.addEdge("n1", "n2");

        assertEquals(n1.to(n2), edge);
        assertEquals(n2.from(n1), edge);
    }

    @Test
    public void Node_Can_Remove_One_Of_Its_To_Edges() {
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");
        g.addEdge("n1", "n2");

        n1.disconnectTo(n2);

        assertFalse(n1.getTo().containsKey("n2"));
        assertFalse(n2.getFrom().containsKey("n1"));

        int expectedNumberOfNodes = 2;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        int expectedNumberOfEdges = 0;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        assertTrue(g.nodeExists("n2"));
        assertTrue(g.nodeExists("n1"));
    }

    @Test
    public void Node_Can_Remove_Itself_From_Graph() throws IOException, InterruptedException {
        Node n1 = g.addNode("n1");
        g.addEdge("n1", "n2");
        g.addEdge("n2", "n3");
        g.addEdge("n4", "n1");
        g.addEdge("n5", "n1");
        g.addEdge("n4", "n5");

        n1.disconnectFromGraph();

        int expectedNumberOfNodes = 4;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        int expectedNumberOfEdges = 2;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        for (Node node : g.getNodes().values()) {
            assertFalse(node.getTo().containsKey(n1.getID()));
            assertFalse(node.getFrom().containsKey(n1.getID()));
        }

    }

    @Test
    public void Node_Can_Remove_One_Of_Its_From_Edges() {

        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");
        g.addEdge("n1", "n2");

        n2.disconnectFrom(n1);

        assertFalse(n1.getTo().containsKey("n2"));
        assertFalse(n2.getFrom().containsKey("n1"));

        int expectedNumberOfNodes = 2;
        int actualNumberOfNodes = g.getNumberOfNodes();
        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);

        int expectedNumberOfEdges = 0;
        int actualNumberOfEdges = g.getNumberOfEdges();
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        assertTrue(g.nodeExists("n2"));
        assertTrue(g.nodeExists("n1"));
    }

    @Test
    public void All_Node_Attributes_Work() {
        Node n1 = g.addNode("n1");
        for (Node.Attribute attribute : Node.Attribute.values()) {
            n1.setAttribute(attribute.getValue(), "some value");
        }

        assertEquals(Node.Attribute.values().length, n1.getAttributes().size());
    }

    @Test
    public void Node_Attributes_Persist_After_Removing_Itself_From_Graph() {

        Node n1 = g.addNode("n1");

        n1.setAttribute("label", "node A");
        n1.setAttribute("color", "blue");
        n1.setAttribute("shape", "star");
        n1.setAttribute("opacity", "30");
        n1.setAttribute("font", "roboto");

        HashMap<String, String> beforeRemoval = n1.getAttributes();

        g.removeNode("n1");
        assertEquals(beforeRemoval, n1.getAttributes());
    }

    @Test
    public void Graph_Remembers_Node_Labels() {
        g.addNode("n1");
        Node n2 = g.addNode("n2");
        Node n3 = g.addNode("n3");
        Node n4 = g.addNode("n4");

        Set<String> expectedNodeLabels = Set.of("n1=null", "n2=hello", "n3=world", "n4=");
        n2.setAttribute("label", "hello");
        n3.setAttribute("label", "world");
        n4.setAttribute("label", "");

        assertEquals(expectedNodeLabels, g.getNodeLabels());
    }

    @Test
    public void Edge_To_String_Shows_ID_And_Attributes() {
        Node n1 = g.addNode("n1");

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

            assertTrue(n1.toString().contains(attribute));
            assertTrue(n1.toString().contains(value));
        }
    }

    @Test
    public void Node_Can_Not_Remove_Itself_From_Graph_When_It_Is_Not_Associated_With_One() {

        Node n1 = g.addNode("n1");
        g.removeNode("n1");

        assertThrows(NullPointerException.class, () -> {
            n1.disconnectFromGraph();
        });

        int expectedNumberOfNodes = 0;
        int actualNumberOfNodes = g.getNumberOfNodes();

        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
    }

    @Test
    public void Node_Can_Not_Set_Graph() {
        assertThrows(NoSuchMethodException.class, () -> {
            Node.class.getMethod("setGraph", Graph.class);
        });
    }
}