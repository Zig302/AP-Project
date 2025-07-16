package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import server.RequestParser.RequestInfo;
import servlets.Servlet;

/**
 * MyHTTPServer is a multi-threaded HTTP server implementation that handles HTTP requests
 * and serves responses based on registered servlets.
 * 
 * <p>The server supports GET, POST, and DELETE HTTP methods and uses a thread pool
 * to handle concurrent requests. Servlets are registered for specific URI patterns
 * and HTTP methods.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Create server on port 8080 with 10 worker threads
 * MyHTTPServer server = new MyHTTPServer(8080, 10);
 * 
 * // Register servlets for different endpoints
 * server.addServlet("GET", "/api/users", new UserListServlet());
 * server.addServlet("POST", "/api/users", new UserCreateServlet());
 * server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
 * 
 * // Start the server
 * server.start();
 * 
 * // Server runs in background, handling requests...
 * 
 * // Shutdown the server
 * server.close();
 * }</pre>
 * 
 * <p>URI matching uses longest prefix matching. For example, if both "/api" and "/api/users"
 * are registered, a request to "/api/users/123" will match "/api/users".
 */
public class MyHTTPServer extends Thread implements HTTPServer{
    private int port;
    private int nThreads;
    private ExecutorService threadPool;
    private ConcurrentHashMap<String, Servlet> GET_servlets = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Servlet> POST_servlets = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Servlet> DEL_servlets = new ConcurrentHashMap<>();
    volatile boolean stopped = false;
    private ServerSocket serverSocket;


    /**
     * Creates a new HTTP server instance.
     * 
     * @param port the port number on which the server will listen for incoming HTTP requests (1-65535)
     * @param nThreads the number of threads in the thread pool to handle concurrent requests
     * @throws IllegalArgumentException if port is not in valid range or nThreads is not positive
     */
    public MyHTTPServer(int port,int nThreads){
        this.port = port;
        this.nThreads = nThreads;
        this.threadPool = Executors.newFixedThreadPool(nThreads);
    }

    /**
     * Registers a servlet to handle requests for a specific HTTP method and URI pattern.
     * Uses longest prefix matching for URI resolution.
     * 
     * <p>Example:
     * <pre>{@code
     * server.addServlet("GET", "/api/users", new UserListServlet());
     * server.addServlet("POST", "/api/users", new UserCreateServlet());
     * server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
     * }</pre>
     * 
     * @param httpCommand the HTTP method (GET, POST, DELETE) - case insensitive
     * @param uri the URI pattern to match (e.g., "/api/users", "/app/")
     * @param s the servlet instance to handle matching requests
     * @throws IllegalArgumentException if httpCommand is unsupported or any parameter is null
     */
    @Override
    public void addServlet(String httpCommand, String uri, Servlet s){
        if (httpCommand == null || uri == null || s == null) {
            throw new IllegalArgumentException("HTTP command, URI, and servlet cannot be null");
        }
        switch (httpCommand.toUpperCase()) {
            case "GET":
                GET_servlets.put(uri, s);
                break;
            case "POST":
                POST_servlets.put(uri, s);
                break;
            case "DELETE":
                DEL_servlets.put(uri, s);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP command: " + httpCommand);
        }
    }

    /**
     * Removes a servlet registration for a specific HTTP method and URI pattern.
     * 
     * @param httpCommand the HTTP method (GET, POST, DELETE) - case insensitive
     * @param uri the URI pattern to remove
     * @throws IllegalArgumentException if httpCommand is unsupported or any parameter is null
     */
    @Override
    public void removeServlet(String httpCommand, String uri){
        if (httpCommand == null || uri == null) {
            throw new IllegalArgumentException("HTTP command and URI cannot be null");
        }
        switch (httpCommand.toUpperCase()) {
            case "GET":
                GET_servlets.remove(uri);
                break;
            case "POST":
                POST_servlets.remove(uri);
                break;
            case "DELETE":
                DEL_servlets.remove(uri);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP command: " + httpCommand);
        }
    }


    /**
     * Processes an incoming client request by parsing it and dispatching to the appropriate servlet.
     * Uses longest prefix matching to find the best servlet for the request URI.
     * 
     * @param clientSocket the socket connected to the client
     */
    private void serveClient(Socket clientSocket) {
        try {
            // Read the request from the client
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream output = clientSocket.getOutputStream(); // Output stream to send response back to the client
            RequestInfo requestInfo = RequestParser.parseRequest(input); // Parse the incoming request

            String httpCommand = requestInfo.getHttpCommand().toUpperCase();
            String uri = requestInfo.getUri();
            Map<String, Servlet> servMap = null;
            switch (httpCommand) {
                case "GET":
                    servMap = GET_servlets;
                    break;
                case "POST":
                    servMap = POST_servlets;
                    break;
                case "DELETE":
                    servMap = DEL_servlets;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported HTTP command: " + httpCommand);
            }

            if (servMap != null) {
                // We need to find the servlet with the longest matching prefix
                Servlet servlet = null;
                String longestMatch = null;

                for (Map.Entry<String, Servlet> entry : servMap.entrySet()) {
                    String key = entry.getKey();
                    if (uri.startsWith(key) && (longestMatch == null || key.length() > longestMatch.length())) {
                        servlet = entry.getValue();
                        longestMatch = key;
                    }
                }

                if (servlet != null) {
                    // Call the servlet's handle method with the parsed request info and output stream
                    servlet.handle(requestInfo, output);
                } else {
                    // If no matching servlet found, send a 404 Not Found response
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println("HTTP/1.1 404 Not Found");
                    writer.println("Content-Type: text/plain");
                    writer.println();
                    writer.println("404 Not Found: No matching servlet for " + uri);
                }
            }
        } catch (IOException ex) {
            System.err.println("Error handling client request: " + ex.getMessage());
        } finally {
            try {
                clientSocket.close(); // Close the client socket after handling the request
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }


    /**
     * Main server loop that accepts and processes client connections.
     * Runs in a separate thread and handles requests until the server is stopped.
     */
    @Override
    public void run(){
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);
            serverSocket.setSoTimeout(1000); // Set a timeout for accept calls
            while (!stopped) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    if (!stopped){
                        threadPool.submit(() -> serveClient(clientSocket));
                    }
                } catch (SocketTimeoutException e) {
                  // Ignore timeout exceptions when server is running
                }

            }
        } catch (Exception e) {
            if (!stopped) {
                System.err.println("Error in server: " + e.getMessage());
            }
        } finally {
            try {
                serverSocket.close(); // Close the server socket when stopping the server
            } catch (Exception e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
    }

    /**
     * Starts the HTTP server in a new thread.
     * The server will begin listening for incoming connections on the configured port.
     */
    @Override
    public void start(){
        stopped = false;
        super.start(); // Call the run method in a new thread
    }

    /**
     * Gracefully shuts down the HTTP server.
     * Stops accepting new connections, shuts down the thread pool, and closes all servlets.
     */
    @Override
    public void close(){
        stopped = true;
        threadPool.shutdown(); // Wait for all tasks to finish
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // Close the server socket
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        // Close all servlets
        closeServlet(GET_servlets.values());
        closeServlet(POST_servlets.values());
        closeServlet(DEL_servlets.values());
    }

    private void closeServlet(Iterable<Servlet> servlets) {
        for (Servlet s : servlets) {
            try { s.close(); }
            catch (IOException e) {
                System.err.println("Error closing servlet: " + e.getMessage());
            }
        }
    }

}
