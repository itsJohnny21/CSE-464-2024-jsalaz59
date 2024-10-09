package org.CSE464;

import org.CSE464.Graph.Edge;
import org.CSE464.Graph.Node;

public class Playground {
    public static void main(String[] args) {
        try {

            Graph g = Graph.parseGraph(
                    "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/main/resources/idkbruvvv.dot");

            // Node node = g.addNode("t1");
            // Node n1 = g.getNode("n1");
            System.out.println(g);
            Node n2 = g.getNode("n2");
            Node n4 = g.getNode("n4");

            n4.setAttribute("shape", "Mcircle");
            n4.setAttribute(Node.Attribute.AREA, "100");
            n4.setAttribute("style", "filled");
            n4.setAttribute("fillcolor", "green");
            n4.setAttribute("label", "breh");

            n4.to(n4).setAttribute("arrowsize", "2");
            n4.to.forEach((k, v) -> {
                n4.to(v).setAttribute(Edge.Attribute.COLOR, "pink");
            });
            g.setAttribute("Graph.Attribute.BGCOLOR", "red");
            // n2.connectTo(n4);
            // n4.to(n2).setAttribute("arrowsize", "10"); //! change the names to make them more intuitive such as node1.to(node2);
            g.outputGraph(
                    "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/main/resources/idkbruvvv2.dot",
                    Format.DOT);
            System.out.println(g);
            // g.removeNode("n1");

            // System.out.println(n1.graph);
            // g.addEdge("fromID", "toID");
            // System.out.println(g);

            // g.removeNode("n1");
            // System.out.println(g);

            // HashMap<String, Graph> edges = new HashMap<>();
            // HashSet<Graph> toEdges = new HashSet<>();

            // Graph g = new Graph();

            // edges.put("1", g);
            // toEdges.add(g);

            // System.out.println(edges.size());
            // System.out.println(toEdges.size());

            // destroy(g);

            // System.out.println(edges.size());
            // System.out.println(toEdges.size());

            // String curDir = System.getProperty("user.dir");
            // System.out.printf("curDir: %s\n", curDir);

            // boolean exists = Files.exists(Path.of("./src/main/resources/idkbruvvv.dot"));
            // System.out.printf("exists: %s\n", exists);

            // Graph gm = new Graph();
            // Node n1 = gm.addNode("n1");
            // gm.addNodeLabel(n1.getID(), "hello");

            // Node[] ns = gm.addNodes("n3", "n4");

            // for (Node n : ns) {
            //     gm.addNodeLabel(n.getID(), "world!");
            //     System.out.println(n);
            // }

            // gm.addEdge("n1", "n2");
            // gm.addEdge("n1", "n3");

            // gm.addEdge("n2", "n1");

            // gm.addEdge("n3", "n2");
            // gm.addEdge("n3", "n4");

            // gm.addEdge("n4", "n2");
            // Edge e4_1 = gm.addEdge("n4", "n4");

            // gm.addEdgeLabel(e4_1.getFromNode().getID(), e4_1.getFromNode().getID(), "yessirr2");

            // System.out.println(gm);

            // gm.outputGraph("./idkbruvvv", Format.DOT);

        } catch (Exception e) {
            System.out.println("Exception");
            System.err.println(e.getMessage());
        }
    }
}
