package org.CSE464;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Nullable;

import guru.nidi.graphviz.attribute.Attributes;
import guru.nidi.graphviz.attribute.ForGraph;
import guru.nidi.graphviz.attribute.ForLink;
import guru.nidi.graphviz.attribute.ForNode;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;

public class MyGraph extends MutableGraph {

    protected MyGraph(boolean strict, boolean directed, boolean cluster, Label name, LinkedHashSet<MutableNode> nodes,
            LinkedHashSet<MutableGraph> subgraphs, List<Link> links, @Nullable Attributes<? extends ForNode> nodeAttrs,
            @Nullable Attributes<? extends ForLink> linkAttrs, @Nullable Attributes<? extends ForGraph> graphAttrs) {
        super(strict, directed, cluster, name, nodes, subgraphs, links, nodeAttrs, linkAttrs, graphAttrs);
    }

    public static MyGraph fromMutableGraph(MutableGraph g) {
        return new MyGraph(g.isStrict(), g.isDirected(), g.isCluster(), g.name(), new LinkedHashSet<>(g.nodes()),
                new LinkedHashSet<>(g.graphs()), g.links(), g.nodeAttrs(), g.linkAttrs(), g.graphAttrs());
    }

    public static MyGraph parseGraph(String filepath) {
        try (InputStream dot = MyGraph.class.getResourceAsStream(filepath)) {
            assert dot != null;
            MyGraph g = fromMutableGraph(new Parser().read(new File(filepath)));
            Graphviz.fromGraph(g).width(700).render(Format.PNG).toFile(new File("trash/ex4-1.png"));
            return g;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String edgeDirections() {
        if (this.edges().size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder("[");
        for (Link edge : this.edges()) {
            assert edge.from() != null;
            sb.append(edge.from().name()).append(" -> ").append(edge.to().name()).append(", ");
        }

        sb.replace(sb.length() - 2, sb.length(), "");
        sb.append("]");
        return sb.toString();
    }

    public static void main(String[] args) {
        MyGraph g = MyGraph.parseGraph(
                "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/main/resources/graph1.dot");
        System.out.print(
                "Number of nodes: " + g.nodes().size()
                        + "\nNodes: " + g.nodes()
                        + "\nNumber of edges: " + g.edges().size()
                        + "\nEdge Directions: " + g.edgeDirections());
        //        System.out.println(g.name());
        //        System.out.println(g.nodes().size());
        //
        //        g.edges().forEach(e -> {
        //            assert e.from() != null;
        //            System.out.printf("%s -> %s%n", e.from().name(), e.to().name());
        //        });

    }
}

/*
Number of nodes: 3
Node    | Label
—————————————————
a       | apple
b       | banana
c       | coconut
Number of edges: 4
Nodes: [a, b, c]
Edge Directions: [a -> b, b -> b, b -> c, c -> a]
Woohoo🥳😀
 */