package configs;

import java.util.ArrayList;
import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class Graph extends ArrayList<Node>{

    // Return true if the graph has cycles, false otherwise
    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) {
                return true; // Cycle detected
            }
        }
        return false; // No cycles found
    }

    // Creates the graph from the topics managed by the topic singleton
    public void createFromTopics(){
        this.clear(); // Clear existing nodes to rebuild the graph
        TopicManager topicManager = TopicManagerSingleton.get(); // Get the singleton instance of TopicManager

        for (Topic topic : topicManager.getTopics()) {
            Node node = new Node("T" + topic.getName()); // Create a node for the topic
            // Set the current message value for the topic node
            node.setMessage(topic.getMsg());
            this.add(node);

            for (Agent pub : topic.getPubs()) { // For each publisher in the topic
                Node pubNode = findNode("A" + pub.getName()); // Find a node by name or create a new one if not found
                pubNode.addEdge(node); // Create an edge from the publisher node to the topic node
            }
            for (Agent sub : topic.getSubs()) { // For each subscriber in the topic
                Node subNode = findNode("A" + sub.getName()); // Find a node by name or create a new one if not found
                node.addEdge(subNode); // Create an edge from the topic node to the subscriber node
            }
        }
    }

    // Find a node by name or create a new one if not found
    private Node findNode(String name) {
        for (Node node : this) {
            if (node.getName().equals(name)) {
                return node; // Return the existing node if found
            }
        }
        Node newNode = new Node(name); // Create a new node if not found
        this.add(newNode);
        return newNode; // Return the newly created node
    }

    // A helper method for me to print the graph into console
    public void printGraph() {
        for (Node node : this) {
            System.out.print(node.getName() + " => ");
            for (Node edge : node.getEdges()) {
                System.out.print(edge.getName() + " ");
            }
            System.out.println();
        }
    }

    // Updates the message values in topic nodes with the current values from topics
    public void updateNodeValues(){
        TopicManager topicManager = TopicManagerSingleton.get();
        
        for (Node node : this) {
            if (node.getName().startsWith("T")) { // If it's a topic node
                String topicName = node.getName().substring(1); // Remove "T" prefix
                Topic topic = topicManager.getTopic(topicName);
                node.setMessage(topic.getMsg()); // Update the node's message with the topic's current message
            }
        }
    }
}
