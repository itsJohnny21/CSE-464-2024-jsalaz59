package org.CSE464;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import guru.nidi.graphviz.model.LinkTarget;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import lombok.Data;

@Data
public class Graph extends DOTElement {
    private static final String DIRECTED_SIGN = "->";
    private static final String UNDIRECTED_SIGN = "--";
    private final LinkedHashMap<String, Node> nodes;
    private final LinkedHashMap<String, Edge> edges;

    public Graph() {
        super();
        this.nodes = new LinkedHashMap<>();
        this.edges = new LinkedHashMap<>();
    }

    public Graph(String ID) {
        super(ID);
        if (!ID.matches(ID_REGEX)) {
            throw new InvalidIDException(String.format(
                    "Error: Attempt to create a '%s' using ID '%s' failed. The ID does not satisfy the ID regex.",
                    getClass(), ID));
        }

        this.nodes = new LinkedHashMap<>();
        this.edges = new LinkedHashMap<>();
    }

    public Collection<Node> getNodes() {
        return nodes.values();
    }

    public Collection<Edge> getEdges() {
        return edges.values();
    }

    public static Graph parseDOT(String filepath) {
        try {
            MutableGraph mutableGraph = new Parser().read(new File(filepath));
            String graphID = mutableGraph.name().toString();

            Graph graph = graphID.isEmpty() ? new Graph() : new Graph(graphID);
            mutableGraph.graphAttrs().forEach(a -> {
                graph.setAttribute(a.getKey(), a.getValue().toString());
            });

            for (MutableNode node : mutableGraph.nodes()) {
                String nodeID = node.name().toString();
                Node nodeCopy;

                if (!graph.nodeExists(nodeID)) {
                    nodeCopy = graph.addNode(nodeID);
                } else {
                    nodeCopy = graph.getNode(nodeID);
                }

                node.attrs().forEach(a -> {
                    nodeCopy.setAttribute(a.getKey(), a.getValue().toString());
                });
                node.links().forEach(l -> {
                    LinkTarget toNode = l.to();
                    String toNodeID = toNode.name().toString();
                    Node toNodeCopy;

                    if (!graph.nodeExists(toNodeID)) {
                        toNodeCopy = graph.addNode(toNodeID);
                    } else {
                        toNodeCopy = graph.getNode(toNodeID);
                    }

                    Edge edge = graph.addEdge(nodeCopy.ID, toNodeCopy.ID);

                    l.attrs().forEach(a -> {
                        edge.setAttribute(a.getKey(), a.getValue().toString());
                    });
                });
            }

            return graph;
        } catch (guru.nidi.graphviz.parse.ParserException | IOException e) {
            throw new ParseException(String.format("Error: Unable to parse graph: %s", e.getMessage()));
        }
    }

    public Node addNode(String nodeID) {
        if (!nodeID.matches(ID_REGEX)) {
            throw new InvalidIDException(String
                    .format("Error: Attempt to add node with id '%s' failed. The id is not allowed.", nodeID, nodeID));
        }

        if (nodeExists(nodeID)) {
            throw new NodeAlreadyExistsException(
                    String.format("Error: Attempt to add node '%s' failed. The node already exists.", nodeID));
        }

        Node node = new Node(this, nodeID);
        nodes.put(nodeID, node);

        return node;
    }

    public Node[] addNodes(String... nodeIDs) {
        Set<String> uniqueNodeIDs = new HashSet<>();

        for (String nodeID : nodeIDs) {
            if (!nodeID.matches(ID_REGEX)) {
                throw new InvalidIDException(
                        String.format("Error: Attempt to add multiple nodes failed. Node with id '%s' is not allowed.",
                                nodeID, nodeID));
            }

            if (nodeExists(nodeID)) {
                throw new NodeAlreadyExistsException(String
                        .format("Error: Attempt to add multiple nodes failed. Node '%s' already exists.", nodeID));
            }

            if (uniqueNodeIDs.contains(nodeID)) {
                throw new DuplicateNodeIDException(String.format(
                        "Error: Attempt to add multiple nodes failed. Duplicate node id '%s' was provided.", nodeID));
            }

            uniqueNodeIDs.add(nodeID);
        }

        Node[] newNodes = new Node[nodeIDs.length];

        int i = 0;
        for (String nodeID : nodeIDs) {
            newNodes[i++] = addNode(nodeID);
        }

        return newNodes;
    }

