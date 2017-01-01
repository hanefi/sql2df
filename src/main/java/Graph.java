package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Graph {
	public Map<String, Vertex> vertices;
	public List<ExpressionEdgeImpl> edges;
	
	public Graph(){
		vertices = new HashMap<>();
		edges = new LinkedList<>();
	}
	
    public Vertex getVertexFromString(String vertexString){
        if(!vertices.containsKey(vertexString)) {
            Vertex vertex = Vertex.fromString(vertexString);
            vertices.put(vertexString, vertex);
        }
        return vertices.get(vertexString);
    }
    
    public void removeEdge(ExpressionEdgeImpl e){
    	e.getSourceVertex().getOutgoingEdges().remove(e);
    	e.getDestinationVertex().getIncomingEdges().remove(e);
    	edges.remove(e);
    }
    
	public void putVertex(Vertex vertex){
		vertices.put(vertex.toString(), vertex);
	}
    
    @Override
    public String toString() {
    	String output = "";
    	output += "digraph {\n";
        for (ExpressionEdgeImpl edge : edges) {
            //System.out.println(edge+ " "+ edge.getSourceVertex());
        	String tail = edge.getSourceVertex().getOutputCardinality();
        	String head = edge.getDestinationVertex().getInputCardinality();
            output += '"' + edge.getSourceVertex().toString() + '"' + " -> " + '"'
                    + edge.getDestinationVertex().toString() + '"' + "[label=\"" + edge.toString() + "\","
                    		+ " headlabel=\"" + head + "\", taillabel=\"" + tail + "\" ]\n";
        }
        output += "}\n";
        return output;
    }
    
    public String printAndCollapse(){
    	String output = "";
    	output += "digraph {\n";
    	
    	List<Collection<Vertex>> metaVertices = new LinkedList<>(); 
    	List<String> groupNames = new LinkedList<>();
    	
    	Iterator<Vertex> iter = vertices.values().iterator();
    	
    	while(iter.hasNext()){
    		Vertex v = iter.next();
    		if(v instanceof MetaVertex){
    			metaVertices.add(((MetaVertex)v).subGraph.vertices.values());
    			groupNames.add(v.vertexName);
    			((MetaVertex)v).mergeWithParent();
    			iter = vertices.values().iterator();
    		}
    	}
    	
    	for(int i = 0; i < metaVertices.size(); i++){
				output += "subgraph cluster"+i+" {\n";
				output += "label=\""+groupNames.get(i)+"\";\n";
				//output += "rank=same;\n";
				for(Vertex v : metaVertices.get(i))
					output += "\""+v+"\";";
				output += "}\n";
    	}
    	
        for (ExpressionEdgeImpl edge : edges) {
            //System.out.println(edge+ " "+ edge.getSourceVertex());
        	String tail = edge.getSourceVertex().getOutputCardinality();
        	String head = edge.getDestinationVertex().getInputCardinality();
            output += '"' + edge.getSourceVertex().toString() + '"' + " -> " + '"'
                    + edge.getDestinationVertex().toString() + '"' + "[label=\"" + edge.toString() + "\","
                    		+ " headlabel=\"" + head + "\", taillabel=\"" + tail + "\" ]\n";
        }
    	
        output += "}\n";
        return output;
    }
    
    
    public String printWithSchedule(List<List<Vertex>> schedule){
    	String output = "";
    	output += "digraph {\n";
    	
    	for(int i = 0; i < schedule.size(); i++){
    		output += "subgraph cluster"+i+" {\n";
    		output += "label=\"Instruction "+i+"\";\n";
    		//output += "rank=same;\n";
    		for(Vertex v : schedule.get(i))
    			output += "\""+v+"\";";
    		output += "}\n";
    	}
    	
    	
        for (ExpressionEdgeImpl edge : edges) {
            //System.out.println(edge+ " "+ edge.getSourceVertex());
        	String tail = edge.getSourceVertex().getOutputCardinality();
        	String head = edge.getDestinationVertex().getInputCardinality();
            output += '"' + edge.getSourceVertex().toString() + '"' + " -> " + '"'
                    + edge.getDestinationVertex().toString() + '"' + "[label=\"" + edge.toString() + "\","
                    		+ " headlabel=\"" + head + "\", taillabel=\"" + tail + "\" ]\n";
        }
    	
        output += "}\n";
        return output;
    }
}
