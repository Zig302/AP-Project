package graph;

import java.util.ArrayList;
import java.util.List;

public class Topic {
    public final String name;
    private final List<Agent> pubs = new ArrayList<>();; // List of publishers for this topic
    private final List<Agent> subs = new ArrayList<>();; // List of subscribers for this topic
    private Message msg = new Message("0");


    Topic(String name){
        // Constructor for Topic, initializes with a name
        this.name=name;
    }

    public void subscribe(Agent a){
        // Subscribe the agent to this topic
        if (!subs.contains(a)) {
            subs.add(a);
        }
    }

    public void unsubscribe(Agent a){
        // Unsubscribe the agent from this topic
        subs.remove(a);
    }

    public void publish(Message m){
        // Publish a message to all subscribers
        this.msg = m;
        for (Agent a : subs) {
            a.callback(this.name, m);
        }
    }

    public List<Agent> getPubs() {
        // Return the list of publishers for this topic
        return pubs;
    }

    public List<Agent> getSubs() {
        // Return the list of subscribers for this topic
        return subs;
    }

    public String getName() {
        // Return the name of the topic
        return name;
    }

    public Message getMsg() {
        // Return the last message published to this topic
        return this.msg;
    }

    public void addPublisher(Agent a){
        // Add a publisher to this topic
        if (!pubs.contains(a)) {
            pubs.add(a);
        }
    }


    public void removePublisher(Agent a){
        // Remove a publisher from this topic
        pubs.remove(a);
    }
}
