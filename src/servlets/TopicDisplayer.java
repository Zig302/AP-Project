package servlets;

import server.RequestParser;
import graph.TopicManagerSingleton;
import graph.Topic;
import graph.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class TopicDisplayer implements Servlet {
    /**
     * @param ri
     * @param toClient
     * @throws IOException
     */
    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        Map<String, String> params = ri.getParameters();
        String topicName = params.get("topic");
        String messageText = params.get("message");
        
        if (topicName == null || messageText == null) {
            sendErrorResponse(toClient, "Missing topic or message parameter");
            return;
        }
        
        // Get the topic and publish the message
        Topic topic = TopicManagerSingleton.get().getTopic(topicName);
        Message message = new Message(messageText);
        topic.publish(message);
        
        // Send HTML response with a table showing ALL topics and their current values
        // Also include JavaScript to refresh the graph frame
        String html = generateAllTopicsTable();
        sendSuccessResponse(toClient, html);
    }
    
    private String generateAllTopicsTable() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n")
            .append("<head>\n")
            .append("    <title>All Topics Status</title>\n")
            .append("    <style>\n")
            .append("        body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append("        table { border-collapse: collapse; width: 100%; }\n")
            .append("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
            .append("        th { background-color: #f2f2f2; }\n")
            .append("        .topic-row { background-color: #f9f9f9; }\n")
            .append("    </style>\n")
            .append("    <script>\n")
            .append("        // Refresh the graph frame when topics are updated\n")
            .append("        window.onload = function() {\n")
            .append("            try {\n")
            .append("                parent.frames['graphFrame'].location.href = '/refresh';\n")
            .append("            } catch (e) {\n")
            .append("                console.log('Could not refresh graph frame:', e);\n")
            .append("            }\n")
            .append("        };\n")
            .append("    </script>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("    <h3>All Topics Status</h3>\n")
            .append("    <table>\n")
            .append("        <tr><th>Topic</th><th>Last Value</th><th>Publishers</th><th>Subscribers</th></tr>\n");
        
        // Get all topics and display them
        for (Topic topic : TopicManagerSingleton.get().getTopics()) {
            String topicName = topic.getName();
            String lastValue = topic.getMsg().asText;
            String publishers = topic.getPubs().size() + " agents";
            String subscribers = topic.getSubs().size() + " agents";
            
            html.append("        <tr class=\"topic-row\">")
                .append("<td>").append(topicName).append("</td>")
                .append("<td>").append(lastValue).append("</td>")
                .append("<td>").append(publishers).append("</td>")
                .append("<td>").append(subscribers).append("</td>")
                .append("</tr>\n");
        }
        
        html.append("    </table>\n")
            .append("</body>\n")
            .append("</html>");
        
        return html.toString();
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
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                         "Content-Type: text/html\r\n" +
                         "Content-Length: " + html.length() + "\r\n" +
                         "\r\n" +
                         html;
        toClient.write(response.getBytes());
        toClient.flush();
    }

    /**
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        // Nothing to close for this servlet
    }
}
