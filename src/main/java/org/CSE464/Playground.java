// packge org.CSE464;

// import java.io.IOException;

// public class Playground {
//     public static void main(String[] args) {
//         MyGraph g = MyGraph.parseGraph(
//                 "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/test/resources/DOT/valid/funnyGraph/funnyGraph.dot");
//         // System.err.println(g.isDirected());
//         // System.out.println(g);
//         // System.out.println(g.getEdgeDirections());
//         System.out.println(g);
//         try {
//             g.outputGraph(
//                     "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/test/reso~`{}. urces/tmp/tmp");

//         } catch (Exception e) {
//             System.out.println("EXCEPTION");
//         }
//         // Set<String> edges = g.edges().stream().map(e -> e.name().toString()).collect(Collectors.toSet());
//         // System.out.printf("edges: %s\n", edges);
//         // System.out.println(g.getNodeLabels());
//         // System.out.println(g.getEdgeDirections());

//         try {
//             MyGraph.outputGraph(g,
//                     "/Users/jonisalazar/School/Fall 2024/CSE464/CSE-464-2024-jsalaz59/src/test/resources/tmp/dr-10.png");
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//         System.out.println("Woohoo🥳😀");
//     }
// }
