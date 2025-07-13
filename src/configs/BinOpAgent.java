package configs;

import java.util.function.BinaryOperator;
import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class BinOpAgent implements Agent {

    private String name; // Name of the agent
    private String input1; // Name of the first input topic
    private String input2; // Name of the second input topic
    private String output; // Name of the output topic
    private BinaryOperator<Double> operation; // Binary operation to perform on the inputs
    private Double x; // Temporary variable to hold the first input value
    private Double y; // Temporary variable to hold the second input value


    /**
     * Constructor for BinOpAgent
     * @param name Name of the agent
     * @param input1 Name of the first input topic
     * @param input2 Name of the second input topic
     * @param output Name of the output topic
     * @param operation Binary operation to perform on the inputs
     */
    public BinOpAgent(String name, String input1, String input2, String output, BinaryOperator<Double> operation) {
        this.name = name;
        this.input1 = input1;
        this.input2 = input2;
        this.output = output;
        this.operation = operation;

        // Subscribe to input topics and add this agent as a publisher to the output topic
        TopicManagerSingleton.get().getTopic(input1).subscribe(this);
        TopicManagerSingleton.get().getTopic(input2).subscribe(this);
        TopicManagerSingleton.get().getTopic(output).addPublisher(this);
    }

    /**
     * Check which input topic triggered the callback
     * Once both x and y are non-null, apply the binary operation and then publish the result to the output topic
     */
    @Override
    public void callback(String topic, Message msg) {
        // Check which input topic the message is from and update the corresponding variable
        if (topic.equals(input1)) {
            this.x = msg.asDouble;
        } else if (topic.equals(input2)) {
            this.y = msg.asDouble;
        }

        // If both inputs are available, perform the operation and publish the result
        if (this.x != null && this.y != null) {
            Double result = this.operation.apply(this.x, this.y);
            TopicManagerSingleton.get().getTopic(this.output).publish(new Message(result));
        }
    }

    @Override
    public void close() {
        // Unsubscribe from input topics and remove this agent as a publisher from the output topic
        TopicManagerSingleton.get().getTopic(input1).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(input2).unsubscribe(this);
        TopicManagerSingleton.get().getTopic(output).removePublisher(this);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void reset() {
        this.x = null; // Reset x to null
        this.y = null; // Reset y to  null
    }
}
