package server;

import servlets.Servlet;
import server.RequestParser.RequestInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* * MyHTTPServer is a simple HTTP server implementation that handles HTTP requests
 * and serves responses based on registered servlets.
 * It implements the HTTPServer interface and runs in a separate thread.
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
     * Constructor for MyHTTPServer.
     * @param port the port on which the server will listen for incoming HTTP requests
     * @param nThreads number of threads in the thread pool to handle requests
     */
    public MyHTTPServer(int port,int nThreads){
        this.port = port;
        this.nThreads = nThreads;
        this.threadPool = Executors.newFixedThreadPool(nThreads);
    }

    /**
     * Starts the HTTP server by binding to the specified port and listening for incoming requests
     *
     * @param httpCommand the HTTP command (e.g., "GET", "POST", "DELETE") to register the servlet for
     * @param uri the URI path for which the servlet should handle requests
     * @param s the servlet instance that will handle requests for the specified HTTP command and URI
     */
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
     * Starts the server and begins listening for incoming HTTP requests
     *
     * @param httpCommand the HTTP command (e.g., "GET", "POST", "DELETE") to register the servlet for
     * @param uri the URI path for which the servlet should handle requests
     */
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
     * Handles incoming client requests by parsing the request and invoking the appropriate servlet
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
     * The run method is the entry point for the thread
     * It will handle incoming HTTP requests in a loop until the server is stopped
     */
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
     * Starts the server and begins listening for incoming HTTP requests
     * This method should be called to initiate the server's operation
     */
    @Override
    public void start(){
        stopped = false;
        super.start(); // Call the run method in a new thread
    }

    /**
     * shuts down the thread pool and server
     */
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
