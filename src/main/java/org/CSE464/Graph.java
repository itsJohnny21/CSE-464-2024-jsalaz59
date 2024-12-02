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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.LinkSource;
import guru.nidi.graphviz.model.LinkTarget;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import guru.nidi.graphviz.parse.Parser;
import guru.nidi.graphviz.parse.ParserException;
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

    //! Abstract class for a search strategy
    public static abstract class SearchStrategy {
        public abstract Path search(Graph g, String srcID, String dstID);
    }

    //! Class for finding a path in a graph
    public static class PathFinder {
        protected SearchStrategy strategy;

        public PathFinder(SearchStrategy strategy) {
            this.strategy = strategy;
        }
    }

    //! Class for BFS strategy
    public static class SearchBFS extends SearchStrategy {
        @Override
        public Path search(Graph g, String srcID, String dstID) {
            return g.graphSearch(srcID, dstID, Algorithm.BFS);
        }
    }

    //! Class for DFS strategy
    public static class SearchDFS extends SearchStrategy {
        @Override
        public Path search(Graph g, String srcID, String dstID) {
            return g.graphSearch(srcID, dstID, Algorithm.DFS);
        }
    }

    //! Class for abstracting out the different search algorithms
    public static class SearchProcessor {
        public Path search(Graph g, PathFinder finder, String srcID, String dstID) {
            return finder.strategy.search(g, srcID, dstID);
        }
    }

    //! graphSearch using the strategy design pattern
    public Path searchByStrategy(String srcID, String dstID, Algorithm algorithm) {
        PathFinder finder = algorithm.equals(Algorithm.BFS) ? new PathFinder(new SearchBFS())
                : new PathFinder(new SearchDFS());
        SearchProcessor processor = new SearchProcessor();
        return processor.search(this, finder, srcID, dstID);
    }

    public Collection<Node> getNodes() {
        return nodes.values();
    }

    public Collection<Edge> getEdges() {
        return edges.values();
    }

    private static void applyGraphAttributesFromDOT(Graph graph, MutableGraph mutableGraph) {
        //! Extract variable
        mutableGraph.graphAttrs().forEach(a -> {
            String attribute = a.getKey();
            String value = a.getValue().toString();
            graph.setAttribute(attribute, value);
        });
    }

    private static void applyNodeAttributesFromDOT(Node node, MutableNode mutableNode) {
        //! Extract variable
        mutableNode.attrs().forEach(a -> {
            String attribute = a.getKey();
            String value = a.getValue().toString();
            node.setAttribute(attribute, value);
        });
    }

    private static void applyEdgeAttributesFromDOT(Edge edge, Link link) {
        //! Rename from l to link
        //! Extract variable
        link.attrs().forEach(a -> {
            String attribute = a.getKey();
            String value = a.getValue().toString();
            edge.setAttribute(attribute, value);
        });
    }

    private static void addNodesFromDOT(Graph graph, MutableGraph mutableGraph) {
        for (MutableNode mutableNode : mutableGraph.nodes()) {
            String nodeID = mutableNode.name().toString();

            //! Extract method
            Node node = graph.addOrGetNode(nodeID);

            //! Extract variable
            //! Extract method
            applyNodeAttributesFromDOT(node, mutableNode);
        }
    }

    private static void addEdgesFromDOT(Graph graph, MutableGraph mutableGraph) {
        for (Link link : mutableGraph.edges()) {
            LinkTarget linkTarget = link.to();
            LinkSource linkSource = link.from();

            if (linkSource == null) {
                continue;
            }

            String toNodeID = linkTarget.name().toString();
            String fromNodeID = linkSource.name().toString();

            //! Reuse
            Node toNode = graph.addOrGetNode(toNodeID);
            Node fromNode = graph.addOrGetNode(fromNodeID);

            Edge edge = graph.addEdge(fromNode.ID, toNode.ID);

            //! Extract variable
            //! Extract method
            applyEdgeAttributesFromDOT(edge, link);
        }
    }

    public static Graph parseDOT(String filepath) {
        try {
            MutableGraph mutableGraph = new Parser().read(new File(filepath));
            String graphID = mutableGraph.name().toString();

            Graph graph = graphID.isEmpty() ? new Graph() : new Graph(graphID);

            //! Extract method
            applyGraphAttributesFromDOT(graph, mutableGraph);

            //! Extract variable
            addNodesFromDOT(graph, mutableGraph);

            //! Extract variable
            addEdgesFromDOT(graph, mutableGraph);

            return graph;
        } catch (ParserException | IOException e) {
            throw new ParseException(String.format("Error: Unable to parse graph: %s", e.getMessage()));
        }
    }

    private Node addOrGetNode(String nodeID) {
        return nodeExists(nodeID) ? getNode(nodeID) : addNode(nodeID);
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

        //! Extract method
        node.clearTo();

        //! Extract method
        node.clearFrom();

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

        //! Code reuse
        Node fromNode = addOrGetNode(fromID);

        //! Code reuse
        Node toNode = addOrGetNode(toID);

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

        //! Rename from nodes to nodesArray
        Node[] nodesArray = new Node[nodeIDs.length];
        nodesArray[0] = nodeExists(nodeIDs[0]) ? getNode(nodeIDs[0]) : addNode(nodeIDs[0]);

        //! Rename from edges to edgesArray
        Edge[] edgesArray = new Edge[nodeIDs.length - 1];

        for (int i = 0; i < nodeIDs.length - 1; i++) {
            String fromNodeID = nodeIDs[i];
            String toNodeID = nodeIDs[i + 1];

            Edge edge = edgeExists(fromNodeID, toNodeID) ? getEdge(fromNodeID, toNodeID)
                    : addEdge(fromNodeID, toNodeID);
            edgesArray[i] = edge;

            nodesArray[i + 1] = edge.toNode;
        }

        Path path = new Path(nodesArray, edgesArray);
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

        //! Rename from nodes to nodesArray
        Node[] nodesArray = new Node[nodeIDs.length];
        nodesArray[0] = currentNode;

        //! Rename from edges to edgesArray
        Edge[] edgesArray = new Edge[nodeIDs.length - 1];

        for (int i = 1; i < nodeIDs.length; i++) {
            String nextNodeID = nodeIDs[i];
            Node nextNode = currentNode.to.get(nextNodeID);

            if (nextNode == null) {
                return null;
            }

            Edge edge = currentNode.to(nextNode);
            currentNode = nextNode;

            edgesArray[i - 1] = edge;
            nodesArray[i] = currentNode;
        }

        Path path = new Path(nodesArray, edgesArray);
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
        //! Rename from get to poll
        public Node poll();

        public boolean add(Node node);

        public boolean isEmpty();

        public void clear();
    }

    public class Queue extends LinkedList<Node> implements Container {
        @Override
        public Node poll() {
            return pollFirst();
        }
    }

    public class Stack extends LinkedList<Node> implements Container {
        @Override
        public Node poll() {
            return pollLast();
        }
    }

    //! Abstract class
    public abstract class GraphSearcher {
        private HashSet<Node> visited;
        private HashMap<Node, Node> prev;
        protected Queue container;
        private Path path = null;

        private void createVisitedSet() {
            this.visited = new HashSet<>();
        }

        private void createPrevSet() {
            this.prev = new HashMap<>();
        }

        protected void createContainer() {
            this.container = new Queue();
        }

        private void clear() {
            visited.clear();
            prev.clear();
            container.clear();
        }

        //! Abstract method
        protected abstract Node pollContainer();

        //! Template method
        protected void search(Node srcNode, Node dstNode) {
            if (visited == null) {
                createVisitedSet();
            }
            if (prev == null) {
                createPrevSet();
            }
            if (container == null) {
                createContainer();
            }

            searchForPath(srcNode, dstNode);
            clear();
        }

        private void searchForPath(Node srcNode, Node dstNode) {
            path = null;
            container.add(srcNode);
            visited.add(srcNode);

            boolean isContainerEmpty = container.isEmpty();

            while (!isContainerEmpty) {
                Node fromNode = pollContainer(); //! abstract method called

                boolean foundDstNode = fromNode.equals(dstNode);
                if (foundDstNode) {
                    buildPath(dstNode);
                    return;
                }

                for (Node toNode : fromNode.to.values()) {
                    boolean alreadyVisitedToNode = visited.contains(toNode);

                    if (!alreadyVisitedToNode) {
                        container.add(toNode);
                        prev.put(toNode, fromNode);
                        visited.add(toNode);
                    }
                }
                isContainerEmpty = container.isEmpty();
            }

        }

        protected Path getResult() {
            return path;
        }

        private void buildPath(Node dstNode) {
            Node currentNode = dstNode;
            LinkedList<Node> nodesList = new LinkedList<>();
            nodesList.add(currentNode);
            LinkedList<Edge> edgesList = new LinkedList<>();

            while (prev.containsKey(currentNode)) {
                Node prevNode = prev.get(currentNode);
                Edge edge = prevNode.to(currentNode);
                edgesList.addFirst(edge);
                nodesList.addFirst(prevNode);

                currentNode = prevNode;
            }

            path = new Path(nodesList.toArray(Node[]::new), edgesList.toArray(Edge[]::new));
        }
    }

    //! Concrete class
    public class GraphSearcherBFS extends GraphSearcher {
        @Override
        protected Node pollContainer() {
            return container.pollFirst();
        }
    }

    //! Concrete class
    public class GraphSearcherDFS extends GraphSearcher {
        @Override
        protected Node pollContainer() {
            return container.pollLast();
        }
    }

    //! Template method for graphSearch
    private Path graphSearchHelperTemplate(Node srcNode, Node dstNode, Algorithm algorithm) {
        GraphSearcher graphSearcher = algorithm.equals(Algorithm.BFS) ? new GraphSearcherBFS() : new GraphSearcherDFS();
        graphSearcher.search(srcNode, dstNode);
        return graphSearcher.getResult();
    }

    private Path graphSearchHelper(Node srcNode, Node dstNode, Algorithm algorithm) {
        HashSet<Node> visited = new HashSet<>();
        Container container = algorithm.equals(Algorithm.BFS) ? new Queue() : new Stack();
        HashMap<Node, Node> prev = new HashMap<>();

        container.add(srcNode);
        visited.add(srcNode);

        while (!container.isEmpty()) {
            Node fromNode = container.poll();

            if (fromNode.equals(dstNode)) {
                return buildPath(dstNode, prev);
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

    //! Method signature changed (srcNode unneeded)
    private Path buildPath(Node dstNode, HashMap<Node, Node> prev) {
        Node currentNode = dstNode;
        //! Rename from nodes to nodesList
        LinkedList<Node> nodesList = new LinkedList<>();
        nodesList.add(currentNode);
        //! Rename from edges to edgesList
        LinkedList<Edge> edgesList = new LinkedList<>();

        while (prev.containsKey(currentNode)) {
            Node prevNode = prev.get(currentNode);
            Edge edge = prevNode.to(currentNode);
            edgesList.addFirst(edge);
            nodesList.addFirst(prevNode);

            currentNode = prevNode;
        }

        //! Improve readability
        Path path = new Path(nodesList.toArray(Node[]::new), edgesList.toArray(Edge[]::new));
        return path;
    }

    public Path graphSearchRandom(String srcID, String dstID, Algorithm algorithm) {
        Node srcNode = getNode(srcID);
        Node dstNode = getNode(dstID);

        return graphSearchHelperRandom(srcNode, dstNode, algorithm);
    }

    private Path graphSearchHelperRandom(Node srcNode, Node dstNode, Algorithm algorithm) {
        HashSet<Node> visited = new HashSet<>();
        LinkedHashSet<Node> visitedExample = new LinkedHashSet<>();
        Container container = algorithm.equals(Algorithm.BFS) ? new Queue() : new Stack();
        HashMap<Node, Node> prev = new HashMap<>();

        container.add(srcNode);
        visited.add(srcNode);

        while (!container.isEmpty()) {
            Node fromNode = container.poll();
            visitedExample.add(fromNode);
            System.out.println(visitedExample);

            if (fromNode.equals(dstNode)) {
                return buildPath(dstNode, prev);
            }

            ArrayList<Node> neighbours = new ArrayList<>(fromNode.to.values());
            Collections.shuffle(neighbours);

            for (Node toNode : neighbours) {
                if (!visited.contains(toNode)) {
                    container.add(toNode);
                    prev.put(toNode, fromNode);
                    visited.add(toNode);
                }
            }
        }

        return null;
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

    public void removeNodes(String... nodeIDs) {
        for (String nodeID : nodeIDs) {
            if (!nodeID.matches(ID_REGEX)) {
                throw new InvalidIDException(
                        String.format("Error: Attempted to remove node with id '%s'. The id is invalid.", nodeID));
            }

            if (!nodeExists(nodeID)) {
                throw new NodeDoesNotExistException(
                        String.format("Error: Attempted to remove node with id '%s'. Node does not exist.", nodeID));
            }
        }

        for (String nodeID : nodeIDs) {
            removeNode(nodeID);
        }
    }

    public String outputGraph(String filepath, Format format) throws IOException, InterruptedException {
        return outputGraph(filepath, format, new String[] {});
    }

    /**
     * Parsing made possible using Nidi3's Graphviz-Java.
     * 
     * <p> For more information, see the Graphviz-Java documentation:</p> <a href="https://github.com/nidi3/graphviz-java">Graphviz-Java By Nidi3</a>
    **/

    private void writeToFile(String content, String filepath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write(content);
        }
    }

    private void executeDotCommand(String dotContent, String filepath, Format format, String... options)
            throws InterruptedException, IOException {
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

    public String outputGraph(String filepath, Format format, String... options)
            throws IOException, InterruptedException {
        String dotContent = toDot();

        if (!filepath.endsWith(format.extension)) {
            filepath += format.extension;
        }

        //! Extract variable
        boolean isRawDot = format.equals(Format.RAWDOT);

        if (isRawDot) {
            //! Extract method
            writeToFile(dotContent, filepath);
        } else {
            //! Extract method
            executeDotCommand(dotContent, filepath, format, options);
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

        private void clearTo() {
            for (Iterator<Entry<String, Node>> it = to.entrySet().iterator(); it.hasNext();) {
                Entry<String, Node> entry = it.next();
                Node toNode = entry.getValue();
                toNode.from.remove(ID);

                edges.remove(this.to(toNode).ID);
                it.remove();
            }
        }

        private void clearFrom() {
            for (Iterator<Entry<String, Node>> it = from.entrySet().iterator(); it.hasNext();) {
                Entry<String, Node> entry = it.next();
                Node fromNode = entry.getValue();
                fromNode.to.remove(ID);

                edges.remove(fromNode.to(this).ID);
                it.remove();
            }
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
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Node node = (Node) obj;
            return ID.equals(node.ID);
        }

        @Override
        public int hashCode() {
            return ID.hashCode();
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