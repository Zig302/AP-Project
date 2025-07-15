import server.HTTPServer;
import server.MyHTTPServer;
import servlets.ConfLoader;
import servlets.HtmlLoader;
import servlets.TopicDisplayer;
import servlets.GraphRefresher;
import servlets.TopicValidator;

public class Main {
    public static void main(String[] args) throws Exception{
        HTTPServer server=new MyHTTPServer(8080,5);
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("GET", "/refresh", new GraphRefresher());
        server.addServlet("GET", "/validate-topic", new TopicValidator());
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        server.start();
        System.in.read();
        server.close();
        System.out.println("done");
    }
}
