package servlets;

import configs.GenericConfig;
import configs.Graph;
import graph.TopicManagerSingleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import server.RequestParser;
import views.HtmlGraphWriter;

/**
 *
 */
public class ConfLoader implements Servlet {
    
    // Keep track of the current configuration to clean it up
    private static GenericConfig currentConfig = null;
    
    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            // Check if there's a file in the request body
            byte[] contentBytes = ri.getContent();
            if (contentBytes == null || contentBytes.length == 0) {
                sendErrorResponse(toClient, "No configuration file uploaded");
                return;
            }
            
            // Clean up previous configuration if it exists
            if (currentConfig != null) {
                currentConfig.close();
            }
            
            // Clear the TopicManager to remove all previous topics and agents
            TopicManagerSingleton.get().clear();
            
            // Create directory if it doesn't exist
            File uploadDir = new File("uploaded_configs");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            
            // Save the uploaded file
            String fileName = "config_" + System.currentTimeMillis() + ".conf";
            File configFile = new File(uploadDir, fileName);
            
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                fos.write(contentBytes);
            }
            
            // Create GenericConfig and Graph
            GenericConfig config = new GenericConfig();
            config.setConfFile(configFile.getAbsolutePath());
            config.create();
            
            // Store reference to current config for future cleanup
            currentConfig = config;
            
            Graph graph = new Graph();
            graph.createFromTopics();
            
            // Generate HTML representation
            List<String> htmlLines = HtmlGraphWriter.getGraphHTML(graph);
            StringBuilder htmlContent = new StringBuilder();
            for (String line : htmlLines) {
                htmlContent.append(line).append("\n");
            }
            
            // Send success response
            sendSuccessResponse(toClient, htmlContent.toString());
            
        } catch (RuntimeException | IOException e) {
            sendErrorResponse(toClient, "Error processing configuration: " + e.getMessage());
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
        String html = "<html><body><h1>Configuration Error</h1><p>" + message + "</p></body></html>";
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                         "Content-Type: text/html\r\n" +
                         "Content-Length: " + html.length() + "\r\n" +
                         "\r\n" +
                         html;
        toClient.write(response.getBytes());
        toClient.flush();
    }

    @Override
    public void close() throws IOException {
        // Clean up current configuration when servlet is closed
        if (currentConfig != null) {
            currentConfig.close();
            currentConfig = null;
        }
        TopicManagerSingleton.get().clear();
    }
}
