package main;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Graph {
	public Map<String, Vertex> vertices;
	public Set<EdgeImpl> edges;
	
	public Graph(){
		vertices = new HashMap<>();
		edges = new HashSet<>();
	}
	
    public Vertex getVertexFromString(String vertexString){
        if(!vertices.containsKey(vertexString)) {
            Vertex vertex = Vertex.fromString(vertexString);
            vertices.put(vertexString, vertex);
        }
        return vertices.get(vertexString);
    }
    
    public void removeEdge(EdgeImpl e){
    	//System.out.println("Edge to be removed " + e.getSourceVertex()+" "+e.getDestinationVertex());
    	
    	e.getSourceVertex().getOutgoingEdges().remove(e);
    	e.getDestinationVertex().getIncomingEdges().remove(e);
    	edges.remove(e);
    }
    
	public void putVertex(Vertex vertex){
		vertices.put(vertex.toString(), vertex);
	}
	
	public void putEdge(String edgeName, Vertex sourceVertex, Vertex destinationVertex, String dataType){		
		if(edgeName.equals(""))
			edgeName = "Unnamed_" + EdgeImpl.getNextEdgeID();
		EdgeImpl insideEdge = new EdgeImpl(edgeName, sourceVertex, destinationVertex, dataType);
		edges.add(insideEdge);
	}
    
    @Override
    public String toString() {
    	String output = "";
    	output += "digraph {\n";
        for (EdgeImpl edge : edges) {
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
    
    
    public void collapseAllMetaVertices(){
    	List<MetaVertex> metaVertexes = new LinkedList<>();
    	for(Vertex v : vertices.values())
    		if(v instanceof MetaVertex)
    			metaVertexes.add((MetaVertex)v);
    	for(MetaVertex v : metaVertexes)
    		v.mergeWithParent();
    }
    
    
    
    public String printWithSchedule(List<List<Vertex>> schedule){
    	String output = "";
    	output += "digraph {\n";
    	
    	for(int i = 0; i < schedule.size(); i++){
    		output += "subgraph cluster"+i+" {\n";
    		output += "label=\"CSUF "+i+"\";\n";
    		//output += "rank=same;\n";
    		for(Vertex v : schedule.get(i))
    			output += "\""+v+"\";";
    		output += "}\n";
    	}
    	
    	
        for (EdgeImpl edge : edges) {
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
