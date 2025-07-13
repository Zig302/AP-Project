package configs;

import graph.Message;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Node {
    private String name;
    private List<Node> edges;
    private Message msg;

    public Node(String name) {
        this.name = name;
        this.edges = new ArrayList<>();
        this.msg = null;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<Node> getEdges() {
        return edges;
    }
    public void setEdges(List<Node> edges) {
        this.edges = edges;
    }
    public Message getMsg() {
        return msg;
    }
    public void setMsg(Message msg) {
        this.msg = msg;
    }

    // Add an edge to the edges list
    public void addEdge(Node node) {
        this.edges.add(node);
    }

    // Check if the node has cycles using depth-first search
    public boolean hasCycles() {
        return dfs(this, new HashSet<>(), new HashSet<>());
    }

    // Helper method to check for cycles in the graph
    private boolean dfs(Node node, Set<Node> inStack, Set<Node> done) {
        if (inStack.contains(node)){
            return true;   // back-edge
        }
        if (done.contains(node)) {
            return false;  // already fully explored
        }

        inStack.add(node);
        for (Node nbr : node.getEdges()) {
            if (dfs(nbr, inStack, done))
                return true;
        }
        inStack.remove(node);
        done.add(node);
        return false;
    }

    // Get the message for this node, returns a default message if none is set
    public Message getMessage() {
        if (this.msg == null) {
            return new Message("0"); // Return a default message if no message is set
        }
        return this.msg; // Return the current message
    }
    // Set the message for this node
    public void setMessage(Message msg) {
        this.msg = msg; // Set the current message
    }
}