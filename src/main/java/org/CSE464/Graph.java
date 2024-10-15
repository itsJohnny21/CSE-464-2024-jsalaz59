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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import guru.nidi.graphviz.model.LinkTarget;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
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

        Node fromNode;
        if (!nodeExists(fromID)) {
            fromNode = addNode(fromID);
        } else {
            fromNode = getNode(fromID);
        }

        Node toNode;
        if (!nodeExists(toID)) {
            toNode = addNode(toID);
        } else {
            toNode = getNode(toID);
        }

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

    public Edge getEdge(String fromID, String toID) {
        if (!edgeExists(fromID, toID)) {
            throw new EdgeDoesNotExistException(
                    String.format("Edge '%s' does not exist.", Graph.createEdgeID(fromID, toID)));
        }

        String edgeID = Graph.createEdgeID(fromID, toID);
        return edges.get(edgeID);
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
            command.addAll(Arrays.asList(options));
            command.add(format.value);
            command.add("-o");
            command.add(filepath);

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
    @EqualsAndHashCode(callSuper = false)
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
            return super.toString();
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
    @EqualsAndHashCode(callSuper = false)
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
            return super.toString();
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
