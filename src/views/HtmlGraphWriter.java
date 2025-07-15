package views;

import configs.Graph;
import configs.Node;
import java.util.ArrayList;
import java.util.List;

public class HtmlGraphWriter {
    
    public static List<String> getGraphHTML(Graph g) {
        List<String> htmlLines = new ArrayList<>();
        
        // Generate HTML header
        htmlLines.add("<!DOCTYPE html>");
        htmlLines.add("<html lang=\"en\">");
        htmlLines.add("<head>");
        htmlLines.add("    <meta charset=\"UTF-8\" />");
        htmlLines.add("    <title>Computational Graph</title>");
        htmlLines.add("    <link href=\"https://unpkg.com/vis-network@9.1.2/styles/vis-network.min.css\" rel=\"stylesheet\" />");
        htmlLines.add("    <script src=\"https://unpkg.com/vis-network@9.1.2/dist/vis-network.min.js\"></script>");
        htmlLines.add("    <style>");
        htmlLines.add("        html, body { height: 100%; margin: 0; }");
        htmlLines.add("        #network { width: 100%; height: 100%; }");
        htmlLines.add("    </style>");
        htmlLines.add("</head>");
        htmlLines.add("<body>");
        htmlLines.add("    <div id=\"network\"></div>");
        htmlLines.add("    <script>");
        
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
        
        // Add nodes and edges to HTML
        htmlLines.add(nodesJson.toString());
        htmlLines.add(edgesJson.toString());
        
        // Add network configuration and creation
        htmlLines.add("        const options = {");
        htmlLines.add("            layout: { improvedLayout: true },");
        htmlLines.add("            physics: { enabled: true },");
        htmlLines.add("            interaction: { dragNodes: true, dragView: true, zoomView: true },");
        htmlLines.add("            nodes: { font: { align: \"center\" } },");
        htmlLines.add("            edges: { arrows: { to: { enabled: true, scaleFactor: 1 } } }");
        htmlLines.add("        };");
        htmlLines.add("        const container = document.getElementById(\"network\");");
        htmlLines.add("        const network = new vis.Network(container, { nodes, edges }, options);");
        htmlLines.add("    </script>");
        htmlLines.add("</body>");
        htmlLines.add("</html>");
        
        return htmlLines;
    }
}
