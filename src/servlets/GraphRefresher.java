package servlets;

import configs.Graph;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import server.RequestParser;
import views.HtmlGraphWriter;

/**
 * GraphRefresher servlet generates and returns the current graph visualization.
 * Accepts GET requests to refresh and display the current state of the graph
 * based on existing topics and their connections.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Register the graph refresher
 * server.addServlet("GET", "/refresh", new GraphRefresher());
 * 
 * // Client can now refresh the graph:
 * // GET /refresh -> returns HTML with current graph visualization
 * }</pre>
 */
public class GraphRefresher implements Servlet {
    
    /**
     * Handles graph refresh requests by generating current graph visualization.
     * 
     * @param ri the parsed request information
     * @param toClient the output stream to write the HTTP response to
     * @throws IOException if an I/O error occurs while processing the request
     */
    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            // Create a new graph from current topics
            Graph graph = new Graph();
            graph.createFromTopics();
            
            // Generate HTML for the graph
            List<String> htmlLines = HtmlGraphWriter.getGraphHTML(graph);
            StringBuilder html = new StringBuilder();
            for (String line : htmlLines) {
                html.append(line).append("\n");
            }
            
            // Send the HTML response
            sendSuccessResponse(toClient, html.toString());
            
        } catch (Exception e) {
            sendErrorResponse(toClient, "Error refreshing graph: " + e.getMessage());
        }
    }
    
    private void sendSuccessResponse(OutputStream toClient, String html) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                         "Content-Type: text/html\r\n" +
                         "Content-Length: " + html.length() + "\r\n" +
                         "\r\n" +
                         html;
        toClient.write(response.getBytes());
        toClient.flush();
    }
    
    private void sendErrorResponse(OutputStream toClient, String message) throws IOException {
        String html = "<html><body><h1>Error</h1><p>" + message + "</p></body></html>";
        String response = "HTTP/1.1 500 Internal Server Error\r\n" +
                         "Content-Type: text/html\r\n" +
                         "Content-Length: " + html.length() + "\r\n" +
                         "\r\n" +
                         html;
        toClient.write(response.getBytes());
        toClient.flush();
    }
    
    @Override
    public void close() throws IOException {
        // Nothing to close for this servlet
    }
}