    public void removeNode(String nodeID) {
        if (!nodeExists(nodeID)) {
            throw new NodeDoesNotExistException(
                    String.format("Error: Attempt to remove node '%s' failed. Node does not exist.", nodeID));
        }

        Node node = getNode(nodeID);

        for (Iterator<Entry<String, Node>> it = node.to.entrySet().iterator(); it.hasNext();) {
            Entry<String, Node> entry = it.next();
            Node toNode = entry.getValue();
            toNode.from.remove(node.ID);

            String edgeID = Graph.createEdgeID(node.ID, toNode.ID);
            edges.remove(edgeID);
            it.remove();
        }
        for (Iterator<Entry<String, Node>> it = node.from.entrySet().iterator(); it.hasNext();) {
            Entry<String, Node> entry = it.next();
            Node fromNode = entry.getValue();
            fromNode.to.remove(node.ID);

            String edgeID = Graph.createEdgeID(fromNode.ID, node.ID);
            edges.remove(edgeID);
            it.remove();
        }

        nodes.remove(node.ID);
        node.setGraph(null);
    }

    public boolean nodeExists(String nodeID) {
        return nodes.containsKey(nodeID);
    }

    public Node getNode(String nodeID) {
        if (!nodeExists(nodeID)) {
            throw new NodeDoesNotExistException(String.format("Error: Node with id '%s' does not exist.", nodeID));
        }

        Node node = nodes.get(nodeID);
        return node;
    }

    public Edge addEdge(String fromID, String toID) {
        if (edgeExists(fromID, toID)) {
            throw new EdgeAlreadyExistsException(
                    String.format("Error: Edge with id '%s' already exists.", Graph.createEdgeID(fromID, toID)));
        }

        Node fromNode = nodeExists(fromID) ? getNode(fromID) : addNode(fromID);
        Node toNode = nodeExists(toID) ? getNode(toID) : addNode(toID);

        Edge edge = new Edge(fromNode, toNode);
        fromNode.to.put(toNode.ID, toNode);
        toNode.from.put(fromNode.ID, fromNode);
        edges.put(edge.ID, edge);
        return edge;
    }

    public boolean edgeExists(String fromID, String toID) {
        String edgeID = Graph.createEdgeID(fromID, toID);
        return edges.containsKey(edgeID);
    }

    public void removeEdge(String fromID, String toID) {
        if (!edgeExists(fromID, toID)) {
            throw new EdgeDoesNotExistException(
                    String.format("Error: Edge '%s' does not exist.", Graph.createEdgeID(fromID, toID)));
        }

        Edge edge = getEdge(fromID, toID);
        Node fromNode = edge.fromNode;
        Node toNode = edge.toNode;

        fromNode.to.remove(toNode.ID);
        toNode.from.remove(fromNode.ID);
        edges.remove(edge.ID);
    }

    private static String createEdgeID(String fromID, String toID) {
        return String.format("%s %s %s", fromID, DIRECTED_SIGN, toID);
    }

    private static String createPathID(String... nodeIDs) {
        if (nodeIDs.length == 0) {
            return "";
        }

        return String.join(String.format(" %s ", DIRECTED_SIGN), nodeIDs);

        // Edge[] edges = new Edge[nodeIDs.length - 1];
        // StringBuilder sb = new StringBuilder();
        // String rootID = edges[0].fromNode.ID;
        // sb.append(rootID);

        // for (Edge edge : edges) {
        // String toID = edge.toNode.ID;
        // sb.append(String.format(" %s %s", DIRECTED_SIGN, toID));
        // }

        // return sb.toString();
    }

