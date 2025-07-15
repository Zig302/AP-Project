package servlets;

import server.RequestParser;
import views.HtmlGraphWriter;
import configs.Graph;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class GraphRefresher implements Servlet {
    
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