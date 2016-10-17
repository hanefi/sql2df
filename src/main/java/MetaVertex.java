package main;
import java.util.List;


public class MetaVertex extends Vertex {
	
	public static final String ROOT = "table: INPUTS";
	public static final String SINK = "table: OUTPUTS";
	private static int metaCnt = 0;
	
	private Graph parentGraph; //Exists if we ever want to collapse a graph
	private Graph subGraph;
	
	private String label;
	private int id;
	
	public MetaVertex(List<Vertex> vertices, Graph parentGraph, String label){
		this.parentGraph = parentGraph;
		subGraph = new Graph();
		putVertices(vertices, parentGraph);
		putEdges(vertices, parentGraph);
		
		this.label = label;
		id = metaCnt ++;
		
		parentGraph.vertices.put(toString(), this);
	}
	
	private void putVertices(List<Vertex> vertices, Graph parentGraph){
		subGraph.getVertexFromString(ROOT);
		subGraph.getVertexFromString(SINK);
		for(Vertex vertex : vertices){
			subGraph.vertices.put(vertex.toString(), vertex);
			parentGraph.vertices.remove(vertex.toString());
		}
	}
		
	private void putEdges(List<Vertex> vertices, Graph parentGraph){
		for(Vertex vertex: vertices){
			for(ExpressionEdgeImpl edge: vertex.getOutgoingEdges()){
				Vertex destinationVertex = edge.getDestinationVertex();
				if(!subGraph.vertices.containsKey(destinationVertex.toString())){
					edge.setDestinationVertex(subGraph.getVertexFromString(SINK));
					ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(edge.edgeName, this, destinationVertex, edge.dataType);
					parentGraph.edges.add(newEdge);
				}
				subGraph.edges.add(edge);
				parentGraph.edges.remove(edge);
			}
			/* Don't add the intermediary edges twice */
			for(ExpressionEdgeImpl edge: vertex.getIncomingEdges()){
				Vertex sourceVertex = edge.getSourceVertex();
				if(!subGraph.vertices.containsKey(sourceVertex.toString())){
					edge.setSourceVertex(subGraph.getVertexFromString(ROOT));
					ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(edge.edgeName, sourceVertex, this, edge.dataType);
					parentGraph.edges.add(newEdge);
					subGraph.edges.add(edge);
					parentGraph.edges.remove(edge);
				}
			}
		}
	}
	
	@Override
	public String toString(){
		return "meta: "+ label +", " + "id: " + id;
	}
	
	
	public String getSubGraph(){
		return subGraph.toString();
	}
	
}
