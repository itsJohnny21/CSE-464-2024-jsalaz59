package org.CSE464;

import org.CSE464.Graph.Edge;
import org.CSE464.Graph.Node;

public class Playground {

    public static void main(String[] args) throws Exception {
        String s1 = "Graph";
        String s2 = "Master";
        String intersectionCharacter = "a";
        double radius = 0.5;
        double angle = Math.PI / 4;

        Graph g = intersectStrings(s1, s2, intersectionCharacter, radius, angle);
        g.setAttribute(Graph.Attribute.BGCOLOR.value, "gray84:gray94");

        for (Edge e : g.getEdges().values()) {
            e.setAttribute(Edge.Attribute.ARROWTAIL.value, "100");
        }

        for (Node n : g.getNodes().values()) {
            String[] posVals = n.getAttribute(Node.Attribute.POS.value).split(",");
            double xPos = Double.parseDouble(posVals[0]) + 10;
            double yPos = Double.parseDouble(posVals[1]) + 10;

            n.setAttribute(Node.Attribute.POS.value, String.format("%s,%s", xPos, yPos));
            n.setAttribute(Node.Attribute.FONTNAME.value, "San Francisco");

        }

        g.outputGraph("./assets/icons/logo2", Format.SVG);
    }

    public static void setNodeAttributes(Node n, String label, double xPos, double yPos, double radius) {
        n.setAttribute(Node.Attribute.LABEL.value, label);
        n.setAttribute(Node.Attribute.POS.value, String.format("%s,%s", xPos, yPos));
        n.setAttribute(Node.Attribute.WIDTH.value, String.format("%s", radius));
        n.setAttribute(Node.Attribute.HEIGHT.value, String.format("%s", radius));
    }

    public static Graph intersectStrings(String s1, String s2, String intersectionCharacter, double radius,
            double angle) throws Exception {
        Graph g = new Graph();
        int intersectionIndex1 = s1.indexOf(intersectionCharacter);
        int intersectionIndex2 = s2.indexOf(intersectionCharacter);

        if (intersectionIndex1 == -1 || intersectionIndex2 == -2) {
            throw new RuntimeException("Strings do not share the intersection character");
        }

        double prevXPos = 0;
        double prevYPos = 0;

        Node prevNode = g.addNode("0");
        setNodeAttributes(prevNode, String.valueOf(s1.charAt(0)), prevXPos, prevYPos, radius);

        for (int i = 1; i < s1.length(); i++) {
            Node nextNode = g.addNode(String.format("%d", g.getNumberOfNodes()));

            prevXPos += Math.cos(angle) * 4 * radius;
            prevYPos += Math.sin(angle) * 4 * radius;

            setNodeAttributes(nextNode, String.valueOf(s1.charAt(i)), prevXPos, prevYPos, radius);
            prevNode.connectTo(nextNode);
            prevNode = nextNode;
        }

        Node pivotNode = g.getNode(String.format("%d", intersectionIndex1));
        pivotNode.setAttribute(Node.Attribute.COLOR.value, "red");

        int intersectionIndex = s2.indexOf(intersectionCharacter);
        String[] pivotNodePosString = pivotNode.getAttribute(Node.Attribute.POS.value).split(",");
        double pivotNodeXPos = Double.parseDouble(pivotNodePosString[0]);
        double pivotNodeYPos = Double.parseDouble(pivotNodePosString[1]);

        prevXPos = pivotNodeXPos + Math.cos(angle + Math.PI / 2) * 4 * radius * intersectionIndex;
        prevYPos = pivotNodeYPos + Math.sin(angle + Math.PI / 2) * 4 * radius * intersectionIndex;
        prevNode = intersectionIndex == 0 ? pivotNode : g.addNode(String.format("%d", s1.length()));

        if (!prevNode.equals(pivotNode)) {
            setNodeAttributes(prevNode, String.valueOf(s2.charAt(0)), prevXPos, prevYPos, radius);
        }

        for (int i = 1; i < s2.length(); i++) {
            Node nextNode = intersectionIndex == i ? pivotNode
                    : g.addNode(String.format("%d", g.getNumberOfNodes() + 1));

            prevXPos += -Math.cos(angle + Math.PI / 2) * 4 * radius;
            prevYPos += -Math.sin(angle + Math.PI / 2) * 4 * radius;

            setNodeAttributes(nextNode, String.valueOf(s2.charAt(i)), prevXPos, prevYPos, radius);
            prevNode.connectTo(nextNode);
            prevNode = nextNode;
        }

        return g;
    }
}
