package examples;

import org.CSE464.Format;
import org.CSE464.Graph;
import org.CSE464.Graph.Edge;
import org.CSE464.Graph.Node;

public class Examples {

    public static void nodeAddition() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1"); // Add a node with ID "n1"

        String dotContent = g.outputGraph("./assets/graphs/nodeAddition", Format.SVG);
    }

    public static void multNodeAddition() throws Exception {
        Graph g = new Graph("Master");
        g.addNode("n1");
        String[] nodeIDs = new String[] { "n2", "n3", "n4" };
        // String[] nodeIDs = new String[] { "n1", "n3", "n4" }; // NodeAlreadyExistsException thrown
        // String[] nodeIDs = new String[] { "n2", "3_badID", "n4" }; // InvalidIDException thrown
        // String[] nodeIDs = new String[] { "n2", "n2", "n4" }; // DuplicateNodeIDException thrown

        g.addNodes(nodeIDs); // Add nodes "n2", "n3", and "n4"

        String dotContent = g.outputGraph("./assets/graphs/multNodeAddition", Format.SVG);
    }

    public static void nodeRetrieval() throws Exception {
        Graph g = new Graph("Master");
        g.addNode("n1");

        Node n1 = g.getNode("n1"); // Get the node with ID "n1"
    }

    public static void checkNodeExists() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");

        System.out.println(g.nodeExists("n1")); // true
        System.out.println(g.nodeExists("n2")); // false
    }

    public static void edgeAddition() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");
        Edge e1 = g.addEdge("n1", "n2"); // Add a directed edge between "n1" and "n2"
        // Edge e1 = n1.connectTo(n2); // An alternative method to create the same exact edge using connectTo
        // Edge e1 = n2.connectFrom(n1); // An alternative method to create the same exact edge using connectFrom

        g.outputGraph("./assets/graphs/edgeAddition", Format.SVG);
    }

    public static void nodeRemoval() throws Exception {
        Graph g = new Graph("Master");
        int n = 5;

        // Create the complete digraph of size 5
        for (int i = 0; i < n; i++) {
            g.addNode(String.valueOf(i));
        }

        for (int i = 0; i < n; i++) {
            Node srcNode = g.getNode(String.valueOf(i));
            srcNode.getID();
            for (int j = 0; j < n; j++) {
                Node dstNode = g.getNode(String.valueOf(j));
                if (i != j) {
                    srcNode.connectTo(dstNode);
                }
            }
        }

        g.outputGraph("./assets/graphs/complete5_before_node_removal", Format.SVG);
        g.removeNode("4");
        g.outputGraph("./assets/graphs/complete5_after_node_removal", Format.SVG);

    }

    public static void edgeRemoval() throws Exception {
        Graph g = new Graph("Master");
        int n = 3;

        for (int i = 0; i < n; i++) {
            g.addNode(String.valueOf(i));
        }

        for (int i = 0; i < n; i++) {
            Node srcNode = g.getNode(String.valueOf(i));
            srcNode.getID();
            for (int j = 0; j < n; j++) {
                Node dstNode = g.getNode(String.valueOf(j));
                if (i != j) {
                    srcNode.connectTo(dstNode);
                }
            }
        }

        g.outputGraph("./assets/graphs/complete3_before_edge_removal", Format.SVG);
        g.removeEdge("2", "0");
        g.outputGraph("./assets/graphs/complete3_after_edge_removal", Format.SVG);
    }

    public static void checkEdgeExists() throws Exception {
        Graph g = new Graph("Master");
        Edge e1 = g.addEdge("n1", "n2");

        System.out.println(g.edgeExists("n1", "n2")); // true
        System.out.println(g.edgeExists("n1", "n1")); // false
    }

    public static void getNumberOfNodes() throws Exception {
        Graph g = new Graph("Master");
        g.addNodes("n1", "n2", "n3");
        g.addEdge("n4", "n5");

        System.out.println(g.getNumberOfNodes()); // 5
        g.outputGraph("./assets/graphs/getNumberOfNodes", Format.SVG);
    }

    public static void getNumberOfEdges() throws Exception {
        Graph g = new Graph("Master");
        Node[] nodes = g.addNodes("n1", "n2", "n3");
        Node root = g.addNode("root");
        for (Node node : nodes) {
            root.connectTo(node);
        }

        System.out.println(g.getNumberOfEdges()); // 3
        g.outputGraph("./assets/graphs/getNumberOfEdges", Format.SVG);
    }

    public static void getNodeNames() throws Exception {
        Graph g = new Graph("Master");
        g.addNodes("n1", "n2", "n3");

        System.out.println(g.getNodeNames()); // [n1, n2, n3]
        g.outputGraph("./assets/graphs/getNodeNames", Format.SVG);
    }

    public static void getNodeLabels() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        n1.setAttribute(Node.Attribute.LABEL, "n1 label");
        n2.setAttribute(Node.Attribute.LABEL, "n2 label");

        System.out.println(g.getNodeLabels()); // [n1=n1 label, n2=n2 label]
        g.outputGraph("./assets/graphs/getNodeLabels", Format.SVG);
    }

    public static void getEdgeDirections() throws Exception {
        Graph g = new Graph("Master");
        Node[] nodes = g.addNodes("n1", "n2", "n3");
        Node root = g.addNode("root");
        for (Node node : nodes) {
            root.connectTo(node);
        }

        g.getNode("n2").connectTo(g.addNode("n4"));

        System.out.println(g.getEdgeDirections()); // [root -> n1, root -> n2, root -> n3, n2 -> n4]
        g.outputGraph("./assets/graphs/getEdgeDirections", Format.SVG);
    }

    public static void connectTo() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        n1.connectTo(n2); // Edge created: "n1 -> n2"

        String methodName = new Exception().getStackTrace()[0].getMethodName();
        String dotContent = g.outputGraph(String.format("./assets/graphs/%s", methodName), Format.SVG);
        System.out.println(dotContent);
    }

    public static void connectFrom() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        n1.connectFrom(n2); // Edge created: "n2 -> n1"

        String methodName = new Exception().getStackTrace()[0].getMethodName();
        String dotContent = g.outputGraph(String.format("./assets/graphs/%s", methodName), Format.SVG);
        System.out.println(dotContent);
    }

    public static void toDot() throws Exception {
        Graph g = new Graph("Master");
        g.setAttribute(Graph.Attribute.BGCOLOR, "pink");
        int n = 3;
        String[] colors = new String[] { "red", "yellow", "orange", "teal", "grey", "blue", "green" };

        for (int i = 0; i < n; i++) {
            Node node = g.addNode(String.valueOf(i));
            node.setAttribute(Node.Attribute.LABEL, String.format("I am node %s", i));
            node.setAttribute(Node.Attribute.COLOR, colors[i % colors.length]);
        }

        for (int i = 0; i < n; i++) {
            Node srcNode = g.getNode(String.valueOf(i));
            srcNode.getID();
            for (int j = 0; j < n; j++) {
                Node dstNode = g.getNode(String.valueOf(j));
                if (i != j) {
                    Edge edge = srcNode.connectTo(dstNode);
                    edge.setAttribute(Edge.Attribute.COLOR, colors[i % colors.length]);
                    edge.setAttribute(Edge.Attribute.LABEL, edge.getID());
                    edge.setAttribute(Edge.Attribute.FILLCOLOR, colors[(i + 3) % colors.length]);
                }
            }
        }

        System.out.println(g.toDot());
    }

    public static void outputGraph() throws Exception {
        Graph g = new Graph("Master");
        g.setAttribute(Graph.Attribute.BGCOLOR, "pink");
        int n = 3;
        String[] colors = new String[] { "red", "yellow", "orange", "teal", "grey", "blue", "green" };

        for (int i = 0; i < n; i++) {
            Node node = g.addNode(String.valueOf(i));
            node.setAttribute(Node.Attribute.LABEL, String.format("I am node %s", i));
            node.setAttribute(Node.Attribute.COLOR, colors[i % colors.length]);
        }

        for (int i = 0; i < n; i++) {
            Node srcNode = g.getNode(String.valueOf(i));
            srcNode.getID();
            for (int j = 0; j < n; j++) {
                Node dstNode = g.getNode(String.valueOf(j));
                if (i != j) {
                    Edge edge = srcNode.connectTo(dstNode);
                    edge.setAttribute(Edge.Attribute.COLOR, colors[i % colors.length]);
                    edge.setAttribute(Edge.Attribute.LABEL, edge.getID());
                    edge.setAttribute(Edge.Attribute.FILLCOLOR, colors[(i + 3) % colors.length]);
                }
            }
        }

        g.outputGraph("./assets/graphs/outputGraphToJSON", Format.JSON); // New filepath is ./assets/graphs/outputGraphToJSON.json
    }

    public static void to() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        n1.connectTo(n2);
        Edge edge = n1.to(n2); // Retrieve the edge "n1 -> n2"
    }

    public static void from() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");

        n1.connectTo(n2);
        Edge edge = n2.from(n1); // Retrieve the edge "n1 -> n2"
    }

    public static void disconnectTo() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");
        n1.connectTo(n2);

        n1.disconnectTo(n2); // Removes the edge "n1 -> n2"
    }

    public static void disconnectFrom() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");
        Node n2 = g.addNode("n2");
        n1.connectTo(n2);

        n2.disconnectFrom(n1); // Removes the edge "n1 -> n2"
    }

    public static void removeFromGraphNode() throws Exception {
        Graph g = new Graph("Master");
        Node n1 = g.addNode("n1");

        n1.removeFromGraph(); // Removes the node with "n1"
    }

    public static void removeFromGraphEdge() throws Exception {
        Graph g = new Graph("Master");
        Edge e1 = g.addEdge("n1", "n2");

        e1.removeFromGraph(); // Removes the edge "n1 -> n2"
    }

    public static void setAttribute() {
        Graph g = new Graph();
        g.setAttribute("custom_attribute", "some value");
        g.setAttribute(Graph.Attribute.BGCOLOR, "red"); // Setting the "bgcolor" attribute to "red"
    }

    public static void getAttribute() {
        Graph g = new Graph();
        g.setAttribute("custom_attribute", "some value");
        g.setAttribute(Graph.Attribute.BGCOLOR, "red");

        g.getAttribute(Graph.Attribute.BGCOLOR); // Retrieve attribute value is "red"
    }

    public static void removeAttribute() {
        Graph g = new Graph();
        g.setAttribute("custom_attribute", "some value");
        g.setAttribute(Graph.Attribute.BGCOLOR, "red");

        g.removeAttribute(Graph.Attribute.BGCOLOR); // Attribute "bgcolor" removed
    }

    public static void main(String[] args) throws Exception {
        // getNumberOfNodes();
        // getNumberOfEdges();
        // getNodeNames();
        // getNodeLabels();
        // getEdgeDirections();
        // outputGraph();
        // connectTo();
        // connectFrom();
        // toDot();
        // to();
        // from();
        // disconnectTo();
        // disconnectFrom();
        removeFromGraphNode();
        removeFromGraphEdge();

    }
}
