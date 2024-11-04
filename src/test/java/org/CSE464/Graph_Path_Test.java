package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.Utils;
import org.CSE464.Graph.Algorithm;
import org.CSE464.Graph.Edge;
import org.CSE464.Graph.Node;
import org.CSE464.Graph.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Graph_Path_Test {
    public static Graph g;

    @BeforeEach
    public void resetGraph() {
        g = new Graph();
    }

    @Test
    public void Path_Class_Only_Has_Private_Constructors() {
        Constructor<?>[] constructors = Path.class.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        }
        assertEquals(constructors.length, 1);
    }

    @Test
    public void Graph_Correctly_Creates_Path_n1_To_n2_To_n3() {
        String n1ID = "n1";
        String n2ID = "n2";
        String n3ID = "n3";

        Path p = g.addPath(n1ID, n2ID, n3ID);

        assertTrue(g.nodeExists(n1ID));
        assertTrue(g.nodeExists(n2ID));
        assertTrue(g.nodeExists(n3ID));

        assertTrue(g.edgeExists(n1ID, n2ID));
        assertTrue(g.edgeExists(n2ID, n3ID));

        int expectedNumberOfNodes = 3;
        int actualNumberOfNodes = g.getNumberOfNodes();

        int expectedNumberOfEdges = 2;
        int actualNumberOfEdges = g.getNumberOfEdges();

        assertEquals(expectedNumberOfNodes, actualNumberOfNodes);
        assertEquals(expectedNumberOfEdges, actualNumberOfEdges);

        String expectedPathID = String.format("%s -> %s -> %s", n1ID, n2ID, n3ID);
        String actualPathID = p.getID();

        assertEquals(expectedPathID, actualPathID);
    }

    @Test
    public void Path_Is_Null_If_Adding_Empty_Path_To_Graph() {
        Path p = g.addPath();
        assertNull(p);
    }

    @Test
    public void Graph_Does_Nothing_If_Adding_Path_With_Zero_Nodes() {
        g.addPath();

        assertEquals(0, g.getNumberOfNodes());
        assertEquals(0, g.getNumberOfEdges());
    }

    @Test
    public void Graph_Automatically_Cretes_Edges_When_Adding_A_Path_With_Nonexistent_Nodes() {
        g.addPath("n1", "n2", "n3", "n4");

        assertEquals(4, g.getNumberOfNodes());
        assertTrue(g.nodeExists("n1"));
        assertTrue(g.nodeExists("n2"));
        assertTrue(g.nodeExists("n3"));
        assertTrue(g.nodeExists("n4"));
    }

    @Test
    public void Graph_Automatically_Cretes_Edges_When_Adding_A_Path_With_Nonexistent_Edges() {
        g.addPath("n1", "n2");
        g.addPath("n3", "n4");
        g.addPath("n1", "n4", "n1");

        assertEquals(4, g.getNumberOfEdges());

        assertTrue(g.edgeExists("n1", "n2"));
        assertTrue(g.edgeExists("n3", "n4"));
        assertTrue(g.edgeExists("n1", "n4"));
        assertTrue(g.edgeExists("n4", "n1"));
    }

    @Test
    public void Graph_Does_Not_Duplicate_Nodes_When_Adding_Duplicate_Paths() {
        g.addNodes("n1", "n2", "n3");
        g.addPath("n1", "n2", "n3");

        assertEquals(3, g.getNumberOfNodes());
    }

    @Test
    public void Graph_Does_Not_Duplicate_Edges_When_Adding_Duplicate_Paths() {
        g.addEdge("n1", "n2");
        g.addEdge("n2", "n3");
        g.addEdge("n3", "n4");

        g.addPath("n1", "n2", "n3", "n4");

        assertEquals(3, g.getNumberOfEdges());
    }

    @Test
    public void Graph_Does_Not_Throw_Exception_When_Adding_Duplicate_Paths() {
        Path p1 = g.addPath("n1", "n2", "n3", "n4");
        Path p2 = g.addPath("n1", "n2", "n3", "n4");

        assertTrue(p1.equals(p2));
        assertEquals(4, g.getNumberOfNodes());
        assertEquals(3, g.getNumberOfEdges());

        List<String> nodeIDs = List.of("n1", "n2", "n3", "n4");
        List<List<String>> powerSet = Utils.generatePowerSet(nodeIDs);

        for (List<String> nodeList : powerSet) {
            String[] nodes = nodeList.toArray(new String[0]);
            g.addPath(nodes);
        }
    }

    @Test
    public void Graph_Correctly_Removes_Edges_When_Removing_A_Path() {
        g.addNode("n1").connectTo(g.addNode("n2")).toNode.connectTo(g.addNode("n3")).toNode.connectTo(g.addNode("n4"));
        g.addNode("a1").connectTo(g.addNode("a2")).toNode.connectTo(g.addNode("a3"));
        g.addNodes("b1", "b2", "b3", "b4");

        int numberOfNodesBefore = g.getNumberOfNodes();
        int numberOfEdgesBefore = g.getNumberOfEdges();
        g.removePath("n1", "n2", "n3");
        int numberOfNodesAfter = g.getNumberOfNodes();
        int numberOfEdgesAfter = g.getNumberOfEdges();

        assertEquals(numberOfNodesBefore, numberOfNodesAfter);
        assertEquals(numberOfEdgesBefore, numberOfEdgesAfter + 2);

        assertTrue(g.edgeExists("n3", "n4"));
    }

    @Test
    public void Graph_Removes_Nothing_When_Removing_A_Path_That_Does_Not_Exist() {
        g.addNodes("n1", "n2", "n3", "n4");
        g.addEdge("n1", "n4");
        g.addEdge("n2", "n2");

        int numberOfNodesBefore = g.getNumberOfNodes();
        int numberOfEdgesBefore = g.getNumberOfEdges();

        assertThrows(PathDoesNotExistException.class, () -> {
            g.removePath("n1", "n2", "n3");
        });

        int numberOfNodesAfter = g.getNumberOfNodes();
        int numberOfEdgesAfter = g.getNumberOfEdges();

        assertEquals(numberOfNodesBefore, numberOfNodesAfter);
        assertEquals(numberOfEdgesBefore, numberOfEdgesAfter);

        assertTrue(g.nodeExists("n1"));
        assertTrue(g.nodeExists("n2"));
        assertTrue(g.nodeExists("n3"));
        assertTrue(g.nodeExists("n4"));

        assertTrue(g.edgeExists("n1", "n4"));
        assertTrue(g.edgeExists("n2", "n2"));
    }

    @Test
    public void Graph_Only_Removes_Edges_In_Path_When_Removing_A_Path() {
        g.addPath("n1", "n2", "n3", "n4");
        g.removePath("n2", "n3");

        assertTrue(g.edgeExists("n1", "n2"));
        assertFalse(g.edgeExists("n2", "n3"));
        assertTrue(g.edgeExists("n3", "n4"));
    }

    @Test
    public void Order_Is_Perserved_When_Adding_A_Path() {
        Path p = g.addPath("n1", "n2", "n3", "n1");

        assertEquals(4, p.getNodes().length);
        assertEquals(3, p.getEdges().length);

        assertEquals(p.getNodes()[0].ID, "n1");
        assertEquals(p.getNodes()[1].ID, "n2");
        assertEquals(p.getNodes()[2].ID, "n3");
        assertEquals(p.getNodes()[3].ID, "n1");
    }

    @Test
    public void Graph_Creates_Correctly_Directed_Edges_When_Adding_Path() {
        g.addPath("a1", "a2", "a3", "a1");

        assertTrue(g.edgeExists("a1", "a2"));
        assertTrue(g.edgeExists("a2", "a3"));
        assertTrue(g.edgeExists("a3", "a1"));

        assertFalse(g.edgeExists("a2", "a1"));
        assertFalse(g.edgeExists("a3", "a2"));
        assertFalse(g.edgeExists("a1", "a3"));
    }

    @Test
    public void Order_Matters_When_Removing_A_Path() {
        g.addPath("n1", "n2", "n3", "n4");

        assertThrows(PathDoesNotExistException.class, () -> {
            g.removePath("n4", "n3", "n2", "n1");
        });

        g.addPath("a1", "a2");

        assertThrows(PathDoesNotExistException.class, () -> {
            g.removePath("a2", "a1");
        });
    }

    @Test
    public void Graph_Can_Crete_Self_Looping_Paths() {
        Path p = g.addPath("n1", "n1");
        assertEquals(p.getNodes()[0], p.getNodes()[p.getNodes().length - 1]);
        assertEquals(1, p.getEdges().length);

        Path p2 = g.addPath("a1", "a2", "a3", "a1");
        assertEquals(p2.getNodes()[0], p2.getNodes()[p2.getNodes().length - 1]);
        assertEquals(3, p2.getEdges().length);

    }

    @Test
    public void Graph_Correctly_Adds_Node_Attributes_To_Nodes_In_Path() {
        Path p1 = g.addPath("n1", "n2", "n3", "n4");
        Path p2 = g.addPath("a1", "a2");

        p1.setAttributes(Node.Attribute.COLOR, "red");
        p2.setAttributes(Node.Attribute.COLOR, "blue");

        for (Node node : p1.getNodes()) {
            assertEquals(node.getAttribute(Node.Attribute.COLOR), "red");
        }

        for (Node node : p2.getNodes()) {
            assertEquals(node.getAttribute(Node.Attribute.COLOR), "blue");
        }
    }

    @Test
    public void Graph_Correctly_Removes_Node_Attributes_From_Nodes_In_Path() {
        Path p1 = g.addPath("n1", "n2", "n3", "n4");

        p1.setAttributes(Edge.Attribute.COLOR, "red");
        p1.setAttributes(Node.Attribute.COLOR, "red");

        Node n1 = g.getNode("n1");
        n1.setAttribute(Node.Attribute.COLOR, "purple");

        p1.removeAttributes(Node.Attribute.COLOR);

        for (Node node : p1.getNodes()) {
            assertEquals(node.getAttribute(Node.Attribute.COLOR), null);
        }
    }

    @Test
    public void Graph_Correctly_Adds_Edge_Attributes_To_Edges_In_Path() {
        Path p1 = g.addPath("n1", "n2", "n3", "n4");
        Path p2 = g.addPath("a1", "a2");

        p1.setAttributes(Edge.Attribute.COLOR, "pink");
        p2.setAttributes(Edge.Attribute.COLOR, "yellow");

        for (Edge edge : p1.getEdges()) {
            assertEquals(edge.getAttribute(Edge.Attribute.COLOR), "pink");
        }

        for (Edge edge : p2.getEdges()) {
            assertEquals(edge.getAttribute(Edge.Attribute.COLOR), "yellow");
        }
    }

    @Test
    public void Graph_Correctly_Removes_Edge_Attributes_From_Edges_In_Path() {
        Path p1 = g.addPath("n1", "n2", "n3", "n4");

        p1.setAttributes(Edge.Attribute.COLOR, "red");
        p1.setAttributes(Node.Attribute.COLOR, "red");

        Edge e1 = g.getEdge("n1", "n2");
        e1.setAttribute(Edge.Attribute.COLOR, "purple");

        p1.removeAttributes(Edge.Attribute.COLOR);

        for (Edge edge : p1.getEdges()) {
            assertEquals(edge.getAttribute(Edge.Attribute.COLOR), null);
        }
    }

    @Test
    public void Graph_Correctly_Gets_Existant_Path() {
        String[] nodeIDs = new String[] { "n1", "n2", "n3", "n4" };
        g.addPath(nodeIDs);

        for (int i = 0; i < nodeIDs.length; i++) {
            for (int j = nodeIDs.length; j > i; j--) {
                String[] newNodeIDs = Arrays.copyOfRange(nodeIDs, i, j);

                Path p = g.getPath(newNodeIDs);

                for (int k = 0; k < newNodeIDs.length; k++) {
                    assertEquals(newNodeIDs[k], p.getNodes()[k].ID);
                }

            }
        }
    }

    @Test
    public void Graph_Returns_Null_When_Getting_Nonexistant_Path() {
        g.addPath("n1", "n2", "n3");
        Path p1 = g.getPath("n1", "n3");

        assertNull(p1);

        Path p2 = g.getPath("n1", "b1");
        assertNull(p2);
    }

    @Test
    public void Graph_Does_Not_Throw_Exception_When_Getting_Path_Of_Nonexistant_Nodes() {
        assertDoesNotThrow(() -> {
            Path p1 = g.getPath("n1", "n2");
            assertNull(p1);
        });
    }

    @Test
    public void Graph_Returns_Null_When_Getting_Empty_Path() {
        Path p1 = g.getPath();

        assertNull(p1);
    }

    @Test
    public void Graph_Correctly_Checks_If_Path_Exists() {
        g.addPath("n1", "n2", "n3");

        assertFalse(g.pathExists("n1", "n3"));
        assertFalse(g.pathExists("n3", "n1"));

        assertTrue(g.pathExists("n1", "n2"));
        assertTrue(g.pathExists("n2", "n3"));

        assertFalse(g.pathExists("n2", "n1"));
        assertFalse(g.pathExists("n3", "n2"));
    }

    @Test
    public void Graph_Correctly_Finds_The_Shortest_Path_Of_Length_3() {
        g.addPath("root", "1", "11");
        g.addPath("root", "1", "12");
        g.addPath("root", "2", "21");
        g.addPath("root", "2", "22");
        g.addPath("11", "22");

        Path p = g.graphSearch("root", "11", Algorithm.DFS);
        assertNotNull(p);
        Node[] expectedPath = new Node[] { g.getNode("root"), g.getNode("1"), g.getNode("11") };
        Node[] actualPath = p.getNodes();
        assertTrue(Arrays.equals(expectedPath, actualPath));

        assertTrue(p.equals(g.graphSearch("root", "11", Algorithm.BFS)));
    }

    @Test
    public void Graph_Correctly_Finds_The_Shortest_Path_Of_Length_1() {
        g.addPath("n1", "n2", "n3");
        Path p = g.graphSearch("n1", "n2", Algorithm.DFS);
        assertNotNull(p);
        Node[] expectedPath = new Node[] { g.getNode("n1"), g.getNode("n2") };
        Node[] actualPath = p.getNodes();
        assertTrue(Arrays.equals(expectedPath, actualPath));

        assertTrue(p.equals(g.graphSearch("n1", "n2", Algorithm.BFS)));
    }

    @Test
    public void Graph_Correctly_Finds_The_Shortest_Path_Of_Length_0() {
        g.addPath("n1", "n2", "n3");
        Path p = g.graphSearch("n1", "n1", Algorithm.DFS);
        assertNotNull(p);
        Node[] expectedPath = new Node[] { g.getNode("n1") };
        Node[] actualPath = p.getNodes();
        assertTrue(Arrays.equals(expectedPath, actualPath));

        assertEquals(p, g.graphSearch("n1", "n1", Algorithm.BFS));
    }

    @Test
    public void Graph_Correctly_Returns_Null_When_Finding_Nonexistant_Path() {
        g.addPath("n1", "n2", "n3");
        g.addNode("n4");
        Path p = g.graphSearch("n1", "n4", Algorithm.DFS);
        assertNull(p);

        assertEquals(p, g.graphSearch("n1", "n4", Algorithm.BFS));
    }

    @Test
    public void Graph_Throws_Exception_When_Finding_The_Path_Of_Nonexistant_Nodes() {
        g.addPath("n1", "n2", "n3");

        assertThrows(NodeDoesNotExistException.class, () -> {
            g.graphSearch("n1", "n4", Algorithm.DFS);
            g.graphSearch("n1", "n4", Algorithm.BFS);
        });

        assertThrows(NodeDoesNotExistException.class, () -> {
            g.graphSearch("n0", "n1", Algorithm.DFS);
            g.graphSearch("n0", "n1", Algorithm.BFS);
        });

        assertThrows(NodeDoesNotExistException.class, () -> {
            g.graphSearch("n0", "n4", Algorithm.DFS);
            g.graphSearch("n0", "n4", Algorithm.BFS);
        });
    }

    @Test
    public void Graph_Finds_Shortest_Path_From_Large_Graph() {
        try {
            g = Graph.parseDOT("src/test/resources/DOT/valid/veryLargeGraph/veryLargeGraph.dot");

            Path p = g.graphSearch("_6T", "N0_", Algorithm.DFS);
            assertNotNull(p);
            assertNotEquals(0, p.getNodes().length);
            assertNotEquals(0, p.getEdges().length);

            assertTrue(p.equals(g.graphSearch("_6T", "N0_", Algorithm.BFS)));

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Graph_Correctly_Finds_Shortest_Path_Using() {
        g.addPath("n1", "n2", "n3", "n4", "n5");
        g.addPath("n1", "n2");

        Path p = g.graphSearch("n1", "n2", Algorithm.DFS);
        assertNotNull(p);
        Node[] expectedPath = new Node[] { g.getNode("n1"), g.getNode("n2") };
        Node[] actualPath = p.getNodes();
        assertTrue(Arrays.equals(expectedPath, actualPath));

        assertTrue(p.equals(g.graphSearch("n1", "n2", Algorithm.BFS)));
    }

    @Test
    public void Graph_Ignores_Self_Loops_When_Finding_Shortest_Path() {
        g.addPath("n1", "n1");

        Path p = g.graphSearch("n1", "n1", Algorithm.DFS);
        assertNotNull(p);
        Node[] expectedPath = new Node[] { g.getNode("n1") };
        Node[] actualPath = p.getNodes();
        assertTrue(Arrays.equals(expectedPath, actualPath));

        Edge[] expectedEdges = new Edge[] {};
        Edge[] actualEdges = p.getEdges();
        System.out.println(Arrays.toString(expectedEdges));
        System.out.println(Arrays.toString(actualEdges));
        assertTrue(Arrays.equals(expectedEdges, actualEdges));

        assertTrue(p.equals(g.graphSearch("n1", "n1", Algorithm.BFS)));
    }

    @Test
    public void Graph_Finds_All_Paths_Of_Connected_Graph() {
        g = Graph.parseDOT("src/test/resources/Search/Connected5Graph.dot");

        for (Node fromNode : g.getNodes()) {
            for (Node toNode : g.getNodes()) {
                Path p = g.graphSearch(fromNode.ID, toNode.ID, Algorithm.DFS);
                assertNotNull(p);

                assertTrue(p.equals(g.graphSearch(fromNode.ID, toNode.ID, Algorithm.BFS)));
            }
        }
    }

    @Test
    public void Graph_Returns_Null_When_Finding_Nonexistant_Path() {
        g.addPath("n1", "n2", "n3");
        g.addPath("a1", "a2", "a3");

        Path p = g.graphSearch("n1", "a1", Algorithm.DFS);
        assertNull(p);
        assertNull(g.graphSearch("n1", "a1", Algorithm.BFS));
    }

    @Test
    public void Graph_Finds_Zero_Paths_From_Disconnected_Graph() {
        g = Graph.parseDOT("src/test/resources/Search/Disconnected5Graph.dot");

        for (Node fromNode : g.getNodes()) {
            for (Node toNode : g.getNodes()) {
                if (fromNode.equals(toNode)) {
                    continue;
                }

                Path p = g.graphSearch(fromNode.ID, toNode.ID, Algorithm.DFS);
                assertNull(p);

                assertNull(g.graphSearch(fromNode.ID, toNode.ID, Algorithm.BFS));
            }
        }
    }

    @Test
    public void Path_To_String_Works_Properly() {
        Path p = g.addPath("n1", "n2");
        assertNotNull(p.toString());
    }

    @Test
    public void BFS_Always_Finds_Shorter_Or_Equal_Length_Path_As_Compared_To_DFS() {
        try (DirectoryStream<java.nio.file.Path> stream = Files
                .newDirectoryStream(java.nio.file.Path.of("src/test/resources/DOT/valid"))) {
            for (java.nio.file.Path filepath : stream) {
                Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(filepath));

                for (Node fromNode : g.getNodes()) {
                    for (Node toNode : g.getNodes()) {
                        Path pathDFS = g.graphSearch(fromNode.ID, toNode.ID, Algorithm.DFS);
                        Path pathBFS = g.graphSearch(fromNode.ID, toNode.ID, Algorithm.BFS);

                        if (pathDFS != null && pathBFS != null) {
                            assertTrue(pathBFS.getNodes().length <= pathDFS.getNodes().length);
                        } else {
                            assertNull(pathDFS);
                            assertNull(pathBFS);
                        }
                    }
                }
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}