package org.CSE464;

public class Playground {
    public static void main(String[] args) throws Exception {
        Graph g = new Graph();
        String projectName = "GraphMaster";

        for (char c : projectName.toCharArray()) {
            String nodeID = String.valueOf(c);
            if (!g.nodeExists(nodeID)) {
                g.addNode(nodeID);

            }
        }

        g.outputGraph("./filepath", Format.RAWDOT);

    }
}
