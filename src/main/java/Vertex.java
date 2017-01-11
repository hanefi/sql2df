package main;
import java.util.LinkedList;
import java.util.List;

public abstract class Vertex {
	
	List<EdgeImpl> outgoingEdges;
	List<EdgeImpl> incomingEdges;
	String vertexName;
	public int id;
	public String inputCardinality;
	public String outputCardinality;
	
	public static int VERTEX_COUNTER = 0;
	
	public Vertex(String vertexName, int id){
		this.vertexName = vertexName;
		outgoingEdges = new LinkedList<>();
		incomingEdges = new LinkedList<>();
		inputCardinality = "1";
		outputCardinality = "1";
		this.id = id;
	}
	
	public Vertex(String vertexName){
		this(vertexName, VERTEX_COUNTER++);
	}
	
	public List<EdgeImpl> getOutgoingEdges(){
		return outgoingEdges;
	}
	
	public List<EdgeImpl> getIncomingEdges(){
		return incomingEdges;
	}
	
	public void removeOutgoingEdge(EdgeImpl edge){
		outgoingEdges.remove(edge);
	}
	
	public void removeIncomingEdge(EdgeImpl edge){
		incomingEdges.remove(edge);
	}
	
	public void addOutgoingEdge(EdgeImpl edge){
		outgoingEdges.add(edge);
	}
	
	public void addIncomingEdge(EdgeImpl edge){
		incomingEdges.add(edge);
	}
	/**
	 * Returns the total data size of outgoing edges.
	 * @return The total data size of outgoing edges.
	 */
	public int getFanOut(){
		int fanOut = 0;
		for(EdgeImpl edge : outgoingEdges){
			fanOut += EdgeImpl.dataSize(edge.dataType);
		}
		return fanOut;
	}
	
	/**
	 * Returns the total data size of incoming edges.
	 * @return The total data size of incoming edges.
	 */
	public int getFanIn(){
		int fanIn = 0;
		for(EdgeImpl edge : incomingEdges){
			fanIn += EdgeImpl.dataSize(edge.dataType);
		}
		return fanIn;
	}
	
	public String getInputCardinality(){
		return inputCardinality;
	}
	
	public String getOutputCardinality(){
		return outputCardinality;
	}
}
