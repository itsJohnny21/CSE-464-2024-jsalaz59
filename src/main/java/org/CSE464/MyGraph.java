package org.CSE464;

import org.jgrapht.graph.*;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.nio.ImportException;
import org.jgrapht.nio.Attribute;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MyGraph extends DirectedPseudograph<String, DefaultEdge> {
    private final HashMap<String, HashMap<String, String>> attributes = new HashMap<>();

    public MyGraph(Class<DefaultEdge> edgeClass) {
        super(edgeClass);
    }

    private static MyGraph parseGraph(String filepath) {
        MyGraph graph = new MyGraph(DefaultEdge.class);

        DOTImporter<String, DefaultEdge> dotImporter = new DOTImporter<>();
        dotImporter.setVertexFactory(name -> name);
        dotImporter.addVertexAttributeConsumer((pair, attr) -> {
            String vertex = pair.getFirst();
            String attributeName = pair.getSecond();
            String attributeValue = attr.getValue();

            if (!graph.attributes.containsKey(vertex)) {
                graph.attributes.put(vertex, new HashMap<>());
            }

            graph.attributes.get(vertex).put(attributeName, attributeValue);
        });

        try (Reader dotFileReader = new FileReader(filepath)) {
            dotImporter.importGraph(graph, dotFileReader);
        } catch (ImportException | IOException e) {
            e.printStackTrace();
        }

        return graph;
    }

    public String edgeDirections() {
        if (this.edgeSet().size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder("[");
        for (DefaultEdge edge : this.edgeSet()) {
            sb.append(this.getEdgeSource(edge)).append(" -> ").append(this.getEdgeTarget(edge)).append(", ");
        }

        sb.replace(sb.length() - 2, sb.length() , "");
        sb.append("]");
        return sb.toString();
    }

    public String nodeLabels() {
        StringBuilder sb = new StringBuilder();
        String longestNodeName = this.vertexSet()
                .stream()
                .max(Comparator.comparingInt(String::length))
                .orElse(null);

        String longestNodeLabel = nodeLabel(this.vertexSet()
                .stream()
                .max((node1, node2) -> Integer.compare(nodeLabel(node1).length(), nodeLabel(node2).length()))
                .orElse(null));

        assert longestNodeName != null;
        int nameWidth = Math.max("Node".length(), longestNodeName.length()) + 3;
        int labelWidth = Math.max("Label".length(), longestNodeLabel.length()) + 3;

        sb.append(String.format("%-" + nameWidth + "s | %-" + labelWidth + "s\n", "Node", "Label"));
        sb.append("—".repeat(nameWidth + labelWidth)).append("\n");

        for (String node : this.vertexSet()) {
            sb.append(String.format("%-" + nameWidth + "s | %-" + labelWidth + "s\n", node, nodeLabel(node)));
        }

        return sb.substring(0, sb.length() - 1);
    }

    public String nodeLabel(String node) {
        if (this.attributes.containsKey(node) && this.attributes.get(node).containsKey("label")) {
            return this.attributes.get(node).get("label");
        } else {
            return "";
        }
    }

    public static void outputGraph(MyGraph graph, String filepath) {
        DOTExporter<String, DefaultEdge> dotExporter = new DOTExporter<>();

        dotExporter.setVertexIdProvider(name -> name);

        dotExporter.setVertexAttributeProvider(name -> {
            Map<String, Attribute> attributes = new LinkedHashMap<>();

            if (!graph.nodeLabel(name).isEmpty()) {
                attributes.put("label", DefaultAttribute.createAttribute(graph.nodeLabel(name)));
            }
            return attributes;
        });

        dotExporter.exportGraph(graph, new File(filepath));
    }

    @Override
    public String toString() {
        return
                "Number of nodes: " + this.vertexSet().size()
                +"\n" + nodeLabels()
                +"\nNumber of edges: " + this.edgeSet().size()
                +"\nNodes: " + this.vertexSet()
                +"\nEdge Directions: " + this.edgeDirections()
                ;
    }

    public static void main(String[] args) {
        MyGraph graph = MyGraph.parseGraph("/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/main/resources/graph1.dot");
        System.out.println(graph);
        MyGraph.outputGraph(graph, "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/main/resources/graph1-output.dot");
        System.out.println("Woohoo🥳😀");
    }
}

// TODO: JUnit testing to guarantee correctness.
// TODO: Read JGraphtT documentation to understand how the DOTParser and Graph interface works.
// TODO: Complete README.md file.
// TODO: Ask instructor if using JGraphT this is way is allowed.
