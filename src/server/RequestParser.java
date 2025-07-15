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
            String[] uriArray = uri.split("\\?");
            String[] paramPairs = uriArray[1].split("&");
            for (String p : paramPairs) {
                String[] values = p.split("=");
                parameters.put(values[0], values[1]);
            }
        }

        String line;
        Map<String, String> header = new HashMap<>();
        int length = 0;
        while (!(line = reader.readLine()).isEmpty()) {
            String[] headers = line.split(": ");
            if (headers.length == 2) {
                header.put(headers[0], headers[1]);
                if (headers[0].equalsIgnoreCase("Content-Length")) {
                    length = Integer.parseInt(headers[1]);
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        if (length > 0) {
            while (!(line = reader.readLine()).isEmpty()) {
                if (line.contains("filename=")) {
                    parameters.put("filename", line.split("filename=")[1]);
                }
            }
            // Read the content
            char[] body = new char[length];
            reader.read(body, 0, length);
            requestBody = new String(body);
            requestBody = requestBody.split("-")[0].trim();

            while (reader.ready()) {
                line = reader.readLine();
            }
        }
        return new RequestInfo(httpCommand, uri, segments, parameters, requestBody.getBytes());
    }

    /**
     * The RequestInfo class represents the parsed information of an HTTP request.
     */
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