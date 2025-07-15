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
        String requestLine = reader.readLine(); // Read the first line (request line)

        if (requestLine == null) {
            throw new IOException();
        }

        String[] requestParts = requestLine.split(" "); // Split by spaces
        // Extract HTTP method, URI, and protocol version
        String httpMethod = requestParts[0];
        String requestUri = requestParts[1];

        // Parse query parameters first (if any)
        Map<String, String> queryParams = extractQueryParameters(requestUri);

        // Extract URI segments
        String[] uriSegments = extractUriSegments(requestUri);

        // Parse HTTP headers
        Map<String, String> httpHeaders = new HashMap<>();
        int contentLength = parseHeaders(reader, httpHeaders, queryParams);

        // Process request body
        String bodyContent = "";
        if (contentLength > 0) {
            bodyContent = processRequestBody(reader, contentLength);
        }

        return new RequestInfo(httpMethod, requestUri.trim(), uriSegments, queryParams, bodyContent.getBytes());
    }

    private static Map<String, String> extractQueryParameters(String uri) {
        Map<String, String> queryParams = new HashMap<>();
        
        if (uri.contains("?")) {
            String[] uriParts = uri.split("\\?");
            String[] parameterPairs = uriParts[1].split("&");
            for (String paramPair : parameterPairs) {
                String[] keyValue = paramPair.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
        
        return queryParams;
    }

    private static String[] extractUriSegments(String uri) {
        ArrayList<String> pathSegments = new ArrayList<>();
        
        for (String segment : uri.split("/")) {
            if (!segment.isEmpty()) {
                if (segment.contains("?")) {
                    pathSegments.add(segment.split("\\?")[0]);
                } else {
                    pathSegments.add(segment);
                }
            }
        }
        
        return pathSegments.toArray(new String[0]);
    }

    private static int parseHeaders(BufferedReader reader, Map<String, String> headers, Map<String, String> params) throws IOException {
        String currentLine;
        int contentLength = 0;
        
        while (!(currentLine = reader.readLine()).isEmpty()) {
            String[] headerParts = currentLine.split(": ");
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
                if (headerParts[0].equalsIgnoreCase("Content-Length")) {
                    contentLength = Integer.parseInt(headerParts[1]);
                }
            }
        }
        
        return contentLength;
    }

    private static String processRequestBody(BufferedReader reader, int contentLength) throws IOException {
        String currentLine;
        Map<String, String> tempParams = new HashMap<>();
        
        // Process multipart form data headers
        while (!(currentLine = reader.readLine()).isEmpty()) {
            if (currentLine.contains("filename=")) {
                tempParams.put("filename", currentLine.split("filename=")[1]);
            }
        }
        
        // Read the actual content
        char[] contentBuffer = new char[contentLength];
        reader.read(contentBuffer, 0, contentLength);
        String bodyContent = new String(contentBuffer);
        bodyContent = bodyContent.split("-")[0].trim();

        // Clear remaining buffer
        while (reader.ready()) {
            reader.readLine();
        }
        
        return bodyContent;
    }

    /**
     * The RequestInfo class represents the parsed information of an HTTP request.
     */
    public static class RequestInfo {
        private final String httpMethod;
        private final String requestUri;
        private final String[] pathSegments;
        private final Map<String, String> queryParameters;
        private final byte[] bodyContent;

        /**
         * Constructs a RequestInfo object with the specified parameters.
         *
         * @param httpMethod the HTTP method (e.g., GET, POST)
         * @param requestUri the URI of the request
         * @param pathSegments the segments of the URI
         * @param queryParameters the query parameters of the request
         * @param bodyContent the content of the request
         */
        public RequestInfo(String httpMethod, String requestUri, String[] pathSegments, Map<String, String> queryParameters, byte[] bodyContent) {
            this.httpMethod = httpMethod;
            this.requestUri = requestUri;
            this.pathSegments = pathSegments;
            this.queryParameters = queryParameters;
            this.bodyContent = bodyContent;
        }

        /**
         * Returns the HTTP method of the request.
         *
         * @return the HTTP method
         */
        public String getHttpCommand() {
            return httpMethod;
        }

        /**
         * Returns the URI of the request.
         *
         * @return the URI
         */
        public String getUri() {
            return requestUri;
        }

        /**
         * Returns the segments of the URI.
         *
         * @return the URI segments
         */
        public String[] getUriSegments() {
            return pathSegments;
        }

        /**
         * Returns the query parameters of the request.
         *
         * @return the query parameters
         */
        public Map<String, String> getParameters() {
            return queryParameters;
        }

        /**
         * Returns the content of the request.
         *
         * @return the content
         */
        public byte[] getContent() {
            return bodyContent;
        }
    }
}