    public Edge getEdge(String fromID, String toID) {
        if (!edgeExists(fromID, toID)) {
            throw new EdgeDoesNotExistException(
                    String.format("Edge '%s' does not exist.", Graph.createEdgeID(fromID, toID)));
        }

        String edgeID = Graph.createEdgeID(fromID, toID);
        return edges.get(edgeID);
    }

    public Path addPath(String... nodeIDs) {
        if (nodeIDs.length == 0) {
            return null;
        }

        Node[] nodes = new Node[nodeIDs.length];
        nodes[0] = nodeExists(nodeIDs[0]) ? getNode(nodeIDs[0]) : addNode(nodeIDs[0]);

        Edge[] edges = new Edge[nodeIDs.length - 1];

        for (int i = 0; i < nodeIDs.length - 1; i++) {
            String fromNodeID = nodeIDs[i];
            String toNodeID = nodeIDs[i + 1];

            Edge edge = edgeExists(fromNodeID, toNodeID) ? getEdge(fromNodeID, toNodeID)
                    : addEdge(fromNodeID, toNodeID);
            edges[i] = edge;

            nodes[i + 1] = edge.toNode;
        }

        Path path = new Path(nodes, edges);
        return path;
    }

    public boolean pathExists(String... nodeIDs) {
        return getPath(nodeIDs) != null;
    }

    public Path getPath(String... nodeIDs) {
        if (nodeIDs.length == 0) {
            return null;
        }

        if (!nodeExists(nodeIDs[0])) {
            return null;
        }

        Node currentNode = getNode(nodeIDs[0]);

        Node[] nodes = new Node[nodeIDs.length];
        nodes[0] = currentNode;
        Edge[] edges = new Edge[nodeIDs.length - 1];

        for (int i = 1; i < nodeIDs.length; i++) {
            String nextNodeID = nodeIDs[i];
            Node nextNode = currentNode.to.get(nextNodeID);

            if (nextNode == null) {
                return null;
            }

            Edge edge = currentNode.to(nextNode);
            currentNode = nextNode;

            edges[i - 1] = edge;
            nodes[i] = currentNode;
        }

        Path path = new Path(nodes, edges);
        return path;
    }

    public void removePath(String... nodeIDs) {
        Path path = getPath(nodeIDs);

        if (path == null) {
            throw new PathDoesNotExistException(
                    String.format("Error: The path '%s' does not exist.", Graph.createPathID(nodeIDs)));
        }

        for (Edge edge : path.edges) {
            edge.removeFromGraph();
        }
    }

    public Path graphSearch(String srcID, String dstID, Algorithm algorithm) {
        Node srcNode = getNode(srcID);
        Node dstNode = getNode(dstID);

        return graphSearchHelper(srcNode, dstNode, algorithm);
    }

    public interface Container {
        public Node get();

        public boolean add(Node node);

        public boolean isEmpty();
    }

    public class Queue extends LinkedList<Node> implements Container {
        @Override
        public Node get() {
            return pollFirst();
        }
    }

    public class Stack extends LinkedList<Node> implements Container {
        @Override
        public Node get() {
            return pollLast();
        }
    }

    private Path graphSearchHelper(Node srcNode, Node dstNode, Algorithm algorithm) {
        HashSet<Node> visited = new HashSet<>();
        Container container = algorithm == Algorithm.BFS ? new Queue() : new Stack();
        HashMap<Node, Node> prev = new HashMap<>();

        container.add(srcNode);
        visited.add(srcNode);

        while (!container.isEmpty()) {
            Node fromNode = container.get();

            if (fromNode.equals(dstNode)) {
                return buildPath(srcNode, dstNode, prev);
            }

            for (Node toNode : fromNode.to.values()) {
                if (!visited.contains(toNode)) {
                    container.add(toNode);
                    prev.put(toNode, fromNode);
                    visited.add(toNode);
                }
            }
        }

        return null;
    }

