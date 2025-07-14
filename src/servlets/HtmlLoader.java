package servlets;

import server.RequestParser;

import java.io.IOException;
import java.io.OutputStream;


public class HtmlLoader implements Servlet {
    private final String root;


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

    }

    /**
     * @throws IOException
     */
    @Override
    public void close() throws IOException {

    }
}
