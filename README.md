# Computational Graph Project

![Computational Graph Visualization](https://img.shields.io/badge/Visualization-vis--network-blue)
![Java](https://img.shields.io/badge/Language-Java-orange)
![MVC Architecture](https://img.shields.io/badge/Architecture-MVC-green)
![HTTP Server](https://img.shields.io/badge/Server-HTTP-yellow)

## Developers
- Ran Levi
- Alex Makarov

## Overview
This project implements a computational graph framework with a web-based visualization interface. The system allows users to define, load, and visualize computational graphs through a web interface. A computational graph consists of agents that process messages and topics that facilitate communication between agents.

## Key Components and Terminology

### Graph Components
- **Agent**: An interface that defines the basic functionality of processing nodes in the graph. Agents can subscribe to topics, receive messages, process them, and publish results to other topics.
- **Topic**: A communication channel that agents can publish to and subscribe to. Topics store the latest message and notify all subscribed agents when a new message is published.
- **Message**: A data container that holds the information passed between agents through topics.
- **Graph**: A collection of interconnected agents and topics forming a computational network.

### Special Agents
- **ParallelAgent**: A decorator for agents that enables asynchronous message processing using a background thread and a message queue. This allows for non-blocking message handling and improved performance.
- **BinOpAgent**: An abstract base class for binary operation agents (like addition, subtraction, etc.).
- **PlusAgent, SubAgent, MulAgent, DivAgent**: Concrete implementations of binary operation agents for arithmetic operations.
- **IncAgent, DecAgent**: Agents that increment or decrement values.

### Management Components
- **TopicManagerSingleton**: A singleton class that manages all topics in the system, ensuring there's only one instance of each topic and providing access to them.

### Configuration
- **Config**: An interface for loading and creating graph configurations.
- **GenericConfig**: A concrete implementation that reads configuration files and dynamically creates the appropriate agents and topics.
- **Graph**: A representation of the computational graph structure that can be traversed and visualized.
- **Node**: A representation of a node in the graph visualization.

## MVC Architecture

The project implements the Model-View-Controller (MVC) architectural pattern:

### Model Layer
The model consists of the core graph components:
- **Graph package**: Contains `Agent`, `Topic`, `Message`, and `TopicManagerSingleton` classes.
- **Config package**: Contains configuration classes that define how graphs are created and structured.

The model encapsulates the business logic and data structures needed to represent and process computational graphs.

### Controller Layer
The controller manages the interaction between the model and view:
- **Server package**: Contains `HTTPServer`, `MyHTTPServer`, and `RequestParser` classes that handle HTTP requests and responses.
- **Servlets package**: Contains various servlets that process specific types of requests:
  - `ConfLoader`: Handles configuration file uploads and initializes the graph system.
  - `HtmlLoader`: Serves static HTML files to the client.
  - `TopicDisplayer`: Publishes messages to topics and displays results.
  - `GraphRefresher`: Updates the graph visualization.
  - `TopicValidator`: Validates if a topic exists in the system.

The controller receives user inputs from the view, processes them, updates the model, and returns the appropriate response.

### View Layer
The view is responsible for presenting the graph to users:
- **Views package**: Contains the `HtmlGraphWriter` class that generates HTML for visualizing the graph.
- **HTML files**: Contains templates and static HTML files that provide the user interface.
  - Uses vis-network.js library for interactive graph visualization.

## Special Features

### Asynchronous Communication
- **Message Queue**: ParallelAgent implements a message queue to allow non-blocking communication.
- **Background Thread Processing**: Messages are processed in a background thread, allowing the main thread to continue execution.

### Parallelism
- **Thread Pool**: The HTTP server uses a thread pool to handle multiple clients concurrently.
- **Concurrent Collections**: Uses ConcurrentHashMap for thread-safe topic management.
- **ParallelAgent**: Enables parallel processing of messages in separate threads.

### Custom Graph Loading
- **Dynamic Agent Creation**: Uses reflection to instantiate agent classes specified in config files.
- **Configuration File Format**: Simple, text-based format for defining graphs, agents, and their connections.
- **Upload Interface**: Web interface for uploading and loading new graph configurations.

### Interactive UI
- **vis-network Integration**: Uses the vis-network JavaScript library to create an interactive graph visualization.
- **Real-time Updates**: Graph visualization updates as messages propagate through the system.
- **Node and Edge Styling**: Different visual styling for agents and topics for clear visualization.

### Additional Servlets
- **GraphRefresher**: Refreshes the graph visualization without reloading the entire page.
- **TopicValidator**: Validates topic existence before attempting to publish messages.

## Design Patterns and SOLID Principles

### Design Patterns
1. **Singleton Pattern**: Used in the TopicManagerSingleton class to ensure a single instance of the TopicManager.
2. **Decorator Pattern**: ParallelAgent decorates any Agent implementation with asynchronous processing capabilities.
3. **Observer Pattern**: Topics act as subjects, and agents act as observers in a publish-subscribe model.
4. **MVC Pattern**: Overall architecture follows the Model-View-Controller pattern.

### SOLID Principles
1. **Single Responsibility Principle**: Classes like Topic, Agent, and servlets have well-defined, single responsibilities.
2. **Open/Closed Principle**: The system is open for extension (e.g., adding new agent types) but closed for modification.
3. **Liskov Substitution Principle**: Different Agent implementations can be used interchangeably.
4. **Interface Segregation Principle**: Clean interfaces like Agent, Config, and Servlet define minimal required methods.
5. **Dependency Inversion Principle**: High-level modules depend on abstractions, not concrete implementations.


## How to Run

### Prerequisites
- Java Development Kit (JDK) 11 or higher (We used JDK 22)
- Web browser with JavaScript enabled

### Running the Application
1. Clone the repository:
   ```
   git clone https://github.com/Zig302/AP-Project.git
   cd AP-Project
   ```

2. Compile the Java files (If needed):
   ```
   javac -d bin src/**/*.java
   ```

3. Run the application:
   ```
   java -cp bin Main
   ```

4. Open a web browser and navigate to:
   ```
   http://localhost:8080/app/
   ```

### Using the Application
1. Upload a configuration file via the web interface, or use one of the sample configurations.
2. The graph will be visualized using the vis-network library.
3. Interact with the graph by sending messages to topics.
4. Watch as messages propagate through the computational graph.

## Sample Configuration Files
The repository includes several sample configuration files in the `config_files` directory:
- `simple.conf`: A basic graph with a few agents
- `fibonacci.conf`: A graph that generates Fibonacci numbers
- `graphTest.conf`: A more complex graph for testing

## Project Demo
[Watch the Demo Video](https://www.youtube.com/link_to_your_demo_video)

## License
This project is for academic purposes as part of the Advanced Programming course.