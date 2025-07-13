package server;

import servlets.Servlet;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ServerTesting { // RequestParser


    private static void testParseRequest() {
        // Test data
        String request = "GET /api/resource?id=123&name=test HTTP/1.1\n" +
                "Host: example.com\n" +
                "Content-Length: 5\n"+
                "\n" +
                "filename=\"hello_world.txt\"\n"+
                "\n" +
                "hello world!\n"+
                "\n" ;

        BufferedReader input=new BufferedReader(new InputStreamReader(new ByteArrayInputStream(request.getBytes())));
        try {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);

            // Test HTTP command
            if (!requestInfo.getHttpCommand().equals("GET")) {
                System.out.println("HTTP command test failed (-5)");
            }

            // Test URI
            if (!requestInfo.getUri().equals("/api/resource?id=123&name=test")) {
                System.out.println("URI test failed (-5)");
            }

            // Test URI segments
            String[] expectedUriSegments = {"api", "resource"};
            if (!Arrays.equals(requestInfo.getUriSegments(), expectedUriSegments)) {
                System.out.println("URI segments test failed (-5)");
                for(String s : requestInfo.getUriSegments()){
                    System.out.println(s);
                }
            }
            // Test parameters
            Map<String, String> expectedParams = new HashMap<>();
            expectedParams.put("id", "123");
            expectedParams.put("name", "test");
            expectedParams.put("filename","\"hello_world.txt\"");
            if (!requestInfo.getParameters().equals(expectedParams)) {
                System.out.println("Parameters test failed (-5)");
            }

            // Test content
            byte[] expectedContent = "hello world!\n".getBytes();
            if (!Arrays.equals(requestInfo.getContent(), expectedContent)) {
                System.out.println("Content test failed (-5)");
            }
            input.close();
        } catch (IOException e) {
            System.out.println("Exception occurred during parsing: " + e.getMessage() + " (-5)");
        }
    }


    public static void testServer() throws Exception {
        // Count initial threads
        int initialThreadCount = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> !t.isDaemon()).toArray().length;

        // Create and configure HTTP server on port 8080 with 1 thread
        HTTPServer server = new MyHTTPServer(5001, 1);

        // Create a servlet that handles /calc/sum endpoint
        Servlet sumServlet = new Servlet() {
            @Override
            public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
                Map<String, String> params = ri.getParameters();

                if (params.containsKey("a") && params.containsKey("b")) {
                    try {
                        double a = Double.parseDouble(params.get("a"));
                        double b = Double.parseDouble(params.get("b"));
                        double sum = a + b;

                        // Write HTTP response
                        PrintWriter writer = new PrintWriter(toClient);
                        writer.println("HTTP/1.1 200 OK");
                        writer.println("Content-Type: text/plain");
                        writer.println(); // Empty line between headers and body
                        writer.println(sum); // Write result to body
                        writer.flush();
                    } catch (NumberFormatException e) {
                        // Handle invalid number format
                        sendErrorResponse(toClient, "Invalid number format");
                    }
                } else {
                    // Handle missing parameters
                    sendErrorResponse(toClient, "Missing parameters");
                }
            }

            private void sendErrorResponse(OutputStream toClient, String message) throws IOException {
                PrintWriter writer = new PrintWriter(toClient);
                writer.println("HTTP/1.1 400 Bad Request");
                writer.println("Content-Type: text/plain");
                writer.println();
                writer.println(message);
                writer.flush();
            }

            @Override
            public void close() throws IOException {
                // Nothing to clean up
            }
        };

        // Register servlet at /calc/sum
        server.addServlet("GET", "/calc/sum", sumServlet);

        // Start the server
        server.start();

        // Wait a bit for the server to start
        Thread.sleep(100);

        // Verify that exactly one non-daemon thread was created
        int afterStartThreadCount = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> !t.isDaemon()).toArray().length;
        if (afterStartThreadCount != initialThreadCount + 1) {
            System.out.println("Incorrect number of threads created. Expected: " + (initialThreadCount + 1) +
                    ", Actual: " + afterStartThreadCount + " (-30)");        }

        // Test server functionality
        Socket socket = null;
        PrintWriter writer = null;
        BufferedReader reader = null;
        try {
            socket = new Socket("localhost", 5001);
            socket.setSoTimeout(5000); // Set 5 second timeout
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send HTTP request
            writer.println("GET /calc/sum?a=4.5&b=1.5 HTTP/1.1");
            writer.println("Host: localhost");
            writer.println(); // Empty line to indicate end of headers
            writer.flush();

            // Read server response
            StringBuilder response = new StringBuilder();
            String line;

            // Skip headers
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // Skip HTTP headers
            }

            // Read the body (only the first line since our response is simple)
            line = reader.readLine();
            if (line != null) {
                response.append(line);
            }

            // Check if the response is correct
            String result = response.toString().trim();
            if (!result.equals("6.0")) {
                System.out.println("Incorrect response. Expected: '6.0', Actual: '" + result + "' (-30)");
            }
        } catch (IOException e) {
            System.out.println("Error testing server: " + e.getMessage() + " (-60)");
        } finally {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket resources: " + e.getMessage());
            }
        }

        // Stop the server
        server.close();

        // Wait for threads to terminate
        Thread.sleep(2000);

        // Verify that all non-daemon threads created by server are gone
        int finalThreadCount = Thread.getAllStackTraces().keySet().stream()
                .filter(t -> !t.isDaemon()).toArray().length;
        if (finalThreadCount != initialThreadCount) {
            System.out.println("Server threads not properly terminated. Expected: " + initialThreadCount +
                    ", Actual: " + finalThreadCount + " (-30)");
        }
    }

    public static void main(String[] args) {
        testParseRequest(); // 40 points
        try{
            testServer(); // 60
        }catch(Exception e){
            System.out.println("your server throwed an exception (-60)");
        }
        System.out.println("done");
    }

}
