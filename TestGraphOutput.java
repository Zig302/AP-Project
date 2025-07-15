import views.HtmlGraphWriter;
import configs.Graph;
import java.util.List;

public class TestGraphOutput {
    public static void main(String[] args) {
        Graph graph = new Graph();
        graph.createFromTopics();
        List<String> htmlLines = HtmlGraphWriter.getGraphHTML(graph);
        
        System.out.println("=== Graph HTML Output ===");
        for (int i = 0; i < htmlLines.size(); i++) {
            String line = htmlLines.get(i);
            System.out.println("Line " + i + ": [" + line + "]");
            if (line.contains("script")) {
                System.out.println("  -> Script tag found!");
            }
        }
    }
}
