package org.CSE464;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MyGraph extends MutableGraph {

    protected MyGraph(boolean strict, boolean directed, boolean cluster, Label name, LinkedHashSet<MutableNode> nodes, LinkedHashSet<MutableGraph> subgraphs, List<Link> links, @Nullable Attributes<? extends ForNode> nodeAttrs, @Nullable Attributes<? extends ForLink> linkAttrs, @Nullable Attributes<? extends ForGraph> graphAttrs) {
        super(strict, directed, cluster, name, nodes, subgraphs, links, nodeAttrs, linkAttrs, graphAttrs);
    }

    public static MyGraph fromMutableGraph(MutableGraph g) {
        return new MyGraph(g.isStrict(), g.isDirected(), g.isCluster(), g.name(), new LinkedHashSet<>(g.nodes()), new LinkedHashSet<>(g.graphs()), g.links(), g.nodeAttrs(), g.linkAttrs(), g.graphAttrs());
    }

    public static MyGraph parseGraph(String filepath) {
        try (InputStream dot = MyGraph.class.getResourceAsStream(filepath)) {
            assert dot != null;
            return fromMutableGraph(new Parser().read(new File(filepath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void outputGraph(MyGraph g, String filepath) throws IOException {
        Graphviz.fromGraph(g).render(Format.PNG).toFile(new File(filepath));
    }

    public String nodeLabels() {
        String longestNodeName = Objects.requireNonNull(this.nodes()
                .stream()
                .max(Comparator.comparingInt(node -> node.name().toString().length()))
                .orElse(null)).name().toString();

        String longestNodeLabel = Objects.requireNonNull(this.nodes()
                .stream()
                .max(Comparator.comparingInt(node -> Objects.requireNonNull(node.attrs().get("label")).toString().length()))
                .orElse(null)).name().toString();

        int nameWidth = Math.max("Node".length(), longestNodeName.length()) + 3;
        int labelWidth = Math.max("Label".length(), longestNodeLabel.length()) + 3;

        String header = String.format("%-" + nameWidth + "s | %-" + labelWidth + "s\n", "Node", "Label") +
                "—".repeat(nameWidth + labelWidth);

        String rows = this.nodes().stream()
                .map(node -> String.format("\n%-" + nameWidth + "s | %-" + labelWidth + "s",
                        node.name(),
                        node.attrs().get("label") != null ? Objects.requireNonNull(node.attrs().get("label")).toString() : "No Label"))
                .collect(Collectors.joining());

        return header + rows;
    }

    public String edgeDirections() {
        return this.edges().stream()
                .map(edge -> {
                    assert edge.from() != null;
                    return edge.from().name() + " -> " + edge.to().name();
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String nodeNames() {
        return "[" + this.nodes().stream()
                .map(node -> node.name().toString())
                .collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public String toString() {
        return String.format(
                "Number of nodes: " + this.nodes().size()
                        + "\n" + this.nodeLabels()
                        + "\nNumber of edges: " + this.edges().size()
                        + "\nNodes: " + this.nodeNames()
                        + "\nEdge Directions: " + this.edgeDirections()
        );
    }

    public static void main(String[] args) {
        MyGraph g = MyGraph.parseGraph("/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/main/resources/graph1.dot");
        System.out.println(g);
        try {
            MyGraph.outputGraph(g, "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/main/resources/graph1.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Woohoo🥳😀");
    }
}