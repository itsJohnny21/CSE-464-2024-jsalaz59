package org.CSE464;

import org.CSE464.Graph.Path;

public class Playground {

    public static void main(String[] args) throws Exception {
        Graph g = Graph.parseDOT(
                "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/test/resources/Search/mediumGraph.dot");

        System.out.println(g);
        Path p = g.graphSearch("j", "g");
        System.out.println(p);
    }
}
