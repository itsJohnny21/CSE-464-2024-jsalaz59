package org.CSE464;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class Graph {
    HashMap<String, Node> nodes;

    //! Not tested
    public Graph() {
        this.nodes = new HashMap<>();
    }

    //! Not tested
    public Node addNode(String nodeID) {
        if (nodeExists(nodeID)) {
            System.err.println(String.format("Warning: Node with id '%s' already exists.", nodeID));
            return getNode(nodeID);
        }

        Node node = new Node(nodeID);
        nodes.put(nodeID, node);

        return node;
    }

    //! Not tested
    public void addNodeLabel(String nodeID, String label) {
        Node node = getNode(nodeID);
        node.attributes.put("label", label);
    }

    //! Not tested
    public Node[] addNodes(String... nodeIDs) {
        Node[] newNodes = new Node[nodeIDs.length];

        int i = 0;
        for (String nodeID : nodeIDs) {
            newNodes[i++] = addNode(nodeID);
        }

        return newNodes;
    }

    //! Not tested
    public boolean nodeExists(String nodeID) {
        return nodes.containsKey(nodeID);
    }

    //! Not tested
    public Node getNode(String nodeID) {
        if (!nodeExists(nodeID)) {
            throw new RuntimeException(String.format("Node '%s' does not exist.", nodeID));
        }

        Node node = nodes.get(nodeID);
        return node;
    }

    //! Not tested
    public Edge addEdge(String fromID, String toID) {
        if (!nodeExists(fromID)) {
            addNode(fromID);
        }

        if (!nodeExists(toID)) {
            addNode(toID);
        }

        Node srcNode = getNode(fromID);
        Node dstNode = getNode(toID);

        Edge edge = srcNode.addTo(dstNode);
        return edge;
    }

    //! Not tested
    public boolean edgeExists(String fromID, String toID) {
        if (!nodeExists(fromID)) {
            return false;
        }

        if (!nodeExists(toID)) {
            return false;
        }

        Node fromNode = getNode(fromID);
        return fromNode.to.containsKey(toID);
    }

    //! Not tested
    public Edge getEdge(String fromID, String toID) {
        if (!edgeExists(fromID, toID)) {
            throw new RuntimeException(String.format("Edge '%s -> %s' does not exist.", fromID, toID));
        }

        Node fromNode = getNode(fromID);
        System.out.println(fromNode);
        return fromNode.to.get(toID);
    }

    //! Not tested
    public void addEdgeLabel(String fromID, String toID, String label) {
        Edge edge = getEdge(fromID, toID);
        edge.attributes.put("label", label);
    }

    //! Not tested
    public int getNumberOfNodes() {
        return nodes.size();
    }

    //! Not tested
    public Set<String> getNodeNames() {
        return nodes.keySet();
    }

    //! Not tested
    public int getNumberOfEdges() {
        int numberOfEdges = 0;

        for (Node node : nodes.values()) {
            numberOfEdges += node.to.size();
        }

        return numberOfEdges;
    }

    //! Not tested
    public Set<String> getEdgeDirections() {
        HashSet<String> edgeDirections = new HashSet<>();

        for (Node node : nodes.values()) {

            for (Edge edge : node.to.values()) {
                edgeDirections.add(String.format("%s -> %s", node.ID, edge.toNode.ID));
            }
        }

        return edgeDirections;
    }

    //! Not tested
    public Set<String> getNodeLabels() {
        HashSet<String> nodeLabels = new HashSet<>();

        for (Node node : nodes.values()) {
            nodeLabels.add(String.format("%s: %s", node.ID, node.label));
        }

        return nodeLabels;
    }

    //! Not tested
    public void outputGraph(String filepath, Format format) throws IOException {
        switch (format) {
            case DOT -> {
                String dotContent = toDot();

                if (!filepath.endsWith(".dot")) {
                    filepath += ".dot";
                }
                try (FileWriter fileWriter = new FileWriter(filepath)) {
                    fileWriter.write(dotContent);
                }
            }
            default -> throw new AssertionError();
        }
    }

    // ! Not tested
    public String toDot() throws IOException {
        StringBuilder nodesSection = new StringBuilder();
        StringBuilder edgesSection = new StringBuilder();

        for (Node node : nodes.values()) {
            StringBuilder nodeAttrs = new StringBuilder();
            for (Entry<String, String> entry : node.attributes.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    nodeAttrs.append(String.format("%s=\"%s\" ", entry.getKey(), entry.getValue()));
                }
            }
            nodesSection.append(String.format("\t%s [%s];\n", node.ID, nodeAttrs.toString().trim()));

            if (!node.to.isEmpty()) {
                for (Edge edge : node.to.values()) {
                    StringBuilder edgeAttrs = new StringBuilder();
                    for (Entry<String, String> entry : edge.attributes.entrySet()) {
                        if (!entry.getValue().isEmpty()) {
                            edgeAttrs.append(String.format("%s=\"%s\" ", entry.getKey(), entry.getValue()));
                        }
                    }
                    edgesSection.append(
                            String.format("\t%s -> %s [%s];\n", node.ID, edge.toNode.ID, edgeAttrs.toString().trim()));
                }
            }
        }

        String dotContent = String.format("digraph {\n%s\n%s}", nodesSection.toString(),
                edgesSection.toString());

        return dotContent;
    }

    // ! Not tested
    @Override
    public String toString() {
        return String.format(
                "Number of nodes: " + getNumberOfNodes()
                        + "\nNodes: " + getNodeNames()
                        + "\nNumber of edges: " + getNumberOfEdges()
                        + "\nEdges: " + getEdgeDirections())
                + "\nNode labels: " + getNodeLabels();
    }

    protected class Node {
        protected String ID;
        protected String label;
        protected HashMap<String, String> attributes;
        protected HashMap<String, Edge> from;
        public HashMap<String, Edge> to;

        //! Not tested
        protected Node(String ID) {
            this.ID = ID;
            this.from = new HashMap<>();
            this.to = new HashMap<>();
            this.attributes = new HashMap<>();
        }

        //! Not tested
        protected Edge addTo(Node toNode) {
            Edge edge = new Edge(this, toNode);
            to.put(toNode.ID, edge);

            return edge;
        }

        //! Not tested
        protected Edge addFrom(Node fromNode) {
            Edge edge = new Edge(fromNode, this);
            from.put(fromNode.ID, edge);

            return edge;
        }

        //! Not tested
        protected void removeTo(Node toNode) {
            to.remove(toNode.ID);
        }

        //! Not tested
        protected void removeFrom(Node fromNode) {
            from.remove(fromNode.ID);
        }

        //! Not tested
        @Override
        public String toString() {
            return this.ID;
        }
    }

    public class Edge {
        protected Node fromNode;
        protected Node toNode;
        protected String label;
        protected HashMap<String, String> attributes;

        protected Edge(Node fromNode, Node toNode) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.attributes = new HashMap<>();
        }

        //! Not tested
        protected void remove() {
            fromNode.removeTo(toNode);
        }
    }

    public enum Format {
        BMP("-Tbmp"), DOT("-Tdot"), JEPG("-Tjpg"), JSON("-Tjson"), PDF("-Tpdf"), PICT("-Tpict"), PLAINTEXT("-Tplain"),
        PNG("-Tpng"), SVG("Tsvg");

        private final String value;

        Format(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static void main(String[] args) {
        try {

            Graph gm = new Graph();
            Node n1 = gm.addNode("n1");
            gm.addNodeLabel(n1.ID, "hello");

            Node[] ns = gm.addNodes("n3", "n4");

            for (Node n : ns) {
                gm.addNodeLabel(n.ID, "world!");
                System.out.println(n);
            }

            gm.addEdge("n1", "n2");
            gm.addEdge("n1", "n3");

            gm.addEdge("n2", "n1");

            gm.addEdge("n3", "n2");
            gm.addEdge("n3", "n4");

            gm.addEdge("n4", "n2");
            Edge e4_1 = gm.addEdge("n4", "n4");

            gm.addEdgeLabel(e4_1.fromNode.ID, e4_1.toNode.ID, "yessirr");

            System.out.println(gm);

            gm.outputGraph("./idkbruvvv", Format.DOT);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
