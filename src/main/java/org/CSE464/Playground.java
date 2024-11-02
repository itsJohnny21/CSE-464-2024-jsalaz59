package org.CSE464;

import org.CSE464.Graph.Algorithm;
import org.CSE464.Graph.Edge;
import org.CSE464.Graph.Node;
import org.CSE464.Graph.Path;

public class Playground {

    public static void main(String[] args) throws Exception {
        Graph g = Graph.parseDOT("/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/mediumGraph.dot");

        for (Node n : g.getNodes()) {
            n.clearAttributes();
        }

        for (Edge e : g.getEdges()) {
            e.clearAttributes();
        }

        Path p1 = g.addPath("j", "d", "h");
        p1.setAttributes(Edge.Attribute.COLOR, "green");
        Path p2 = g.addPath("j", "d", "h");
        System.out.println(p2);
        p2.setAttributes(Node.Attribute.COLOR, "blue");

        for (Node n : g.getNodes()) {
            n.removeAttribute(Node.Attribute.LABEL);
        }

        System.out.println(g);
        Path p = g.graphSearch("c", "g", Algorithm.BFS);

        p.setAttributes(Edge.Attribute.COLOR, "pink");
        p.setAttributes(Node.Attribute.COLOR, "red");

        if (p != null) {
            System.out.println(p);
        }
        g.outputGraph("/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/mediumGraph.dot",
                Format.RAWDOT);
    }
}
