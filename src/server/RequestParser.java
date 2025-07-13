package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    /**
     * Parses an HTTP request from a BufferedReader according to the project specifications.
     *
     * @param reader BufferedReader containing the full HTTP request.
     * @return RequestInfo object containing all parsed information.
     * @throws IOException if there is an issue reading from the stream or if the request is malformed.
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        // Parse the Request Line (e.g., "GET /api/resource?id=123 HTTP/1.1")
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Request line is empty or null.");
        }

        String[] requestLineParts = requestLine.split(" ");
        if (requestLineParts.length < 2) {
            throw new IOException("Invalid request line: " + requestLine);
        }

        String httpCommand = requestLineParts[0];
        String uri = requestLineParts[1];

        // Parse URI Path Segments and Query Parameters
        Map<String, String> parameters = new HashMap<>();
        String path = uri;

        // Separate path from query string
        if (uri.contains("?")) {
            path = uri.substring(0, uri.indexOf("?"));
            String queryString = uri.substring(uri.indexOf("?") + 1);
            // Parse query string parameters
            String[] paramPairs = queryString.split("&");
            for (String pair : paramPairs) {
                // Split by "=" but only into two parts to allow "=" in the value
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                } else if (keyValue.length == 1 && !keyValue[0].isEmpty()){
                    parameters.put(keyValue[0], ""); // Handle params with no value
                }
            }
        }

        // Split the path into segments, ignoring empty parts
        List<String> uriSegmentsList = new ArrayList<>();
        for (String segment : path.split("/")) {
            if (!segment.isEmpty()) {
                uriSegmentsList.add(segment);
            }
        }
        String[] uriSegments = uriSegmentsList.toArray(new String[0]);

        // Parse Headers
        Map<String, String> headersMap = new HashMap<>();
        int contentLength = 0;
        String currentLine;
        while ((currentLine = reader.readLine()) != null && !currentLine.isEmpty()) {
            String[] headerParts = currentLine.split(": *", 2);
            if (headerParts.length == 2) {
                String headerName = headerParts[0].trim();
                String headerValue = headerParts[1].trim();
                headersMap.put(headerName, headerValue);

                // Specifically look for Content-Length to determine body size
                if (headerName.equalsIgnoreCase("Content-Length")) {
                    try {
                        contentLength = Integer.parseInt(headerValue);
                    } catch (NumberFormatException e) {
                        // Handle cases where Content-Length is not a valid number
                        System.err.println("Warning: Invalid Content-Length value: " + headerValue);
                        contentLength = 0;
                    }
                }
            }
        }

        // Handle post‐header parameters like 'filename="..."'
        while (reader.ready() && (currentLine = reader.readLine()) != null && !currentLine.isEmpty()) {
            if (currentLine.contains("=")) {
                String[] keyValue = currentLine.split("=", 2);
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                // Keep quotes around filename value
                parameters.put(key, value);
            }
        }

        // Read body content until an empty line or end of stream
        StringBuilder bodyBuilder = new StringBuilder();

        // If Content-Length is specified, read that many characters
        if (contentLength > 0) {
            char[] buf = new char[contentLength];
            int actuallyRead = reader.read(buf, 0, contentLength);
            if (actuallyRead > 0) {
                bodyBuilder.append(buf, 0, actuallyRead);
            }
        }

        // Keep consuming lines until we hit the first blank line or
        // the stream is exhausted (reader.ready() == false).
        while (reader.ready()) {
            currentLine = reader.readLine();
            if (currentLine == null || currentLine.isEmpty())
                break;                                // blank line means body finished
            bodyBuilder.append(currentLine).append('\n');
        }
        // Convert the body content to bytes
        byte[] content = bodyBuilder.toString().getBytes(StandardCharsets.UTF_8);

        // Construct and return RequestInfo
        return new RequestInfo(httpCommand, uri, uriSegments, parameters, content);
    }


	// RequestInfo given internal class
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        /**
         * Constructs a RequestInfo object with the parsed request details.
         *
         * @param httpCommand The HTTP command (e.g., GET, POST).
         * @param uri The full URI including query parameters.
         * @param uriSegments The segments of the URI path.
         * @param parameters The parsed query parameters.
         * @param content The content of the request body.
         */
        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        // Getters for the parsed request information
        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String[] getUriSegments() {
            return uriSegments;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
