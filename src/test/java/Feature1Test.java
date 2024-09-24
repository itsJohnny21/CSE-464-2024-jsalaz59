import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.CSE464.MyGraph;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.model.MutableNode;

public class Feature1Test {
    private final static Path resourcesFilepath = Paths.get("src/test/resources");
    private final static Path DOTPath = Paths.get(resourcesFilepath.toString(), "DOT");
    private final static Path tmpPath = Paths.get("src/test/tmp");
    private final static Path DOTValidPath = Paths.get(DOTPath.toString(), "valid");
    private final static Path DOTInvalidPath = Paths.get(DOTPath.toString(), "invalid");

    private final static Path nodesX_Y_Z = Paths.get(DOTValidPath.toString(),
            "nodesX_Y_Z");
    private final static Path emptyGraph = Paths.get(DOTValidPath.toString(),
            "emptyGraph");
    private final static Path someNodesZeroEdges = Paths.get(DOTValidPath.toString(),
            "someNodesZeroEdges");
    private final static Path nodeIDsWithLettersOnly = Paths.get(DOTValidPath.toString(),
            "nodeIDsWithLettersOnly");
    private final static Path nodeIDsWithNumbers = Paths.get(DOTValidPath.toString(),
            "nodeIDsWithNumbers");
    private final static Path nodeIDsWithUnderscores = Paths.get(DOTValidPath.toString(),
            "nodeIDsWithUnderscores");
    private final static Path nodesX_Y_ZLabeled = Paths.get(DOTValidPath.toString(),
            "nodesX_Y_ZLabeled");
    private final static Path threeNodes = Paths.get(DOTValidPath.toString(),
            "threeNodes");
    private final static Path fourEdges = Paths.get(DOTValidPath.toString(),
            "fourEdges");
    private final static Path circularABC = Paths.get(DOTValidPath.toString(),
            "circularABC");
    private final static Path selfLoopEdges = Paths.get(DOTValidPath.toString(),
            "selfLoopEdges");
    private final static Path veryLargeGraph = Paths.get(DOTValidPath.toString(),
            "veryLargeGraph");
    private final static Path invalidNodeSyntax = Paths.get(DOTInvalidPath.toString(),
            "invalidNodeSyntax");
    private final static Path invalidEdgeSyntax = Paths.get(DOTInvalidPath.toString(),
            "invalidEdgeSyntax");
    private final static Path invalidSemicolon = Paths.get(DOTInvalidPath.toString(),
            "invalidSemicolon");
    private final static Path graphTypeTypo = Paths.get(DOTInvalidPath.toString(),
            "graphTypeTypo");
    private final static Path bracketIncomplete = Paths.get(DOTInvalidPath.toString(),
            "bracketIncomplete");