    private Path buildPath(Node srcNode, Node dstNode, HashMap<Node, Node> prev) {
        Node currentNode = dstNode;
        LinkedList<Node> nodes = new LinkedList<>();
        nodes.add(currentNode);

        LinkedList<Edge> edges = new LinkedList<>();

        while (prev.containsKey(currentNode)) {
            Node prevNode = prev.get(currentNode);
            Edge edge = prevNode.to(currentNode);
            edges.addFirst(edge);
            nodes.addFirst(prevNode);

            currentNode = prevNode;
        }

        Path path = new Path(nodes.toArray(new Node[0]), edges.toArray(new Edge[0]));
        return path;
    }

    public int getNumberOfNodes() {
        return nodes.size();
    }

    public Set<String> getNodeNames() {
        return nodes.keySet();
    }

    public int getNumberOfEdges() {
        return edges.size();
    }

    public Set<String> getEdgeDirections() {
        return edges.keySet();
    }

    public Set<String> getNodeLabels() {
        HashSet<String> nodeLabels = new HashSet<>();

        for (Node node : nodes.values()) {
            nodeLabels.add(String.format("%s=%s", node.ID, node.attributes.get("label")));
        }

        return nodeLabels;
    }

    public String outputGraph(String filepath, Format format) throws IOException, InterruptedException {
        return outputGraph(filepath, format, new String[] {});
    }

    /**
     * Parsing made possible using Nidi3's Graphviz-Java.
     * 
     * <p> For more information, see the Graphviz-Java documentation:</p> <a href="https://github.com/nidi3/graphviz-java">Graphviz-Java By Nidi3</a>
    **/
    public String outputGraph(String filepath, Format format, String... options)
            throws IOException, InterruptedException {
        String dotContent = toDot();

        if (!filepath.endsWith(format.extension)) {
            filepath += format.extension;
        }

        if (format.equals(Format.RAWDOT)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
                writer.write(dotContent);
            }
        } else {

            List<String> command = new ArrayList<>();
            command.add("dot");
            command.add(format.value);
            command.add("-o");
            command.add(filepath);
            command.addAll(Arrays.asList(options));

            ProcessBuilder processBuilder = new ProcessBuilder(command);

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            try (OutputStream outputStream = process.getOutputStream()) {
                outputStream.write(dotContent.getBytes());
                outputStream.flush();
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                InputStream inputStream = process.getInputStream();
                String errorMessage = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                throw new ParseException(errorMessage);
            }
        }

