package servlets;

import server.RequestParser;
import graph.TopicManagerSingleton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class TopicValidator implements Servlet {
    
    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        Map<String, String> params = ri.getParameters();
        String topicName = params.get("topic");
        String message = params.get("message");
        
        // Both topic and message are required
        if (topicName != null && message != null) {
            validateTopicAndMessage(topicName, message, toClient);
        } else {
            sendErrorResponse(toClient, "Both topic and message parameters are required");
        }
    }
    
    private void validateTopicAndMessage(String topicName, String message, OutputStream toClient) throws IOException {
        // Check if the topic exists
        if (!TopicManagerSingleton.get().topicExists(topicName)) {
            sendNotFoundResponse(toClient, "Topic not found");
            return;
        }
        
        // Check if the message contains only numbers
        if (!isNumeric(message)) {
            sendBadRequestResponse(toClient, "Message must contain only numbers");
            return;
        }
        
        // Both validations passed
        sendSuccessResponse(toClient);
    }
    
    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void sendSuccessResponse(OutputStream toClient) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                         "Content-Type: text/plain\r\n" +
                         "Content-Length: 2\r\n" +
                         "\r\n" +
                         "OK";
        toClient.write(response.getBytes());
        toClient.flush();
    }
    
    private void sendNotFoundResponse(OutputStream toClient, String message) throws IOException {
        String response = "HTTP/1.1 404 Not Found\r\n" +
                         "Content-Type: text/plain\r\n" +
                         "Content-Length: " + message.length() + "\r\n" +
                         "\r\n" +
                         message;
        toClient.write(response.getBytes());
        toClient.flush();
    }
    
    private void sendBadRequestResponse(OutputStream toClient, String message) throws IOException {
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                         "Content-Type: text/plain\r\n" +
                         "Content-Length: " + message.length() + "\r\n" +
                         "\r\n" +
                         message;
        toClient.write(response.getBytes());
        toClient.flush();
    }
    
    private void sendErrorResponse(OutputStream toClient, String message) throws IOException {
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                         "Content-Type: text/plain\r\n" +
                         "Content-Length: " + message.length() + "\r\n" +
                         "\r\n" +
                         message;
        toClient.write(response.getBytes());
        toClient.flush();
    }

    @Override
    public void close() throws IOException {
        // Nothing to close for this servlet
    }
}
