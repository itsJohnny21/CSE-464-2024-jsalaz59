package org.CSE464;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    private MyGraph(boolean strict, boolean directed, boolean cluster, Label name, LinkedHashSet<MutableNode> nodes,
            LinkedHashSet<MutableGraph> subgraphs, List<Link> links, @Nullable Attributes<? extends ForNode> nodeAttrs,
            @Nullable Attributes<? extends ForLink> linkAttrs, @Nullable Attributes<? extends ForGraph> graphAttrs) {
        super(strict, directed, cluster, name, nodes, subgraphs, links, nodeAttrs, linkAttrs, graphAttrs);
    }

    private MyGraph(MutableGraph g) {
        super(g.isStrict(), g.isDirected(), g.isCluster(), g.name(), new LinkedHashSet<>(g.nodes()),
                new LinkedHashSet<>(g.graphs()), g.links(), g.nodeAttrs(), g.linkAttrs(), g.graphAttrs());
    }

    public static MyGraph parseGraph(String filepath) {
        try {
            return new MyGraph(new Parser().read(new File(filepath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void outputGraph(MyGraph g, String filepath, Format format) throws IOException {
        Graphviz.fromGraph(g).render(format).toFile(new File(filepath));
    }

    public static void outputGraph(MyGraph g, String filepath) throws IOException {
        MyGraph.outputGraph(g, filepath, Format.PNG);
    }

    public void outputGraph(String filepath) throws IOException {
        MyGraph.outputGraph(this, filepath);
    }

    public void outputGraph(String filepath, Format format) throws IOException {
        MyGraph.outputGraph(this, filepath, format);
    }

    public HashMap<String, String> getNodeLabels() {
        HashMap<String, String> labels = new HashMap<>();
        for (MutableNode node : this.nodes()) {
            String name = node.name().toString();
            String label = node.attrs().get("label") != null ? node.attrs().get("label").toString()
                    : "";
            labels.put(name, label);
        }

        return labels;
    }

    public Set<String> getEdgeDirections() {
        Set<String> edgeDirections = new HashSet<>();
        String linkString = this.isDirected() ? " -> " : " -- ";

        for (Link edge : this.edges()) {
            edgeDirections.add(edge.from().name() + linkString + edge.to().name());
        }

        return edgeDirections;
    }

    public Set<String> getNodeNames() {
        Set<String> nodeNames = new HashSet<>();

        for (MutableNode node : this.nodes()) {
            nodeNames.add(node.name().toString());
        }

        return nodeNames;
    }

    public int getNumberOfNodes() {
        return this.nodes().size();
    }

    public int getNumberOfEdges() {
        return this.edges().size();
    }

    @Override
    public MyGraph copy() {
        return new MyGraph(strict, directed, cluster, name,
                new LinkedHashSet<>(nodes), new LinkedHashSet<>(subgraphs), links,
                nodeAttrs, linkAttrs, graphAttrs);
    }

    @Override
    public String toString() {
        return String.format(
                "Number of nodes: " + this.getNumberOfNodes()
                        + "\nNodes: " + this.getNodeNames()
                        + "\nNumber of edges: " + this.getNumberOfEdges()
                        + "\nEdges: " + this.getEdgeDirections())
                + "\nNode labels: " + this.getNodeLabels();
    }
}
