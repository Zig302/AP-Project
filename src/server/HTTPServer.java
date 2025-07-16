package server;

import servlets.Servlet;

/**
 * HTTPServer interface defines the contract for HTTP server implementations.
 * Provides methods to manage servlets and control server lifecycle.
 * 
 * <p>Example usage:
 * <pre>{@code
 * HTTPServer server = new MyHTTPServer(8080, 10);
 * server.addServlet("GET", "/api/users", new UserServlet());
 * server.addServlet("POST", "/api/data", new DataServlet());
 * server.start();
 * // ... server runs in background
 * server.close();
 * }</pre>
 */
public interface HTTPServer extends Runnable{
    /**
     * Registers a servlet to handle requests for a specific HTTP command and URI.
     * 
     * @param httpCommand the HTTP method (GET, POST, DELETE) to handle
     * @param uri the URI path pattern to match (e.g., "/api/users")
     * @param s the servlet instance to handle matching requests
     */
    public void addServlet(String httpCommand, String uri, Servlet s);
    
    /**
     * Removes a servlet registration for a specific HTTP command and URI.
     * 
     * @param httpCommand the HTTP method (GET, POST, DELETE) to unregister
     * @param uri the URI path pattern to remove
     */
    public void removeServlet(String httpCommand, String uri);
    
    /**
     * Starts the HTTP server and begins listening for incoming requests.
     * Server will run in a separate thread.
     */
    public void start();
    
    /**
     * Stops the HTTP server and releases all resources.
     * All registered servlets will be closed.
     */
    public void close();
}