        return dotContent;
    }

    public String getAttribute(Attribute attribute) {
        return getAttribute(attribute.value);
    }

    public void setAttribute(Attribute attribute, String value) {
        setAttribute(attribute.value, value);
    }

    public void removeAttribute(Attribute attribute) {
        removeAttribute(attribute.value);
    }

    @Override
    public String toDot() {
        String attrsString = attributes.isEmpty() ? ""
                : "\t" + attributes.entrySet().stream().map(e -> String.format("%s=\"%s\";", e.getKey(), e.getValue()))
                        .collect(Collectors.joining("\n\t"));

        String nodesSection = nodes.isEmpty() ? ""
                : (attributes.isEmpty() ? "" : "\n\n") + nodes.values().stream()
                        .map(n -> String.format("\t%s", n.toDot())).collect(Collectors.joining("\n"));

        String edgesSection = edges.isEmpty() ? ""
                : "\n\n" + edges.values().stream().map(e -> String.format("\t%s", e.toDot()))
                        .collect(Collectors.joining("\n"));

        String dotContent = String.format("digraph%s{\n%s%s%s\n}",
                (ID == null || ID.isEmpty()) ? " " : String.format(" %s ", ID), attrsString, nodesSection,
                edgesSection);

        return dotContent;
    }

    public String describe() {
        return String
                .format("Graph: " + ID + "\nNumber of nodes: " + getNumberOfNodes() + "\nNodes: " + getNodeNames()
                        + "\nNumber of edges: " + getNumberOfEdges() + "\nEdges: " + getEdgeDirections())
                + "\nNode labels: " + getNodeLabels();
    }

    @Override
    public String toString() {
        return String.format("%s, Nodes=%s, Edges=%s", ID, nodes.values(), edges.values());
    }

    public enum Algorithm {
        DFS, BFS
    }

    /**
     * Enum representing the attributes of a graph in Graphviz.
     *
     * <p> For more information, see the Graphviz documentation:</p> <a href=https://graphviz.org/docs/graph/">Graphviz Graph Attributes</a>
     */
    public enum Attribute {
        _BACKGROUND("_background"), BB("bb"), BEAUTIFY("beautify"), BGCOLOR("bgcolor"), CENTER("center"),
        CHARSET("charset"), CLASS("class"), CLUSTERRANK("clusterrank"), COLORSCHEME("colorscheme"), COMMENT("comment"),
        COMPOUND("compound"), CONCENTRATE("concentrate"), DAMPING("Damping"), DEFAULTDIST("defaultdist"), DIM("dim"),
        DIMEN("dimen"), DIREDGECONSTRAINTS("diredgeconstraints"), DPI("dpi"), EPSILON("epsilon"), ESEP("esep"),
        FONTCOLOR("fontcolor"), FONTNAME("fontname"), FONTNAMES("fontnames"), FONTPATH("fontpath"),
        FONTSIZE("fontsize"), FORCELABELS("forcelabels"), GRADIENTANGLE("gradientangle"), HREF("href"), ID("id"),
        IMAGEPATH("imagepath"), INPUTSCALE("inputscale"), K("K"), LABEL("label"), LABEL_SCHEME("label_scheme"),
        LABELJUST("labeljust"), LABELLOC("labelloc"), LANDSCAPE("landscape"), LAYERLISTSEP("layerlistsep"),
        LAYERS("layers"), LAYERSELECT("layerselect"), LAYERSEP("layersep"), LAYOUT("layout"), LEVELS("levels"),
        LEVELSGAP("levelsgap"), LHEIGHT("lheight"), LINELENGTH("linelength"), LP("lp"), LWIDTH("lwidth"),
        MARGIN("margin"), MAXITER("maxiter"), MCLIMIT("mclimit"), MINDIST("mindist"), MODE("mode"), MODEL("model"),
        NEWRANK("newrank"), NODESEP("nodesep"), NOJUSTIFY("nojustify"), NORMALIZE("normalize"),
        NOTRANSLATE("notranslate"), NSLIMIT("nslimit"), NSLIMIT1("nslimit1"), ONEBLOCK("oneblock"),
        ORDERING("ordering"), ORIENTATION("orientation"), OUTPUTORDER("outputorder"), OVERLAP("overlap"),
        OVERLAP_SCALING("overlap_scaling"), OVERLAP_SHRINK("overlap_shrink"), PACK("pack"), PACKMODE("packmode"),
        PAD("pad"), PAGE("page"), PAGEDIR("pagedir"), QUADTREE("quadtree"), QUANTUM("quantum"), RANKDIR("rankdir"),
        RANKSEP("ranksep"), RATIO("ratio"), REMINCROSS("remincross"), REPULSIVEFORCE("repulsiveforce"),
        RESOLUTION("resolution"), ROOT("root"), ROTATE("rotate"), ROTATION("rotation"), SCALE("scale"),
        SEARCHSIZE("searchsize"), SEP("sep"), SHOWBOXES("showboxes"), SIZE("size"), SMOOTHING("smoothing"),
        SORTV("sortv"), SPLINES("splines"), START("start"), STYLE("style"), STYLESHEET("stylesheet"), TARGET("target"),
        TBBALANCE("TBbalance"), TOOLTIP("tooltip"), TRUECOLOR("truecolor"), URL("URL"), VIEWPORT("viewport"),
        VORO_MARGIN("voro_margin"), XDOTVERSION("xdotversion");

        protected final String value;

        Attribute(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Data
    public final class Path {
        private final String ID;
        private final Node[] nodes;
        private final Edge[] edges;

        private Path(Node[] nodes, Edge[] edges) {
            this.ID = Graph.createPathID(Arrays.stream(nodes).map(node -> node.ID).toArray(String[]::new));
            this.nodes = nodes;
            this.edges = edges;
        }

        public void setAttributes(Edge.Attribute attribute, String value) {
            for (Edge edge : edges) {
                edge.setAttribute(attribute, value);
            }
        }

        public void setAttributes(Node.Attribute attribute, String value) {
            for (Node node : nodes) {
                node.setAttribute(attribute, value);
            }
        }

        public void removeAttributes(Edge.Attribute attribute) {
            for (Edge edge : edges) {
                edge.removeAttribute(attribute);
            }
        }

        public void removeAttributes(Node.Attribute attribute) {
            for (Node node : nodes) {
                node.removeAttribute(attribute);
            }
        }

        @Override
        public String toString() {
            return ID;
        }
    }

    @Data
    public final class Node extends DOTElement {
        protected Graph graph;
        protected final HashMap<String, Node> from;
        protected final HashMap<String, Node> to;

        private Node(Graph graph, String ID) {
            super(ID);
            this.graph = graph;
            this.from = new HashMap<>();
            this.to = new HashMap<>();
        }

        private void setGraph(Graph graph) {
            this.graph = graph;
        }

        public Edge connectTo(Node toNode) {
            if (this.graph == null) {
                throw new NullPointerException(String.format(
                        "Error: Attempt to connect node '%s' to node '%s' failed. Node '%s' does not belong to a graph.",
                        this.ID, toNode.ID, this.ID));
            } else if (toNode.graph == null) {
                throw new NullPointerException(String.format(
                        "Error: Attempt to connect node '%s' to node '%s' failed. Node '%s' does not belong to a graph.",
                        this.ID, toNode.ID, toNode.ID));
            }

            if (!this.graph.equals(toNode.graph)) {
                throw new DifferingGraphsException(String.format(
                        "Error: Attempt to connect node '%s' from graph '%s' to node '%s' from graph '%s' failed. The nodes belong to different graphs.",
                        this.ID, this.graph.ID, toNode.ID, toNode.graph.ID));
            }

            return graph.addEdge(this.ID, toNode.ID);
        }

        public Edge to(Node toNode) {
            return graph.getEdge(this.ID, toNode.ID);
        }

        public Edge connectFrom(Node fromNode) {
            return fromNode.connectTo(this);
        }

        public Edge from(Node fromNode) {
            return fromNode.to(this);
        }

        public void disconnectTo(Node toNode) {
            graph.removeEdge(this.ID, toNode.ID);
        }

        public void disconnectFrom(Node fromNode) {
            fromNode.disconnectTo(this);
        }

        public void removeFromGraph() {
            graph.removeNode(ID);
        }

        public String getAttribute(Attribute attribute) {
            return getAttribute(attribute.value);
        }

        public void setAttribute(Attribute attribute, String value) {
            setAttribute(attribute.value, value);
        }

        public void removeAttribute(Attribute attribute) {
            removeAttribute(attribute.value);
        }

        @Override
        public String toString() {
            return ID;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        /**
         * Enum representing the attributes of a node in Graphviz.
         *
         * <p> For more information, see the Graphviz documentation:</p> <a href="https://graphviz.org/docs/nodes/">Graphviz Node Attributes</a>
         */
        public enum Attribute {
            AREA("area"), CLASS("class"), COLOR("color"), COLORSCHEME("colorscheme"), COMMENT("comment"),
            DISTORTION("distortion"), FILLCOLOR("fillcolor"), FIXEDSIZE("fixedsize"), FONTCOLOR("fontcolor"),
            FONTNAME("fontname"), FONTSIZE("fontsize"), GRADIENTANGLE("gradientangle"), GROUP("group"),
            HEIGHT("height"), HREF("href"), ID("id"), IMAGE("image"), IMAGEPOS("imagepos"), IMAGESCALE("imagescale"),
            LABEL("label"), LABELLOC("labelloc"), LAYER("layer"), MARGIN("margin"), NOJUSTIFY("nojustify"),
            ORDERING("ordering"), ORIENTATION("orientation"), PENWIDTH("penwidth"), PERIPHERIES("peripheries"),
            PIN("pin"), POS("pos"), RECTS("rects"), REGULAR("regular"), ROOT("root"), SAMPLEPOINTS("samplepoints"),
            SHAPE("shape"), SHAPEFILE("shapefile"), SHOWBOXES("showboxes"), SIDES("sides"), SKEW("skew"),
            SORTV("sortv"), STYLE("style"), TARGET("target"), TOOLTIP("tooltip"), URL("URL"), VERTICES("vertices"),
            WIDTH("width"), XLABEL("xlabel"), XLP("xlp"), Z("z");

            protected final String value;

            Attribute(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }
    }

    @Data
    public final class Edge extends DOTElement {
        protected final Node fromNode;
        protected final Node toNode;

        private Edge(Node fromNode, Node toNode) {
            super(Graph.createEdgeID(fromNode.ID, toNode.ID));
            this.fromNode = fromNode;
            this.toNode = toNode;
        }

        public void removeFromGraph() {
            fromNode.graph.removeEdge(fromNode.ID, toNode.ID);
        }

        public String getAttribute(Attribute attribute) {
            return getAttribute(attribute.value);
        }

        public void setAttribute(Attribute attribute, String value) {
            setAttribute(attribute.value, value);
        }

        public void removeAttribute(Attribute attribute) {
            removeAttribute(attribute.value);
        }

        @Override
        public String toString() {
            return ID;
        }

        /**
         * Enum representing the attributes of an edge in Graphviz.
         *
         * <p> For more information, see the Graphviz documentation:</p> <a href="https://graphviz.org/docs/edges/">Graphviz Edge Attributes</a>
         */
        public enum Attribute {
            ARROWHEAD("arrowhead"), ARROWSIZE("arrowsize"), ARROWTAIL("arrowtail"), CLASS("class"), COLOR("color"),
            COLORSCHEME("colorscheme"), COMMENT("comment"), CONSTRAINT("constraint"), DECORATE("decorate"), DIR("dir"),
            EDGEHREF("edgehref"), EDGETARGET("edgetarget"), EDGETOOLTIP("edgetooltip"), EDGEURL("edgeURL"),
            FILLCOLOR("fillcolor"), FONTCOLOR("fontcolor"), FONTNAME("fontname"), FONTSIZE("fontsize"),
            HEAD_LP("head_lp"), HEADCLIP("headclip"), HEADHREF("headhref"), HEADLABEL("headlabel"),
            HEADPORT("headport"), HEADTARGET("headtarget"), HEADTOOLTIP("headtooltip"), HEADURL("headURL"),
            HREF("href"), ID("id"), LABEL("label"), LABELANGLE("labelangle"), LABELDISTANCE("labeldistance"),
            LABELFLOAT("labelfloat"), LABELFONTCOLOR("labelfontcolor"), LABELFONTNAME("labelfontname"),
            LABELFONTSIZE("labelfontsize"), LABELHREF("labelhref"), LABELTARGET("labeltarget"),
            LABELTOOLTIP("labeltooltip"), LABELURL("labelURL"), LAYER("layer"), LEN("len"), LHEAD("lhead"), LP("lp"),
            LTAIL("ltail"), MINLEN("minlen"), NOJUSTIFY("nojustify"), PENWIDTH("penwidth"), POS("pos"),
            SAMEHEAD("samehead"), SAMETAIL("sametail"), SHOWBOXES("showboxes"), STYLE("style"), TAIL_LP("tail_lp"),
            TAILCLIP("tailclip"), TAILHREF("tailhref"), TAILLABEL("taillabel"), TAILPORT("tailport"),
            TAILTARGET("tailtarget"), TAILTOOLTIP("tailtooltip"), TAILURL("tailURL"), TARGET("target"),
            TOOLTIP("tooltip"), URL("URL"), WEIGHT("weight"), XLABEL("xlabel"), XLP("xlp");

            protected final String value;

            Attribute(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }
    }
}