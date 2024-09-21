package org.CSE464;

import org.jgrapht.Graph;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTImporter;
import org.jgrapht.nio.ImportException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

public class MyGraph extends SimpleDirectedGraph<String, DefaultEdge> {
    private HashMap<String, HashMap<String, String>> attributes = new HashMap<>();

    public MyGraph(Class edgeClass) {
        super(edgeClass);
    }

    private static MyGraph parseGraph(String filepath) {
        MyGraph graph = new MyGraph(DefaultEdge.class);

        DOTImporter<String, DefaultEdge> dotImporter = new DOTImporter<>();
        dotImporter.setVertexFactory(name -> name);
        dotImporter.addVertexAttributeConsumer((pair, attr) -> {
            String vertex = pair.getFirst();
            String attributeName = pair.getSecond();
            String attributeValue = attr.getValue();

            if (!graph.attributes.containsKey(vertex)) {
                graph.attributes.put(vertex, new HashMap<String, String>());
            }

            graph.attributes.get(vertex).put(attributeName, attributeValue);
        });

        try (Reader dotFileReader = new FileReader(filepath)) {
            dotImporter.importGraph(graph, dotFileReader);
        } catch (ImportException | IOException e) {
            e.printStackTrace();
        }

        return graph;
    }

    public static void main(String[] args) {
        MyGraph graph = MyGraph.parseGraph("/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/main/resources/graph1.dot");
        System.out.println(graph);
        System.out.println("Woohoo🥳😀");
    }

    public String edgeDirections() {
        if (this.edgeSet().size() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder("[");
        for (DefaultEdge edge : this.edgeSet()) {
            sb.append(this.getEdgeSource(edge)).append(" -> ").append(this.getEdgeTarget(edge)).append(", ");
        }

        sb.replace(sb.length() - 2, sb.length() , "");
        sb.append("]");
        return sb.toString();
    }

//    public String nodeLabels() {
//        StringBuilder sb = new StringBuilder()
//        for (String node : this.vertexSet()) {
//
//        }
//    }

    public String nodeLabel(String node) {
        return this.attributes.get(node).get("label");
    }

    @Override
    public String toString() {
        return
                "Number of nodes: " + this.vertexSet().size()
//                +"\nNode labels: " + nodeLabels()
                +"\nNodes: " + this.vertexSet()
                +"\nNumber of edges:" + this.vertexSet().size()
                +"\nEdges: " + this.edgeDirections()
                ;
    }
}
