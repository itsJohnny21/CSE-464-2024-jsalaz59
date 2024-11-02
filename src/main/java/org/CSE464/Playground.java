package org.CSE464;

import org.CSE464.Graph.Edge;
import org.CSE464.Graph.Path;

public class Playground {

    public static void main(String[] args) throws Exception {
        Graph g = Graph.parseDOT(
                "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/test/resources/Search/mediumGraph.dot");
        Path p = g.graphSearch("j", "g");
        p.setAttributes(Edge.Attribute.COLOR, "purple");
        System.out.println(p);
        g.outputGraph(
                "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/test/resources/Search/mediumGraph.dot",
                Format.RAWDOT);
    }
}
