package servlets;

import graph.TopicManagerSingleton;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import server.RequestParser;

/**
 * TopicValidator servlet validates topic existence and message format.
 * Accepts GET requests with topic and message parameters to validate if a topic exists
 * and if the message is valid for that topic.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Register the topic validator
 * server.addServlet("GET", "/validate", new TopicValidator());
 * 
 * // Client can now validate topics and messages:
 * // GET /validate?topic=math&message=5
 * // GET /validate?topic=nonexistent&message=test
 * }</pre>
 */
public class TopicValidator implements Servlet {
    
    /**
     * Handles topic and message validation requests.
     * 
     * @param ri the parsed request information containing topic and message parameters
     * @param toClient the output stream to write the HTTP response to
     * @throws IOException if an I/O error occurs while processing the request
     */
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
