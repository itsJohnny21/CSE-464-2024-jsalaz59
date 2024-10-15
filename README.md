# GraphMaster - A Graph Manager Powered by Graphviz

Created by me, [Johnny Salazar](https://github.com/itsJohnny21/itsJohnny21), for my CSE 467 (Software QA and Testing) course, taught by professor [Xusheng Xiao](https://xusheng-xiao.github.io/).

## Dependencies

This project is powered by Maven. To install the necessary dependencies, simply run the `mvn package` command. This will install the following:

- lombok (for setters and getters)
- sl4j-nop (for logging)
- graphviz-java (for parsing DOT files)
- junit-jupiter-engine (for JUnit testing)

After installing these dependencies, you can now begin creating your first graph!

## Graph

A `Graph` consists of a set of nodes and edges. The graph automatically manages its nodes and edges when performing node and edge operations such as adding a node, removing a node, adding an edge, etc. Its subclasses, [Node](#node) and [Edge](#edge), can have instances created only by the `Graph`. This form of composition ensures that the nodes and edges can only live within a graph. Each method of [Node](#node) and [Edge](#edge) (besides the attribute methods) directly calls a method from the `Graph`, ensuring that the `Graph` is always the one managing node and edge operations. For example, the `connectTo` method of a `Node` simply calls its graph's `addEdge` method to create this edge.

`Graph` also inherits from the `DOTElement` class, allowing it to have `attributes`. A supporting nested enum called `Attribute` makes it easy to perform attribute operatoins on a graph. The list of attributes was acquired from the official [Graphviz Attributes page](https://graphviz.org/docs/graph/).

See [examples](#examples) of creating a `Graph`.

### Create from scratch

To build a graph from scratch, you can choose from two constructors.

1. Constructor with no parameters. This will automatically give the the graph an ID equal to its Java hash code.

   ```java
   Graph g1 = new Graph();
   ```

2. Constructor with ID (String) parameter. You can manually provide your own ID for the graph.

   ```java
   Graph g2 = new Graph("Master");
   ```

The ID value (which must follow the [ID regex](#id-regex)) is final, meaning it cannot be modified after the `Graph` is created.

When [converting](#outputting-a-graph) the `Graph` into a DOT file, the graph's ID will be used as the name of the graph. For example, [converting](#outputting-a-graph) `g2` into a DOT file will look like:

```dot
digraph Master {

}
```

Notice the graph is a digraph (directed graph). This tool will only create digraphs. Creating a regular graph is not an option. This is because a Graphviz digraph can perform all the functionalities a regular graph can perform, plus some more.

**_Note: As of currently, there is no support for creating undirected edges, but support for this feature will be available some time in the near future._**

### Create from a DOT file

Another way to create a graph is by providing the path of a valid DOT file to the static `parseDOT` method provided by `Graph`. This static method utilizies the [Nidi3 graphviz-java](https://github.com/nidi3/graphviz-java) libarary to parse a DOT file by creating a MutableGraph object from graphviz-java, and then uses that MutableGraph object to add the nodes and edges (including all attributes) to the `Graph`. After parsing is complete, the graph should contain all the nodes, edges, and attributes from the DOT file.

The following is an example of parsing a valid [DOT file](./src/test/resources/DOT/valid/nodesX_Y_ZLabeled/nodesX_Y_ZLabeled.dot):

```Java
String dotPath = "./src/test/resources/DOT/valid/nodesX_Y_ZLabeled/nodesX_Y_ZLabeled.dot";
Graph g = Graph.parseDOT(dotPath);
```

### Add a node

The `Graph` object uses a nested class called `Node` to represent nodes in the graph. There are no public constructors for this `Node` class, ensuring that it can only be created by a `Graph`. To create a `Node`, you can call the `addNode `method from a `Graph`, and pass in the node's ID as an argument (which must follow the [ID regex](#id-regex)).

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1"); // Add a node with ID "n1"
```

**_Note: If a node with ID "n1" already existed, this method would have thrown a `NodeAlreadyExists` exception._**

This ID will be stored onto a HashMap, and will be used by the graph to quickly access its nodes. The ID will also be used to represent a node when converting the `Graph` into a DOT file. If we were to convert `g2` into a DOT file, it would look like:

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
    n1 [];
}
</pre>
    </div>
    <div
        style="
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: 20px;
        "
        >
    <p>Visualized</p>
    <img src="./assets/graphs/nodeAddition.svg" />
    </div>
</div>

Notice the empty brackets next to the node. These empty brackets contain the node's attributes, which in this case, the node has no attributes. To learn how to set and modify attributes for a node, see [Attributes](#attributes).

### Check node existance

A `Graph` allows you to check if a node exists. To do this, pass the node's ID into the `nodeExists` method provided by the `Graph`. The method returns true if the node exists, otherwise false.

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");

System.out.println(g.nodeExists("n1")); // true
System.out.println(g.nodeExists("n2")); // false
```

This method is useful to check if a node exists before removing a node.

### Add multiple nodes

For pure convenience, a `Graph` allows you to add multiple nodes. This can be achieved using the `addNodes` method which takes in an array of node IDs.

```java
Graph g = new Graph("Master");
g.addNode("n1");
String[] nodeIDs = new String[] { "n2", "n3", "n4" };
// String[] nodeIDs = new String[] { "n1", "n3", "n4" }; // NodeAlreadyExistsException thrown
// String[] nodeIDs = new String[] { "n2", "3_badID", "n4" }; // InvalidIDException thrown
// String[] nodeIDs = new String[] { "n2", "n2", "n4" }; // DuplicateNodeIDException thrown

g.addNodes(nodeIDs); // Add nodes "n2", "n3", and "n4"
```

Converting the graph `g` into a DOT file will result in the following:

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
        n1 [];
        n2 [];
        n3 [];
        n4 [];
}
</prev>
    </div>
    <div
        style="
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: 20px;
        "
        >
    <p>Visualized</p>
    <img src="./assets/graphs/multNodeAddition.svg" />
    </div>
</div>

**_Note: The IDs have to be unique, otherwise a a `DuplicateNodeIDException` will be thrown._**
**_Note: If a node with one of the provided IDs already exists when executing this method, a `NodeAlreadyExistsException` will be thrown._**
**_Note: If an invalid node ID is provided when executing this method, a `InvalidNodeIDException` will be thrown._**
**_Note: If any of the exceptions is thrown during execution of the method, no nodes will be added._**

### Get a node

To retrieve a node from a `Graph`, you can simply call the `getNode` method and pass in the associated node's ID.

```java
Graph g = new Graph("Master");
g.addNode("n1");

Node n1 = g.getNode("n1"); // Get the node with ID "n1"
```

This method is useful for getting a node that you don't have a direct reference to.

### Remove a node

A `Graph` can remove a `Node` by calling the `removeNode` method and passing in the node's [ID](#id-regex). This will also automatically remove any `Edge` that had this `Node` as either its source `Node` or destination `Node`. An example of a removing a `Node` from a complete digraph of size five is provided:

```java
Graph g = new Graph("Master");
int n = 5;

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

g.removeNode("4");
// Node n4 = g.getNode("4");
// n4.removeFromGraph(); // An alternative method for removing a node
```

A total of five nodes were created with IDs corresponding to their number in the creation process. After the complete digraph of size five was complete, the node with ID "4" node was removed.

<div style="display: flex; justify-content: center;">
    <div style="display: flex; flex-direction: column; align-items: center;">
        <p>Before</p>
        <img src="./assets/graphs/complete5_before_node_removal.svg" alt="Before" style="width: 60%;"/>
    </div>
    <div style="display: flex; flex-direction: column; align-items: center;">
        <p>After</p>
        <img src="./assets/graphs/complete5_after_node_removal.svg" alt="After" style="width: 60%;"/>
    </div>
</div>

Note that another way to remove a node is by calling the `Node`'s `removeFromGraph` method. This method simply calls its graph's `removeNode` method and passes in its own [ID](#id-regex).

### Add an edge

The `Graph` object has another nested class called `Edge` that represents an edge between two nodes in the graph. Similarly to a `Node `, there are no public constructors. The `Graph`'s `addEdge` method can be used to create an edge between two nodes. Simply provide the two node IDs to do so.

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");
Edge e1 = g.addEdge("n1", "n2"); // Add a directed edge between "n1" and "n2"
// Edge e1 = n1.connectTo(n2); // An alternative method to create the same exact edge using connectTo
// Edge e1 = n2.connectFrom(n1); // An alternative method to create the same exact edge using connectFrom
```

**_Note: If an edge already existed between nodes "n1" and "n2", then this method will throw a `EdgeAlreadyExists` exception._**
**_Note: If using connectTo or connectFrom, both nodes have to be from the same graph, otherwise a `DifferingGraphsException` will be thrown._**

Notice that before executing this method, no node with ID "n2" existed. The `Graph` automatically created this node to make it even easier to create graphs. If you want to check if a node exists, you can use the `nodeExists` method. After creating the edge, the edge will have an ID "n1 -> n2". This edge ID is automatically created by the static method `createEdgeID` from `Graph`. This ID is final and will be used to represent an edge when converting the `Graph` into a DOT file. If we were to convert `g2` into a DOT file, it would look like:

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
        n1 [];
        n2 [];<br>
        n1 -> n2 [];
}
</prev>
    </div>
    <div
        style="
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: 20px;
        "
        >
    <p>Visualized</p>
    <img src="./assets/graphs/edgeAddition.svg" />
    </div>
</div>

Another way to add an edge is by using two `Node` objects within the same graph. Simply call the `connectTo` method of the source `Node`, and pass in the destination `Node`. Or conversely, you can use the `connectFrom` of the destination `Node` and pass in the source `Node`. Either way, the `Graph` will create a directed edge from the source `Node` to the destination `Node`. These alternative methods are commented out the

**_Note: If a node with the ID passed into `getNode` does not exist, then a `NodeDoesNotExistException` will be thrown._**

### Check edge existance

To check if an edge exsits between two nodes, you can use the `edgeExists` method provided by the `Graph`. Remember, order matters since all edges are directed edges.

```java
Graph g = new Graph("Master");
Edge e1 = g.addEdge("n1", "n2");

System.out.println(g.edgeExists("n1", "n2")); // true
System.out.println(g.edgeExists("n1", "n1")); // false
```

This method is useful to check if a edge exists before removing a edge.

### Get an edge

A `Graph` allows you to retrieve an edge through the `getEdge` method which requires the IDs of the nodes associated with that edge in the correct order.

```java
Graph g = new Graph("Master");
g.addEdge("n1", "n2");

Edge e = g.getEdge("n1", "n2"); // Get the edge "n1 -> n2"
```

This method is useful for getting an edge that you don't have a direct reference to.

### Remove an edge

To remove an `Edge` from a `Graph`, you can simply use the `removeEdge` method provided by the `Graph`. This method requires the IDs of the two nodes connected by the edge. The first parameter is the ID of the source node, and the second parameter is the ID of the destination node. **The order matters!** An example of removing an edge from a complete digraph of size three will be provided:

```java
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

g.removeEdge("2", "0");
```

A total of three nodes were created with IDs corresponding to their number in the creation process. After the complete digraph of size five was complete, the edge "2 -> 0" was removed.

<div style="display: flex; justify-content: center;">
    <div style="display: flex; flex-direction: column; align-items: center;">
        <p>Before</p>
        <img src="./assets/graphs/complete5_before_edge_removal.svg" alt="Before" style="width: 60%;"/>
    </div>
    <div style="display: flex; flex-direction: column; align-items: center;">
        <p>After</p>
        <img src="./assets/graphs/complete5_after_edge_removal.svg" alt="After" style="width: 60%;"/>
    </div>
</div>

### Attributes

Since a `Graph` inherits from a `DOTElement`, it can have any number of attributes. To learn how to set, get, and remove attributes, see [Attributes](#attributes).

### Get number of nodes

For convenience, a `Graph` lets you get the node count by calling the `getNumberOfNodes` method. This effectively returns the size of the graph's nodes hashmap.

```java
Graph g = new Graph("Master");
g.addNodes("n1", "n2", "n3");
g.addEdge("n4", "n5");

System.out.println(g.getNumberOfNodes()); // 5
```

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
        n1 [];
        n2 [];
        n3 [];
        n4 [];
}
</prev>
    </div>
    <div
        style="
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: 20px;
        "
        >
    <p>Visualized</p>
    <img src="./assets/graphs/getNumberOfNodes.svg" />
    </div>
</div>

### Get node names

For convenience, a `Graph` lets you get the IDs of its nodes by calling the `getNodeNames` method. This effectively returns the [ID](#ir-regex) of the nodes in the nodes hashmap.

```java
Graph g = new Graph("Master");
g.addNodes("n1", "n2", "n3");

System.out.println(g.getNodeNames()); // [n1, n2, n3]
```

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
        n1 [];
        n2 [];
        n3 [];
        n4 [];
}
</prev>
    </div>
    <div
        style="
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: 20px;
        "
        >
    <p>Visualized</p>
    <img src="./assets/graphs/getNodeNames.svg" />
    </div>
</div>

### Get node labels

For convenience, a `Graph` lets you get the labels of its nodes by calling the `getNodeLabels` method. This returns all the labels of the nodes in the nodes hashmap.

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");
Node n2 = g.addNode("n2");

n1.setAttribute(Node.Attribute.LABEL, "n1 label");
n2.setAttribute(Node.Attribute.LABEL, "n2 label");

System.out.println(g.getNodeLabels()); // [n1=n1 label, n2=n2 label]
```

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
        n1 [];
        n2 [];
        n3 [];
        n4 [];
}
</prev>
    </div>
    <div
        style="
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: 20px;
        "
        >
    <p>Visualized</p>
    <img src="./assets/graphs/getNodeLabels.svg" />
    </div>
</div>

### Get number of edges

For convenience, a `Graph` lets you get the edge count by calling the `getNumberOfEdges` method. This effectively returns the size of the graph's edges hashmap.

```java
Graph g = new Graph("Master");
Node[] nodes = g.addNodes("n1", "n2", "n3");
Node root = g.addNode("root");
for (Node node : nodes) {
    root.connectTo(node);
}

System.out.println(g.getNumberOfEdges()); // 3
```

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
        n1 [];
        n2 [];
        n3 [];
        n4 [];
}
</prev>
    </div>
    <div
        style="
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: 20px;
        "
        >
    <p>Visualized</p>
    <img src="./assets/graphs/getNumberOfEdges.svg" />
    </div>
</div>

### Get edge directions

For convenience, a `Graph` lets you get the edge directions by calling the `getEdgeDirections` method. This returns all the edge [ID](#ir-regex)s from the edges hashmap. An edge ID looks like "node1 -> node2".

```java
Graph g = new Graph("Master");
Node[] nodes = g.addNodes("n1", "n2", "n3");
Node root = g.addNode("root");

for (Node node : nodes) {
    root.connectTo(node);
}

g.getNode("n2").connectTo(g.addNode("n4"));

System.out.println(g.getEdgeDirections()); // [root -> n1, root -> n2, root -> n3, n2 -> n4]
```

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
        n1 [];
        n2 [];
        n3 [];
        n4 [];
}
</prev>
    </div>
    <div
        style="
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-left: 20px;
        "
        >
    <p>Visualized</p>
    <img src="./assets/graphs/getEdgeDirections.svg" />
    </div>
</div>

### Convert to DOT

Any `Graph` can be converted to an equivalent DOT graph. To do this, you can call the `toDot` method (no arguments). The DOT graph will be a digraph with a name equal to the ID of the `Graph` object, its nodes, and its edges, and all attributes associate with the graph, nodes, and edges. As an example, we'll create a complete digraph of size three with attributes and convert it to a DOT graph.

```java
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
```

The equivalent DOT graph is the following:

```dot
digraph Master {
        bgcolor="pink";

        0 [color="red" label="I am node 0"];
        1 [color="yellow" label="I am node 1"];
        2 [color="orange" label="I am node 2"];

        0 -> 1 [fillcolor="teal" color="red" label="0 -> 1"];
        0 -> 2 [fillcolor="teal" color="red" label="0 -> 2"];
        1 -> 0 [fillcolor="grey" color="yellow" label="1 -> 0"];
        1 -> 2 [fillcolor="grey" color="yellow" label="1 -> 2"];
        2 -> 0 [fillcolor="blue" color="orange" label="2 -> 0"];
        2 -> 1 [fillcolor="blue" color="orange" label="2 -> 1"];
}
```

### Output graph

A `Graph` can also be converted to a wide variety of formats using the `outputGraph` method which takes in a filepath (String) and a format (Format) enum value. If the filepath provided does not end in the extension of the format, it will be automatically added. For example, if the filepath is "mygraph" and the format is "Format.DOT", the new filepath will be "filepath.dot".

The list of output formats was acquired from the official [Graphviz Output Formats page](https://graphviz.org/docs/outputs/). [Graphviz Output Formats page](https://graphviz.org/docs/outputs/).

An example of converting a graph to a JSON value is provided:

```java
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
```

## Node

A `Node` can only be created from a Graph object via the `addNode` method. It also needs to reference its `Graph` in order to call the appropriate methods of its `Graph`. For example, calling the `connectTo` method calls its graph's `addEdge` method. All of the methods provided by `Node` (excluding the [Attribute](#attribute) methods) follow this design pattern to ensure that it's strictly the `Graph` that manages the nodes and edge operations. These methods are purely for convenience and are by no means necessary for the functionality of `Graph`.

A `Node` also stores its 'to' and 'from' edges for efficient node and edge operations. A 'to' edge reefers to an edge where the node itself is the source node. In contrast, a 'from' edge refers to an edge where the node itself is a destination node.

### Connect to another node

You can connect one `Node` to another by callind the `connectTo` method. This method takes in the destination `Node` and these two nodes **must** share the same graph. Under the hood, this method is calling the node's graph's `addEdge` method, and creates an edge "calling node -> parameter node".

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");
Node n2 = g.addNode("n2");

n1.connectTo(n2); // Edge created: "n1 -> n2"
```

**_Note: If the graphs of both nodes do not match, then a `DifferingGraphsException` will be thrown._**

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
        n1 [];
        n2 [];

</div>

### Connect from another node

The `connectFrom` method is similar to `connectTo` in that you provide a another `Node`, and an edge is created between the two nodes. For this method, you specify the source node to create the edge "parameter node -> calling node".

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");
Node n2 = g.addNode("n2");

n1.connectFrom(n2); // Edge created: "n2 -> n1"
```

**_Note: If the graphs of both nodes do not match, then a `DifferingGraphsException` will be thrown._**

<div style="display: flex; flex-direction: row">
    <div
    style="
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20px;
    "
    >
    <p>DOT</p>
<pre>
digraph Master {
        n1 [];
        n2 [];

</div>

### Get a 'to' edge

A `Node` allows you to access its 'to' edges using the `to` method that accepts a `Node` object. A 'to' edge refers to an edge where the other node is the destination node.

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");
Node n2 = g.addNode("n2");

n1.connectTo(n2);
Edge edge = n1.to(n2); // Retrieve the edge "n1 -> n2"
```

**_Note: If a node object is passed that is not part of its 'to' edges, then a `EdgeDoesNotExistException` will be thrown._**

### Get a 'from' edge

A `Node` allows you to access its 'from' edges using the `from` method that accepts a `Node` object. A 'from' edge refers to an edge where the other node is the source node.

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");
Node n2 = g.addNode("n2");

n1.connectTo(n2);
Edge edge = n2.from(n1); // Retrieve the edge "n1 -> n2"
```

### Disconnect from a 'to' node

If an edge "n1 -> n2" exists, then the node with ID "n1" can disconnect from "n2", removing the edge "n1 -> n2". To do this, you can call the `disconnectTo` method provided by the `Node`. The method takes in a reference to the other node as an argument.

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");
Node n2 = g.addNode("n2");

n1.connectTo(n2);
n1.disconnectTo(n2); // Removes the edge "n1 -> n2"
```

### Disconnect from a 'from' node

If an edge "n1 -> n2" exists, then the node with ID "n2" can disconnect from "n1", removing the edge "n1 -> n2". To do this, you can call the `disconnectFrom` method provided by the `Node`. The method takes in a reference to the other node as an argument.

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");
Node n2 = g.addNode("n2");

n1.connectTo(n2);
n2.disconnectFrom(n1); // Removes the edge "n1 -> n2"
```

### Remove from graph

A `Node` can remove itself from a graph using the `removeFromGraph` method, which takes no arguments. As a result, the node's graph reference will be set to null since it is no longer part of that graph.

```java
Graph g = new Graph("Master");
Node n1 = g.addNode("n1");

n1.removeFromGraph(); // Removes the node with "n1"
```

## Edge

An `Edge` is a directed edge and stores references to its source node and destination node. An edge can only be created by a `Graph` to ensure edge operations perform correctly. The only method (excluding its [Attribute](#attributes) methods) is the `removeFromGraph` method. More methods may be provided in the future if there is a demand for it.

### Remove from graph

An `Edge` can be removed from a graph by calling the `removeFromGraph` method which takes no arguments. Behind the scenes, the `removeEdge` method will be called from the `Graph` associated with its source and destinatino nodes. The nodes 'to'and 'from' hashMaps will also be updated appropriately to reflect the removal of this `Edge`.

```java
Graph g = new Graph("Master");
Edge e1 = g.addEdge("n1", "n2");

e1.removeFromGraph(); // Removes the edge "n1 -> n2"
```

## Attributes

All three classes, `Graph`, `Node`, and `Edge` inherit from the `DOTElement` superclass. This superclass is an abstract class that manages the object's attributes. By default, a newly created `DOTElement` has no attributes. By inheritng from `DOTElement`, the `Graph`, `Node`, and `Edge` can all have and manage their own attributes.

### Setting an attribute

To give a `DOTElement` an attribute, you can do so by calling the `setAttribute` method that takes in a attribute (String) and value (String) as its arguments. The attribute and value will then be stored in a HashMap for quick access. The following is an example of setting some attributes for each type of `DOTElement`:

```java
Graph g = new Graph();
g.setAttribute("custom_attribute", "some value");
g.setAttribute(Graph.Attribute.BGCOLOR, "red"); // Setting the "bgcolor" attribute to "red"
// g.setAttribute("bgcolor", "red"); // Equivalent way to set the same attribute
```

### Getting an attribute

To get an attribute from a `DOTElement`, you can use the `getAttribute`, which takes in a attribute (String), and uses it to retrieve the attribute's value.

```java
Graph g = new Graph();
g.setAttribute("custom_attribute", "some value");
g.setAttribute(Graph.Attribute.BGCOLOR, "red");
// g.setAttribute("bgcolor", "red"); // Equivalent way to set the same attribute

g.getAttribute(Graph.Attribute.BGCOLOR); // Retrieve attribute value is "red"
```

### Removing an attribute

Removing an attribute from a `DOTElement` can be achieved using the `removeAttribute` method, which takes in the attribute (String).

```java
Graph g = new Graph();
g.setAttribute("custom_attribute", "some value");
g.setAttribute(Graph.Attribute.BGCOLOR, "red");
// g.setAttribute("bgcolor", "red"); // Equivalent way to set the same attribute

g.removeAttribute(Graph.Attribute.BGCOLOR); // Attribute "bgcolor" removed
```

### Custom attributes

Each class `Graph`, `Node`, and `Edge` has its own nested Attribute enums and purely exists for convenience. The attribute methods can either take in an appropriate Attribute enum value, or it can take any string (as long it matches the [ID regex](#id-regex)). This means you can add custom attributes if needed.

## ID regex

In order to create a `Node`, you need to provide a valid ID. Furthermore, passing an ID to a `Graph` or setting attributes to a `DotElement` also need valid IDs. A valid ID is one that matches the ID regex which can be found as a final static variable in the `DotElement` class. This ID regex was acquired from the official [Graphviz Grammar page](https://graphviz.org/doc/info/lang.html).

See [Valid ID](#valid-id) and [Invalid ID](#invalid-id) to see some examples of valid and invalid IDs.

### Valid ID

- a1 (letters followed by digits are allowed)
- \_a (underscore is allowed)
- 1 (digit can be the first character if the ID only contains digits)

### Invalid ID

- node A (whitespace is not allowed)
- node! ("!" is not allowed)
- 1a (cannot start with a digit if it contains alpha letters after it)

## Example

Below are some examples demonstrating the capabilities of this tool.

### Logo

How the GraphMaster logo was created:

```java
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

        g.outputGraph("./assets/icons/logo", Format.SVG); // Output the logo as SVG
    }

    public static void setNodeAttributes(Node n, String label, double xPos, double yPos, double radius) {
        n.setAttribute(Node.Attribute.LABEL, label); // Set the 'label' attribute
        n.setAttribute(Node.Attribute.POS, String.format("%s,%s", xPos, yPos)); // Set the 'pos' attribute
        n.setAttribute(Node.Attribute.WIDTH, String.format("%s", radius)); // Set the 'width' attribute
        n.setAttribute(Node.Attribute.HEIGHT, String.format("%s", radius)); // Set the 'height' attribute
    }

    public static Graph intersectStrings(String s1, String s2, String intersectionCharacter, double radius,
            double angle) throws Exception {
        Graph g = new Graph();
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
```
