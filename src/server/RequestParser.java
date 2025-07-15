package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * The RequestParser class provides methods to parse HTTP requests.
 */
public class RequestParser {

    /**
     * Parses an HTTP request from the given BufferedReader.
     *
     * @param reader the BufferedReader to read the request from
     * @return a RequestInfo object containing the parsed request information
     * @throws IOException if an I/O error occurs
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String firstLine = reader.readLine(); // Read the first line (request line)

        if (firstLine == null) {
            throw new IOException();
        }

        String[] firstLineParts = firstLine.split(" "); // Split by spaces
        String requestBody = "";
        // Extract HTTP command, URI, and protocol version
        String httpCommand = firstLineParts[0];
        String uri = firstLineParts[1];

        ArrayList<String> address = new ArrayList<>();
        for (String s : uri.split("/")) {
            if (!s.isEmpty()) {
                if (s.contains("?")) {
                    address.add(s.split("\\?")[0]);
                } else {
                    address.add(s);
                }
            }
        }
        uri = uri.trim();

        // Create an array to store the segments
        String[] segments = new String[address.size()];
        segments = address.toArray(segments);

        // Parse query parameters (if any)
        Map<String, String> parameters = new HashMap<>();

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
         * Constructs a RequestInfo object with the specified parameters.
         *
         * @param httpCommand the HTTP command (e.g., GET, POST)
         * @param uri the URI of the request
         * @param uriSegments the segments of the URI
         * @param parameters the query parameters of the request
         * @param content the content of the request
         */
        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        /**
         * Returns the HTTP command of the request.
         *
         * @return the HTTP command
         */
        public String getHttpCommand() {
            return httpCommand;
        }

        /**
         * Returns the URI of the request.
         *
         * @return the URI
         */
        public String getUri() {
            return uri;
        }

        /**
         * Returns the segments of the URI.
         *
         * @return the URI segments
         */
        public String[] getUriSegments() {
            return uriSegments;
        }

        /**
         * Returns the query parameters of the request.
         *
         * @return the query parameters
         */
        public Map<String, String> getParameters() {
            return parameters;
        }

        /**
         * Returns the content of the request.
         *
         * @return the content
         */
        public byte[] getContent() {
            return content;
        }
    }
}