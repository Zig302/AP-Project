package servlets;

import server.RequestParser;

import java.io.IOException;
import java.io.OutputStream;

public class TopicDisplayer implements Servlet {
    /**
     * @param ri
     * @param toClient
     * @throws IOException
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
