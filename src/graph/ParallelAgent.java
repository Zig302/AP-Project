package graph;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent{
    private Agent agent; // The agent to be executed in parallel
    private BlockingQueue<Message> queue; // Queue to hold messages for the agent
    private Thread messageThread; // Thread to process messages asynchronously
    private volatile boolean running = true; // Flag to control the running state of the agent


    public ParallelAgent(Agent agent, int queueSize) {
        // Constructor for ParallelAgent, initializes with an agent and a queue size
        this.agent = agent;
        this.queue = new ArrayBlockingQueue<>(queueSize);
        startMessageThread();
    }

    public ParallelAgent(Agent agent) {
        // Constructor for ParallelAgent with default queue size
        this(agent, 200); // Default capacity of 200
    }

    private void startMessageThread() {
        // Start a new thread to process messages from the queue
        messageThread = new Thread(() -> {
            while (running) {
                try {
                    Message msg = queue.take(); // Take a message from the queue
                    String[] msgParts = msg.asText.split(":", 2);
                    if (msgParts.length == 2) {
                        String topic = msgParts[0].trim(); // Extract topic from message
                        Message realMessage = new Message(msgParts[1].trim()); // Create a new Message object
                        agent.callback(topic, realMessage); // Call the agent's callback method with the topic and message
                    }

                } catch (InterruptedException e) {
                    if (!running) {
                        break; // Exit the loop if running is set to false
                    }
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
            }
        });
        messageThread.start(); // Start the message processing thread
    }

    @Override
    public String getName() {
        // Return the name of the wrapped agent
        return agent.getName();
    }

    @Override
    public void reset() {
        // Reset the agent
        agent.reset();
    }

    @Override
    public void callback(String topic, Message msg) {
        // Add the message to the queue for processing
        try {
            queue.put(new Message(topic + ":" + msg.asText)); // Add message to the queue with topic
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
    }

    @Override
    public void close() {
        // Close the ParallelAgent, stopping the message processing thread
        running = false; // Set running to 'false' to stop the thread
        messageThread.interrupt(); // Interrupt the thread to wake it up if it's waiting
        try {
            messageThread.join(); // Wait for the thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
        }
        agent.close(); // Close the agent
    }


}