    @BeforeAll
    public static void verifyEnvironment() {
        try {
            Utils.removeDirectory(tmpPath, true);
            Utils.createDirectory(tmpPath, true);

            DirectoryStream<Path> stream = Files.newDirectoryStream(DOTValidPath);
            int maxFileCount = 0;

            for (Path testDirectory : stream) {
                int fileCount = testDirectory.getNameCount();
                maxFileCount = Math.max(maxFileCount, fileCount);

                String DOTFilepath = Utils.getDOTFilepathFromTestDirectory(testDirectory);
                String filename1 = Paths.get(DOTFilepath).getFileName().toString().replace(".dot", "");
                String filename2 = testDirectory.getFileName().toString();
                assertEquals(filename1, filename2, "DOT file and its parent directory should have the same name.");
            }

            assertNotEquals(0, maxFileCount);

        } catch (

        Exception e) {
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
    public void Should_Have_parseGraph_Method() {
        try {
            Utils.hasMethod(MyGraph.class, "parseGraph", String.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Should_Have_outputGraph_Method() {
        try {
            Utils.hasMethod(MyGraph.class, "outputGraph", String.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Should_Have_Overriden_toString_Method() {
        try {
            Utils.hasMethod(MyGraph.class, "toString");
            Method toStringMethod = MyGraph.class.getMethod("toString");
            assertTrue(!toStringMethod.getDeclaringClass().equals(MyGraph.class.getGenericSuperclass()),
                    "The toString() method was not overriden from its declaring class.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Should_Have_Zero_Public_Constructors() {
        try {
            int constructorQty = MyGraph.class.getConstructors().length;
            assertEquals(constructorQty, 0, "MyGraph should have zero public constructors.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Valid_DOT_File_Can_Parse() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_Z));
            assertNotNull(g, "Parsing error.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Valid_DOT_File_With_Labels_Can_Parse() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_ZLabeled));
            assertNotNull(g, "Parsing error.");

            for (MutableNode node : g.nodes()) {
                assertNotNull(node.attrs().get("label"), "Label was not parsed.");
            }

            HashMap<String, String> expectedNodeLabels = new HashMap<>();
            expectedNodeLabels.put("X", "Node X");
            expectedNodeLabels.put("Y", "Node Y");
            expectedNodeLabels.put("Z", "Node Z");

            HashMap<String, String> actualNodeLabels = g.getNodeLabels();

            assertEquals(expectedNodeLabels, actualNodeLabels, "Incorrect node label parsing.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Valid_DOT_File_With_Edges_Can_Parse() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(fourEdges));
            assertNotNull(g, "Parsing error.");

            Set<String> exptectedEdgeDirections = Set.of(
                    "A -> B",
                    "B -> C",
                    "C -> D",
                    "D -> A");

            Set<String> actualEdgeDirections = g.getEdgeDirections();

            assertEquals(exptectedEdgeDirections, actualEdgeDirections, "Incorrect node label parsing.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Invalid_DOT_File_With_Syntax_Error_Should_Not_Parse() {
        try {
            assertThrows(RuntimeException.class, () -> {
                MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(invalidSemicolon));
            }, "The DOT file should not be able to be parsed due to invalid syntax.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void After_Parsing_Can_Be_Deep_Copied() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_Z));
            assertNotNull(g, "Parse error.");
            
            MyGraph g2 = g.copy();
            assertNotEquals(System.identityHashCode(g), System.identityHashCode(g2),
                    "The copied graph should not be the same instance as the original graph.");

            assertEquals(g.toString(), g2.toString(),
                    "The copied graph should have the same state as the original.");

            String distinguisher = "😏😏";
            g2.setName(g.name().toString() + distinguisher);

            assertNotEquals(g.name().toString(), g2.name().toString(),
                    "After changing the name of the copied graph to distinguish itself from the original graph, its states should also be different from the original graph.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void After_Parsing_Can_Output_To_PNG() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_Z));
            Path outputPath = Paths.get(tmpPath.toString(), "output.png");
            g.outputGraph(outputPath.toString());
            assertTrue(Files.exists(outputPath));
            Files.deleteIfExists(outputPath);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void After_Parsing_Can_Output_To_DOT() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_Z));
            Path outputPath = Paths.get(tmpPath.toString(), "output.dot");
            g.outputGraph(outputPath.toString(), Format.DOT);
            assertTrue(Files.exists(outputPath));
            Files.deleteIfExists(outputPath);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void After_Parsing_Can_Output_To_PNG_Statically() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(someNodesZeroEdges));
            Path outputPath = Paths.get(tmpPath.toString(), "output.png");
            MyGraph.outputGraph(g, outputPath.toString(), Format.PNG);
            assertTrue(Files.exists(outputPath));
            Files.deleteIfExists(outputPath);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void After_Parsing_Can_Output_To_DOT_Statically() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(someNodesZeroEdges));
            Path outputPath = Paths.get(tmpPath.toString(), "output.dot");
            MyGraph.outputGraph(g, outputPath.toString(), Format.DOT);
            assertTrue(Files.exists(outputPath));
            Files.deleteIfExists(outputPath);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Empty_Graph_Can_Parse() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(emptyGraph));
            assertNotNull(g, "Parsing error.");

            int expectedNumNodes = 0;
            int acutalNumNodes = g.getNumberOfNodes();
            assertEquals(expectedNumNodes, acutalNumNodes, "There should be zero nodes.");

            int expectedNumEdges = 0;
            int acutalNumEdges = g.getNumberOfEdges();
            assertEquals(expectedNumEdges, acutalNumEdges, "There should be zero edges.");

            Set<String> expcetedNodeNames = new HashSet<String>();
            Set<String> actualNodeNames = g.getNodeNames();
            assertEquals(expcetedNodeNames, actualNodeNames, "Set of node names should be empty.");

            Set<String> expcetedEdges = new HashSet<String>();
            Set<String> actualEdges = g.getEdgeDirections();
            assertEquals(expcetedEdges, actualEdges, "Set of edges should be empty.");

            for (MutableNode node : g.nodes()) {

                assertNotNull(node.attrs().get("label"), "Label was not parsed.");
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Very_Large_Graph_Can_Parse() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(veryLargeGraph));
            assertNotNull(g, "Parsing error.");

            for (MutableNode node : g.nodes()) {
                assertNotNull(node.attrs().get("label"), "Label was not parsed.");
            }

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
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(threeNodes));
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
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(fourEdges));
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
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(someNodesZeroEdges));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Circular_A_To_B_To_C_Can_Parse() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(circularABC));
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
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(nodesX_Y_Z));
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
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(nodeIDsWithLettersOnly));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Node_IDs_Using_Underscore_Can_Parse() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(nodeIDsWithUnderscores));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Node_IDs_Using_Numbers_Can_Parse() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(nodeIDsWithNumbers));
            assertNotNull(g, "The valid DOT file should have been parsed.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Invalid_Node_ID_Should_Not_Parse() {
        try {
            assertThrows(RuntimeException.class, () -> {
                MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(invalidNodeSyntax));
            }, "A DOT file with invalid node IDs should be parsed successfully.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Invalid_Edge_Syntax_Should_Not_Parse() {
        try {
            assertThrows(RuntimeException.class, () -> {
                MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(invalidEdgeSyntax));
            }, "A DOT file with invalid edge syntax should be parsed successfully.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_Self_Loops_Can_Parse() {
        try {
            MyGraph g = MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(selfLoopEdges));
            assertNotNull(g, "The valid DOT file should have been parsed.");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_A_Typo_In_Graph_Type_Should_Not_Parse() {
        try {
            assertThrows(RuntimeException.class, () -> {
                MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(graphTypeTypo));
            }, "A DOT file with invalid a typo should not be able to be parsed.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void Given_A_Graph_With_A_Bracket_Syntax_Error_Should_Not_Parse() {
        try {
            assertThrows(RuntimeException.class, () -> {
                MyGraph.parseGraph(Utils.getDOTFilepathFromTestDirectory(bracketIncomplete));
            }, "A DOT file with invalid a typo should not be able to be parsed.");

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
