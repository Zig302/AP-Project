package servlets;

import java.io.IOException;
import java.io.OutputStream;
import server.RequestParser.RequestInfo;

/**
 * Servlet interface defines the contract for handling HTTP requests in the server.
 * Implementations should process the request and write the response to the output stream.
 * 
 * <p>Example implementation:
 * <pre>{@code
 * public class UserServlet implements Servlet {
 *     {@literal @}Override
 *     public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
 *         PrintWriter writer = new PrintWriter(toClient, true);
 *         writer.println("HTTP/1.1 200 OK");
 *         writer.println("Content-Type: application/json");
 *         writer.println();
 *         writer.println("{\"users\": []}");
 *     }
 *     
 *     {@literal @}Override
 *     public void close() throws IOException {
 *         // Clean up resources
 *     }
 * }
 * }</pre>
 */
public interface Servlet {
    /**
     * Handles an HTTP request and writes the response to the client.
     * 
     * @param ri the parsed request information including URI, headers, and body
     * @param toClient the output stream to write the HTTP response to
     * @throws IOException if an I/O error occurs while processing the request or response
     */
    void handle(RequestInfo ri, OutputStream toClient) throws IOException;
    
    /**
     * Closes the servlet and releases any resources it holds.
     * Called when the servlet is being removed or the server is shutting down.
     * 
     * @throws IOException if an I/O error occurs while closing resources
     */
    void close() throws IOException;
}
