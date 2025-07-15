package views;

import configs.Graph;
import configs.Node;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HtmlGraphWriter {
    
    public static List<String> getGraphHTML(Graph g) {
        List<String> htmlLines = new ArrayList<>();
        
        try {
            // Read the template file
            String templatePath = "html_files/graph.html";
            String template = new String(Files.readAllBytes(Paths.get(templatePath)));
            
            // Generate nodes and edges data
            StringBuilder nodesJson = new StringBuilder();
            StringBuilder edgesJson = new StringBuilder();
            
            nodesJson.append("        const nodes = new vis.DataSet([");
            edgesJson.append("        const edges = new vis.DataSet([");
            
            int nodeIdCounter = 1;
            List<Node> nodeList = new ArrayList<>();
            
            // Create nodes
            for (Node node : g) {
                nodeList.add(node);
                if (nodesJson.length() > 47) { // If not the first node
                    nodesJson.append(",");
                }
                nodesJson.append("\n");
                
                String nodeType = node.getName().startsWith("T") ? "topic" : "agent";
                String label = node.getName().substring(1); // Remove T or A prefix
                
                // Remove #NUMBER from agent names
                if (nodeType.equals("agent") && label.contains("#")) {
                    label = label.substring(0, label.indexOf("#") - 1);
                }
                
                String messageValue = node.getMessage() != null ? node.getMessage().asText : "0";
                
                if (nodeType.equals("topic")) {
                    // Topics are boxes with amber color
                    nodesJson.append("            { id: ").append(nodeIdCounter)
                            .append(", label: \"").append(label).append(" \\n(").append(messageValue).append(")\", ")
                            .append("shape: \"box\", color: \"#FFD966\" }");
                } else {
                    // Agents are circles with blue color
                    nodesJson.append("            { id: ").append(nodeIdCounter)
                            .append(", label: \"").append(label).append("\", ")
                            .append("shape: \"circle\", color: \"#6FA8DC\" }");
                }
                nodeIdCounter++;
            }
            
            nodesJson.append("\n        ]);");
            
            // Create edges
            int edgeCount = 0;
            for (int i = 0; i < nodeList.size(); i++) {
                Node fromNode = nodeList.get(i);
                int fromId = i + 1;
                
                for (Node toNode : fromNode.getEdges()) {
                    int toId = nodeList.indexOf(toNode) + 1;
                    
                    if (edgeCount > 0) {
                        edgesJson.append(",");
                    }
                    edgesJson.append("\n");
                    edgesJson.append("            { from: ").append(fromId)
                            .append(", to: ").append(toId)
                            .append(", arrows: \"to\" }");
                    edgeCount++;
                }
            }
            
            edgesJson.append("\n        ]);");
            
            // Replace placeholders in template
            template = template.replace("{{NODES_DATA}}", nodesJson.toString());
            template = template.replace("{{EDGES_DATA}}", edgesJson.toString());
            
            // Split the template into lines for return
            String[] lines = template.split("\n");
            for (String line : lines) {
                htmlLines.add(line);
            }
            
        } catch (IOException e) {
            // Fallback: return error message
            htmlLines.add("<!DOCTYPE html>");
            htmlLines.add("<html><head><title>Error</title></head>");
            htmlLines.add("<body><h1>Error loading graph template: " + e.getMessage() + "</h1></body>");
            htmlLines.add("</html>");
        }
        
        return htmlLines;
    }
}
