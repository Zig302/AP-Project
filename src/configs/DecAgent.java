package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class DecAgent implements Agent {
    /**
     * The DecAgent class implements the Agent interface and
     * calculates the decrement of one value received from subscribed topic
     * And publishes the result to the first output topic.
     */

    // The value to be decremented
    private double value1;
    private String inputTopic1; // Topic for the first value
    private String outputTopic; // Topic for the output result
    private String[] subs; // Subscribed topics
    private String[] pubs; // Published topics
    private Message msgFromTopic1 = null;
    private static int counter = 0; // Counter to generate unique names for agents
    private final String name; // Unique name for the agent


    public DecAgent(String[] subs, String[] pubs) {
        if (subs.length != 1 || pubs.length != 1) {
            throw new IllegalArgumentException("DecAgent requires exactly one input topic and one output topic.");
        }
        counter++; // Increment the counter to ensure unique names for each agent instance
        this.subs = subs;
        this.pubs = pubs;
        this.name = ("Dec_Agent_#" + counter); // Unique name for the agent based on the counter

        // Now we subscribe to the first topic from the subs array
        TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);

        // And we add ourselves as a publisher to the output topic
        TopicManagerSingleton.get().getTopic(pubs[0]).addPublisher(this);

        // Initialize the input and output topics names and values
        this.inputTopic1 = subs[0];
        this.outputTopic = pubs[0];
        this.value1 = 0.0;
    }


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        this.value1 = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        // Check which input topic the message is from and update the corresponding variable
        if (topic.equals(inputTopic1)) {
            this.msgFromTopic1 = msg;
            this.value1 = msg.asDouble;
        }
        // If the input is available, decrement the value and publish the result
        if (this.msgFromTopic1 != null) {
            double result = this.value1 - 1; // Decrement the value by 1
            TopicManagerSingleton.get().getTopic(this.outputTopic).publish(new Message(result));
        }

    }

    @Override
    public void close() {
        // Unsubscribe from input topic and remove this agent as a publisher from the output topic
        TopicManagerSingleton.get().getTopic(inputTopic1).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(outputTopic).removePublisher(this);
    }
}
