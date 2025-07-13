package configs;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class SubAgent implements Agent {
    /**
     * The SubAgent class implements the Agent interface and
     * calculates the substraction of two values received from subscribed topics
     */

    // The values to be subtracted
    private double value1;
    private double value2;
    private String inputTopic1; // Topic for the first value
    private String inputTopic2; // Topic for the second value
    private String outputTopic; // Topic for the output result
    private String[] subs; // Subscribed topics
    private String[] pubs; // Published topics
    private Message msgFromTopic1 = null;
    private Message msgFromTopic2 = null;
    private static int counter = 0; // Counter to generate unique names for agents
    private final String name; // Unique name for the agent


    public SubAgent(String[] subs, String[] pubs) {
        if (subs.length != 2 || pubs.length != 1) {
            throw new IllegalArgumentException("SubAgent requires exactly two input topics and one output topic.");
        }
        counter++; // Increment the counter to ensure unique names for each agent instance
        this.subs = subs;
        this.pubs = pubs;
        this.name = ("Sub_Agent_#" + counter); // Unique name for the agent based on the counter

        // Now we subscribe to the first two topics from the subs array
        TopicManagerSingleton.get().getTopic(subs[0]).subscribe(this);
        TopicManagerSingleton.get().getTopic(subs[1]).subscribe(this);

        // And we add ourselves as a publisher to the output topic
        TopicManagerSingleton.get().getTopic(pubs[0]).addPublisher(this);

        // Initialize the input and output topics names and values
        this.inputTopic1 = subs[0];
        this.inputTopic2 = subs[1];
        this.outputTopic = pubs[0];
        this.value1 = 0.0;
        this.value2 = 0.0;

    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        this.value1 = 0.0;
        this.value2 = 0.0;
    }

    @Override
    public void callback(String topic, Message msg) {
        // Check which topic the message is from and update the corresponding value
        if (topic.equals(inputTopic1)) {
            this.msgFromTopic1 = msg;
            this.value1 = msg.asDouble; // Update value1 with the message from topic1
        } else if (topic.equals(inputTopic2)) {
            this.msgFromTopic2 = msg;
            this.value2 = msg.asDouble; // Update value2 with the message from topic2
        }

        // If both values are set, calculate the diff and publish it to the output topic
        if (this.msgFromTopic1 != null && this.msgFromTopic2 != null) {
            double result = this.value1 - this.value2; // Calculate the diff of the two values
            Message resultMessage = new Message(result); // Create a new message with the result
            TopicManagerSingleton.get().getTopic(outputTopic).publish(resultMessage); // Publish the result to the output topic
        }

    }

    @Override
    public void close() {
        // Unsubscribe from the input topics and remove this agent as a publisher from the output topic
        TopicManagerSingleton.get().getTopic(inputTopic1).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(inputTopic2).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(outputTopic).removePublisher(this);

    }
}
