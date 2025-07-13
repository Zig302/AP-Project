package graph;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class TopicManagerSingleton {

    public static TopicManager get(){
        // Return the singleton instance of TopicManager
        return TopicManager.instance;
    }

    public static class TopicManager{
        private static final TopicManager instance = new TopicManager(); // Singleton instance of TopicManager
        public ConcurrentHashMap<String, Topic> topics = new ConcurrentHashMap<>(); // Map to hold topics by name

        // Private constructor to prevent creation of TopicManager instances
        private TopicManager() {}

        // Singleton instance of TopicManager
        private static TopicManager get(){
            // Return the singleton instance
            return TopicManager.instance;
        }

        public Topic getTopic(String name){
            // Get or create a topic by name
            return topics.computeIfAbsent(name, k -> new Topic(name));
        }

        public Collection<Topic> getTopics() {
            // Return collection of topics
            return topics.values();
        }

        public void clear(){
            // Clear all topics in the TopicManager
            topics.clear();
        }
    }
}
