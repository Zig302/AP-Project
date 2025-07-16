package servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import server.RequestParser;

/**
 * HtmlLoader servlet serves static HTML files from a specified root directory.
 * Handles security by preventing directory traversal attacks.
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Serve HTML files from the "html_files" directory
 * HTTPServer server = new MyHTTPServer(8080, 10);
 * server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
 * 
 * // Now clients can access:
 * // GET /app/index.html -> serves html_files/index.html
 * // GET /app/about.html -> serves html_files/about.html
 * // GET /app/ -> serves html_files/index.html (default)
 * }</pre>
 */
public class HtmlLoader implements Servlet {
    private final String root;

    /**
     * Creates a new HtmlLoader servlet.
     * 
     * @param root the root directory path containing HTML files to serve
     */
    public HtmlLoader(String root) {
        this.root = root;
    }
    
    /**
     * Handles the HTTP request by loading an HTML file from the specified root directory.
     * The file is determined by the URI in the request.
     *
     * @param ri RequestInfo containing details about the HTTP request
     * @param toClient OutputStream to write the response to the client
     * @throws IOException if an I/O error occurs while reading the file or writing the response
     */
    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        String uri = ri.getUri();
        
        // Remove /app/ prefix to get the actual file path
        String filePath = uri.substring(5); // Remove "/app/"
        
        // Handle root path
        if (filePath.isEmpty() || filePath.equals("/")) {
            filePath = "index.html";
        }
        
        // Construct the full file path
        Path fullPath = Paths.get(root, filePath);
        File file = fullPath.toFile();
        
        if (!file.exists() || !file.isFile()) {
            sendNotFoundResponse(toClient, filePath);
            return;
        }
        
        // Ensure the file is within the root directory (security check)
        if (!file.getCanonicalPath().startsWith(new File(root).getCanonicalPath())) {
            sendNotFoundResponse(toClient, filePath);
            return;
        }
        
        try {
            // Read the file content
            byte[] fileContent = Files.readAllBytes(fullPath);
            
            // Determine content type based on file extension
            String contentType = getContentType(filePath);
            
            // Send successful response
            sendSuccessResponse(toClient, fileContent, contentType);
            
        } catch (IOException e) {
            sendNotFoundResponse(toClient, filePath);
        }
    }
    
    private String getContentType(String filePath) {
        String lowercasePath = filePath.toLowerCase();
        if (lowercasePath.endsWith(".html") || lowercasePath.endsWith(".htm")) {
            return "text/html";
        } else if (lowercasePath.endsWith(".css")) {
            return "text/css";
        } else if (lowercasePath.endsWith(".js")) {
            return "application/javascript";
        } else if (lowercasePath.endsWith(".png")) {
            return "image/png";
        } else if (lowercasePath.endsWith(".jpg") || lowercasePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowercasePath.endsWith(".gif")) {
            return "image/gif";
        } else {
            return "text/plain";
        }
    }
    
    private void sendSuccessResponse(OutputStream toClient, byte[] content, String contentType) throws IOException {
        String headers = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "\r\n";
        
        toClient.write(headers.getBytes());
        toClient.write(content);
        toClient.flush();
    }
    
    private void sendNotFoundResponse(OutputStream toClient, String requestedPath) throws IOException {
        String html = "<html><body><h1>404 - File Not Found</h1><p>The requested file '" + 
                     requestedPath + "' was not found on this server.</p></body></html>";
        
        String response = "HTTP/1.1 404 Not Found\r\n" +
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
