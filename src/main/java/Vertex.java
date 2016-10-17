package main;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Vertex {
	
	List<ExpressionEdgeImpl> outgoingEdges;
	List<ExpressionEdgeImpl> incomingEdges;
	
	public Vertex(){
		outgoingEdges = new LinkedList<>();
		incomingEdges = new LinkedList<>();
	}
	
	public List<ExpressionEdgeImpl> getOutgoingEdges(){
		return outgoingEdges;
	}
	
	public List<ExpressionEdgeImpl> getIncomingEdges(){
		return incomingEdges;
	}
	
	public void removeOutgoingEdge(ExpressionEdgeImpl edge){
		outgoingEdges.remove(edge);
	}
	
	public void removeIncomingEdge(ExpressionEdgeImpl edge){
		incomingEdges.remove(edge);
	}
	
	public void addOutgoingEdge(ExpressionEdgeImpl edge){
		outgoingEdges.add(edge);
	}
	
	public void addIncomingEdge(ExpressionEdgeImpl edge){
		incomingEdges.add(edge);
	}
	
	public int getFanOut(){
		int fanOut = 0;
		for(ExpressionEdgeImpl edge : outgoingEdges){
			fanOut += ExpressionEdgeImpl.dataSize(edge.dataType);
		}
		return fanOut;
	}
	
	public int getFanIn(){
		int fanIn = 0;
		for(ExpressionEdgeImpl edge : incomingEdges){
			fanIn += ExpressionEdgeImpl.dataSize(edge.dataType);
		}
		return fanIn;
	}
	
	public static Vertex fromString(String vertexString) {
		try{
			return TableVertex.fromString(vertexString);
		} catch(IllegalArgumentException e) {
			return VertexImpl.fromString(vertexString);
		}
	}
}
