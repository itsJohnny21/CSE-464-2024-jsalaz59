package examples;

import org.CSE464.Format;
import org.CSE464.Graph;
import org.CSE464.Graph.Node;

public class Logo {

    public static void main(String[] args) throws Exception {
        String s1 = "Graph"; // Will be used to create a string of nodes that display 'Graph'
        String s2 = "Master"; // Will be used to create a string of nodes that display 'Master'
        String intersectionCharacter = "a"; // Where the string of nodes should intersect
        double radius = 0.5; // Size of a node
        double angle = Math.PI / 4; // The angle of the string of nodes

        Graph g = intersectStrings(s1, s2, intersectionCharacter, radius, angle); // Returns a graph where the string of nodes sharing an intersection character are display as two strings of nodes, one for each string provided
        g.setAttribute(Graph.Attribute.BGCOLOR, "gray84:gray94"); // Color of the background

        for (Node n : g.getNodes().values()) {
            n.setAttribute(Node.Attribute.FONTNAME, "San Francisco"); // Change the font name of the node
        }

        g.outputGraph("./assets/icons/logo", Format.SVG, "-Kfdp", "-n"); // Output the logo as SVG using -Kfdp engine to render
        g.outputGraph("./assets/graphs/logo", Format.RAWDOT); // Output the logo as RAWDOT
    }

    public static void setNodeAttributes(Node n, String label, double xPos, double yPos, double radius) {
        n.setAttribute(Node.Attribute.LABEL, label); // Set the 'label' attribute
        n.setAttribute(Node.Attribute.POS, String.format("%s,%s", xPos, yPos)); // Set the 'pos' attribute
        n.setAttribute(Node.Attribute.WIDTH, String.format("%s", radius)); // Set the 'width' attribute
        n.setAttribute(Node.Attribute.HEIGHT, String.format("%s", radius)); // Set the 'height' attribute
    }

    public static Graph intersectStrings(String s1, String s2, String intersectionCharacter, double radius,
            double angle) throws Exception {
        Graph g = new Graph("MasterGraph");
        int intersectionIndex1 = s1.indexOf(intersectionCharacter); // Index of intersetion character for string1
        int intersectionIndex2 = s2.indexOf(intersectionCharacter); // Index of intersetion character for string2

        if (intersectionIndex1 == -1 || intersectionIndex2 == -2) { // Both strings must contain the intersection character
            throw new RuntimeException("Strings do not share the intersection character");
        }

        double prevXPos = 0; // Initial position for x
        double prevYPos = 0; // Initial position for y

        Node prevNode = g.addNode("0"); // Create the first node with id '0'
        setNodeAttributes(prevNode, String.valueOf(s1.charAt(0)), prevXPos, prevYPos, radius); // Set the attributes of the first node

        for (int i = 1; i < s1.length(); i++) {
            Node nextNode = g.addNode(String.format("%d", g.getNumberOfNodes())); // Next node that will be connected as prevNode -> nextNode

            prevXPos += Math.cos(angle) * 4 * radius; // Update the x position
            prevYPos += Math.sin(angle) * 4 * radius; // Update the y position

            setNodeAttributes(nextNode, String.valueOf(s1.charAt(i)), prevXPos, prevYPos, radius); // Set the attributes of nextNode
            prevNode.connectTo(nextNode); // Create the edge
            prevNode = nextNode; // Update the prevNode
        }

        Node pivotNode = g.getNode(String.format("%d", intersectionIndex1)); // Pivot node where the intersection is made
        pivotNode.setAttribute(Node.Attribute.COLOR, "red"); // Change the color of the pivot node to red

        String[] pivotNodePosString = pivotNode.getAttribute(Node.Attribute.POS).split(","); // Get the x and y positions of the pivot node
        double pivotNodeXPos = Double.parseDouble(pivotNodePosString[0]); // Store the x position of the pivot node
        double pivotNodeYPos = Double.parseDouble(pivotNodePosString[1]); // Store the y position of the pivot node

        prevXPos = pivotNodeXPos + Math.cos(angle + Math.PI / 2) * 4 * radius * intersectionIndex2; // Update the x position so that it is at it's appropriate location. It will be length of string 2 minus intersection index 2 positions away in the x direction
        prevYPos = pivotNodeYPos + Math.sin(angle + Math.PI / 2) * 4 * radius * intersectionIndex2; // Update the y position so that it is at it's appropriate location. It will be length of string 2 minus intersection index 2 positions away in the y direction
        prevNode = intersectionIndex2 == 0 ? pivotNode : g.addNode(String.format("%d", s1.length())); // Check if the first character of string 2 is the intersection character and make it the pivot node if so

        setNodeAttributes(prevNode, String.valueOf(s2.charAt(0)), prevXPos, prevYPos, radius); // Set attributes of prevNode

        for (int i = 1; i < s2.length(); i++) {
            Node nextNode = intersectionIndex2 == i ? pivotNode
                    : g.addNode(String.format("%d", g.getNumberOfNodes() + 1)); // Check if the next node is the pivot node

            prevXPos += -Math.cos(angle + Math.PI / 2) * 4 * radius; // Update the x position
            prevYPos += -Math.sin(angle + Math.PI / 2) * 4 * radius; // Update the y position

            setNodeAttributes(nextNode, String.valueOf(s2.charAt(i)), prevXPos, prevYPos, radius); // Set attributes of nextNode
            prevNode.connectTo(nextNode); // Create the edge prevNode -> nextNode
            prevNode = nextNode; // Update the prevNode
        }

        return g; // Return graph containing the intersected string of nodes
    }
}
