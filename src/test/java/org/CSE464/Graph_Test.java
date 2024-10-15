package org.CSE464;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.Utils;
import org.CSE464.Graph.Node;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Graph_Test {

    private final static Path resourcesFilepath = Paths.get("src/test/resources");
    private final static Path DOTPath = Paths.get(resourcesFilepath.toString(), "DOT");
    private final static Path tmpPath = Paths.get("src/test/tmp");
    private final static Path DOTValidPath = Paths.get(DOTPath.toString(), "valid");
    private final static Path DOTInvalidPath = Paths.get(DOTPath.toString(), "invalid");

    private final static Path nodesX_Y_Z = Paths.get(DOTValidPath.toString(), "nodesX_Y_Z");
    private final static Path emptyGraph = Paths.get(DOTValidPath.toString(), "emptyGraph");
    private final static Path someNodesZeroEdges = Paths.get(DOTValidPath.toString(), "someNodesZeroEdges");
    private final static Path nodeIDsWithLettersOnly = Paths.get(DOTValidPath.toString(), "nodeIDsWithLettersOnly");

    private final static Path nodeIDsWithNumbers = Paths.get(DOTValidPath.toString(), "nodeIDsWithNumbers");
    private final static Path nodeIDsWithUnderscores = Paths.get(DOTValidPath.toString(), "nodeIDsWithUnderscores");
    private final static Path nodesX_Y_ZLabeled = Paths.get(DOTValidPath.toString(), "nodesX_Y_ZLabeled");
    private final static Path threeNodes = Paths.get(DOTValidPath.toString(), "threeNodes");
    private final static Path fourEdges = Paths.get(DOTValidPath.toString(), "fourEdges");
    private final static Path circularABC = Paths.get(DOTValidPath.toString(), "circularABC");
    private final static Path selfLoopEdges = Paths.get(DOTValidPath.toString(), "selfLoopEdges");
    private final static Path veryLargeGraph = Paths.get(DOTValidPath.toString(), "veryLargeGraph");
    private final static Path graphWithAttributes = Paths.get(DOTValidPath.toString(), "graphWithAttributes");
    private final static Path invalidNodeSyntax = Paths.get(DOTInvalidPath.toString(), "invalidNodeSyntax");
    private final static Path invalidEdgeSyntax = Paths.get(DOTInvalidPath.toString(), "invalidEdgeSyntax");
    private final static Path invalidSemicolon = Paths.get(DOTInvalidPath.toString(), "invalidSemicolon");
    private final static Path graphTypeTypo = Paths.get(DOTInvalidPath.toString(), "graphTypeTypo");
    private final static Path bracketIncomplete = Paths.get(DOTInvalidPath.toString(), "bracketIncomplete");

    @BeforeAll
    public static void verifyEnvironment() {
        try {
            Utils.removeDirectory(tmpPath, true);
            Utils.createDirectory(tmpPath, true);

            DirectoryStream<Path> stream = Files.newDirectoryStream(DOTValidPath);
            int DOTFileCount = 0;

            for (Path testDirectory : stream) {
                int fileCount = testDirectory.getNameCount();
                DOTFileCount += fileCount;

                String DOTFilepath = Utils.getDOTFilepathFromTestDirectory(testDirectory);
                String filename1 = Paths.get(DOTFilepath).getFileName().toString().replace(".dot", "");
                String filename2 = testDirectory.getFileName().toString();
                assertEquals(filename1, filename2, "DOT file and its parent directory should have the same name.");
            }
            stream.close();

            assertEquals(84, DOTFileCount, "There should be 78 files in the DOT Valid path.");

        } catch (Exception e) {
            fail(e.getMessage());

        }
    }

    @AfterAll
    public static void cleanEnvironment() {
        try {
            Utils.removeDirectory(tmpPath, true);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Graph_Can_Be_Constructed_Without_An_ID() {
        Graph g = new Graph();
        assertNotNull(g);

        assertNull(g.getID());
    }

    @Test
    public void Graph_Can_Be_Constructed_With_An_ID() {
        String graphID = "test";
        Graph g = new Graph(graphID);
        assertNotNull(g);

        assertEquals(g.getID(), graphID);
    }

    @Test
    public void Given_A_Valid_DOT_File_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_Z));
            assertNotNull(g, "Parsing error.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Valid_DOT_File_With_Labels_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_ZLabeled));
            assertNotNull(g, "Parsing error.");

            for (Node node : g.getNodes().values()) {
                assertNotNull(node.getAttribute("label"), "Label was not parsed.");
            }

            Set<String> expectedNodeLabels = Set.of("X=Node X", "Y=Node Y", "Z=Node Z");

            Set<String> actualNodeLabels = g.getNodeLabels();

            assertEquals(expectedNodeLabels, actualNodeLabels, "Incorrect node label parsing.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Valid_DOT_File_With_Edges_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(fourEdges));
            assertNotNull(g, "Parsing error.");

            Set<String> exptectedEdgeDirections = Set.of("A -> B", "B -> C", "C -> D", "D -> A");

            Set<String> actualEdgeDirections = g.getEdgeDirections();

            assertEquals(exptectedEdgeDirections, actualEdgeDirections, "Incorrect node label parsing.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Invalid_DOT_File_With_Syntax_Error_Should_Not_Parse() {
        try {
            assertThrows(ParseException.class, () -> {
                Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(invalidSemicolon));
            }, "The DOT file should not be able to be parsed due to invalid syntax.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void After_Parsing_Can_Output_To_PNG() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_Z));
            Path outputPath = Paths.get(tmpPath.toString(), "output.png");
            g.outputGraph(outputPath.toString(), Format.PNG);
            assertTrue(Files.exists(outputPath));
            Files.deleteIfExists(outputPath);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void After_Parsing_Can_Output_To_DOT() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_Z));
            Path outputPath = Paths.get(tmpPath.toString(), "output.dot");
            g.outputGraph(outputPath.toString(), Format.DOT);
            assertTrue(Files.exists(outputPath));
            Files.deleteIfExists(outputPath);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Empty_Graph_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(emptyGraph));
            assertNotNull(g, "Parsing error.");

            int expectedNumNodes = 0;
            int acutalNumNodes = g.getNumberOfNodes();
            assertEquals(expectedNumNodes, acutalNumNodes, "There should be zero nodes.");

            int expectedNumEdges = 0;
            int acutalNumEdges = g.getNumberOfEdges();
            assertEquals(expectedNumEdges, acutalNumEdges, "There should be zero edges.");

            Set<String> expcetedNodeNames = new HashSet<>();
            Set<String> actualNodeNames = g.getNodeNames();
            assertEquals(expcetedNodeNames, actualNodeNames, "Set of node names should be empty.");

            Set<String> expcetedEdges = new HashSet<>();
            Set<String> actualEdges = g.getEdgeDirections();
            assertEquals(expcetedEdges, actualEdges, "Set of edges should be empty.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Very_Large_Graph_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(veryLargeGraph));
            assertNotNull(g, "Parsing error.");
            for (Node node : g.getNodes().values()) {
                assertNotNull(node.getAttribute("label"), "Label was not parsed.");
            }

            Path outputPath = Paths.get(tmpPath.toString(), "output.png");
            g.outputGraph(outputPath.toString(), Format.PNG);
            int expectedNumNodes = 999;
            int actualNumNodes = g.getNumberOfNodes();
            assertEquals(expectedNumNodes, actualNumNodes, "Incorrect node count.");

            int expectedNumEdges = 999;
            int actualNumEdges = g.getNumberOfEdges();
            assertEquals(expectedNumEdges, actualNumEdges, "Incorrect edge count.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_3_Nodes_Can_Count_Number_Of_Nodes() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(threeNodes));
            final int actual = g.getNumberOfNodes();
            final int expected = 3;
            assertEquals(expected, actual, "Incorrect node count.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_4_Edges_Can_Count_Number_Of_Edges() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(fourEdges));
            final int actual = g.getNumberOfEdges();
            final int expected = 4;
            assertEquals(expected, actual, "Incorrect edge count.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Some_Nodes_And_Zero_Edges_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(someNodesZeroEdges));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_With_Attributes_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(graphWithAttributes));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Circular_A_To_B_To_C_Graph_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(circularABC));
            assertNotNull(g, "The valid DOT file should have been parsed.");
            final Set<String> actual = g.getNodeNames();
            final Set<String> expected = Set.of("A", "B", "C");
            assertTrue(!actual.isEmpty(), "Node names were not parsed.");
            assertEquals(expected, actual, "Node names not parsed properly.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Nodes_X_Y_Z_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_Z));
            final Set<String> actual = g.getNodeNames();
            final Set<String> expected = Set.of("X", "Y", "Z");
            assertTrue(!actual.isEmpty(), "Node names were not parsed.");
            assertEquals(expected, actual, "Node names not parsed properly.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Node_IDs_Using_Only_Letters_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodeIDsWithLettersOnly));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Node_IDs_Using_Underscore_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodeIDsWithUnderscores));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Node_IDs_Using_Numbers_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodeIDsWithNumbers));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Invalid_Node_ID_Should_Not_Parse() {
        try {
            assertThrows(ParseException.class, () -> {
                Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(invalidNodeSyntax));
            }, "A DOT file with invalid node IDs should be parsed successfully.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Invalid_Edge_Syntax_Should_Not_Parse() {
        try {
            assertThrows(ParseException.class, () -> {
                Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(invalidEdgeSyntax));
            }, "A DOT file with invalid edge syntax should be parsed successfully.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Self_Loops_Can_Parse() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(selfLoopEdges));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_A_Typo_In_Graph_Type_Should_Not_Parse() {
        try {
            assertThrows(RuntimeException.class, () -> {
                Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(graphTypeTypo));
            }, "A DOT file with invalid a typo should not be able to be parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_A_Bracket_Syntax_Error_Should_Not_Parse() {
        try {
            assertThrows(RuntimeException.class, () -> {
                Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(bracketIncomplete));
            }, "A DOT file with invalid a typo should not be able to be parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_All_Valid_DOT_Files_Can_Parse() {
        int graphsParsed = 0;
        int expectedGraphsParsed = 14;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(DOTValidPath)) {
            for (Path p : stream) {
                assertTrue(p.getNameCount() != 0, "The test directory should not be empty.");
                Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(p));
                assertNotNull(g, "The valid DOT file should have been parsed.");
                graphsParsed += 1;
            }
            assertEquals(expectedGraphsParsed, graphsParsed,
                    "A total of " + expectedGraphsParsed + " tests should have been performed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void All_Graph_Attributes_Work() {
        Graph g = new Graph();
        for (Graph.Attribute attribute : Graph.Attribute.values()) {
            assertDoesNotThrow(() -> {
                g.setAttribute(attribute, "some value");
                g.getAttribute(attribute);
                g.removeAttribute(attribute);
            });
            g.setAttribute(attribute.getValue(), "some value");
        }

        assertEquals(Graph.Attribute.values().length, g.getAttributes().size());
    }

    @Test
    public void All_Output_Formats_Work() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_ZLabeled));

            for (Format format : Format.values()) {
                assertNotNull(format.getValue());
                assertNotNull(format.getExtension());

                String path = Path.of(tmpPath.toString(), String.format("tmp_file%s", format.extension)).toString();
                g.outputGraph(path, format);
                assertTrue(Files.exists(Path.of(path)));
            }

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Graph_Can_Describe_Itself() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_ZLabeled));
            String graphDescription = g.describe();
            assertTrue(graphDescription.contains("Node X"));
            assertTrue(graphDescription.contains("Node Y"));
            assertTrue(graphDescription.contains("Node Z"));
            assertTrue(graphDescription.contains(String.valueOf(g.getNumberOfNodes())));
            assertTrue(graphDescription.contains(String.valueOf(g.getNumberOfEdges())));

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Graph_To_String_Shows_ID_Nodes_Edges_And_Attributes() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_ZLabeled));

            for (Entry<String, String> entry : g.getAttributes().entrySet()) {
                String attribute = entry.getKey();
                String value = entry.getValue();

                assertNotNull(g.toString());
                assertFalse(g.toString().isEmpty());
                assertTrue(g.toString().contains(attribute));
                assertTrue(g.toString().contains(value));

                assertNotNull(g.toDot());
                assertFalse(g.toDot().isEmpty());
                assertTrue(g.describe().contains(attribute));
                assertTrue(g.describe().contains(value));
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Graph_Can_Output_To_RAWDOT() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_ZLabeled));

            String path = Path.of(tmpPath.toString(), String.format("tmp_file%s", Format.RAWDOT.extension)).toString();
            String dotContent = g.outputGraph(path, Format.RAWDOT);
            assertNotNull(dotContent);
            assertFalse(dotContent.isEmpty());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Graph_Can_Not_Output_To_SVG_With_Bad_Options() {
        try {
            Graph g = Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_ZLabeled));

            String path = Path.of(tmpPath.toString(), String.format("tmp_file%s", Format.SVG.extension)).toString();

            assertThrows(ParseException.class, () -> {
                g.outputGraph(path, Format.SVG, "bad-option!");

            });

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_All_Invalid_DOT_Files_Should_Not_Parse() {
        int graphsParsed = 0;
        int expectedGraphsParsed = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(DOTValidPath)) {
            for (Path p : stream) {
                assertTrue(p.getNameCount() != 0, "The test directory should not be empty.");
                ParseException e = assertThrows(ParseException.class, () -> {
                    Graph.parseDOT(Utils.getDOTFilepathFromTestDirectory(bracketIncomplete));
                }, "A DOT file with an invalid typo should not be able to be parsed.");
                if (e == null) {
                    graphsParsed += 1;
                }
            }
            assertEquals(expectedGraphsParsed, graphsParsed,
                    "A total of " + expectedGraphsParsed + " tests should have been performed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
