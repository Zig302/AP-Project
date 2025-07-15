package configs;

import graph.Agent;
import graph.ParallelAgent;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GenericConfig implements Config {
    /**
     * Given a configuration file, this class creates the agents and topics
     * Where the agents are defined in the configuration file by the following format (line by line):
     * project_name.config_files.(agent_name)
     * Topic1, Topic2, Topic3, etc. are the names of the topics that the agents subscribe to.
     * Topic1, Topic2, Topic3, etc. are the names of the topics that the agents publish to.
     */

    public String pathToConfigFile = "";
    public ArrayList<ParallelAgent> agents = new ArrayList<>();


    @Override
    public void create() {
        List<String> readLines = new ArrayList<>();
        File file = new File(pathToConfigFile);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) { // Read the file line by line
                readLines.add(scanner.nextLine());
            }
            scanner.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        if (readLines.size() % 3 != 0) {
            throw new IllegalArgumentException("Configuration file is not valid, it should have a multiple of 3 lines.");
        }

        for (int i = 0; i < readLines.size(); i += 3) { // Iterate through the lines in steps of 3
            String agentClassName = readLines.get(i); // The first line is the agent class name
            String[] inputTopics = readLines.get(i + 1).split(","); // The second line is the input topics
            String[] outputTopics = readLines.get(i + 2).split(","); // The third line is the output topics

            try {
                Class<?> agentClass = Class.forName(agentClassName); // Load the agent class dynamically
                Constructor<?> constructor = agentClass.getConstructor(String[].class, String[].class); // Get the constructor that takes two String arrays
                Object agentInstance = constructor.newInstance((Object) inputTopics, (Object) outputTopics); // Create an instance of the agent class
                ParallelAgent agent = new ParallelAgent((Agent) agentInstance); // Wrap the agent instance in a ParallelAgent
                agents.add(agent); // Add the created agent to the list of agents

            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Agent class not found: " + agentClassName + ". Make sure the class exists and is in the classpath.", e);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause != null) {
                    throw new RuntimeException("Error in agent constructor for: " + agentClassName + ". Agent threw exception: " + cause.getClass().getSimpleName() + " - " + cause.getMessage(), cause);
                } else {
                    throw new RuntimeException("Error invoking constructor for agent: " + agentClassName, e);
                }
            } catch (Exception e) {
                throw new RuntimeException("Unexpected error creating agent: " + agentClassName + " - " + e.getMessage(), e);
            }
        }
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getVersion() {
        return 0;
    }

    // Closes all agents and clears the list of agents
    @Override
    public void close() {
        for (ParallelAgent agent : agents) {
            agent.close();
        }
        agents.clear();
    }

    // Sets the path to the configuration file
    public void setConfFile(String s) {
        this.pathToConfigFile=s;
    }
}
