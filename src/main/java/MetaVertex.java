package main;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class MetaVertex extends Vertex {
	
	public static final String ROOT = "table: INPUTS";
	public static final String SINK = "table: OUTPUTS";
	private static int metaCnt = 0;
	
	private Graph parentGraph; //Exists if we ever want to collapse a graph
	private Graph subGraph;
	
	private String label;
	private int id;
	
	public MetaVertex(List<Vertex> vertices, Graph parentGraph, String label){
		
		MetaVertex.canCreateMetaNode(vertices, parentGraph);
		
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
	
	private static boolean verticesExistInGraph(List<Vertex> vertices, Graph parentGraph){
		for(Vertex v: vertices){
			if(!parentGraph.vertices.values().contains(v))
				throw new IllegalArgumentException("Cannot form meta node. "
						+ "Vertex " + v + "is not in the given graph");			
		}
		return true;
	}
	
	/* Performs DAG analysis on the part before the meta node. 
	 * Then checks if all incoming edges to meta-node are from that part. */
	private static boolean verticesDontCreateLoop(List<Vertex> vertices, Graph parentGraph){
		Map<Vertex, Integer> indegreeMap = new HashMap<>();
		Queue<Vertex> dagQueue = new LinkedList<Vertex>();
		for(Vertex v : parentGraph.vertices.values()){
			indegreeMap.put(v, v.getIncomingEdges().size());
			if(indegreeMap.get(v) == 0)
				dagQueue.add(v);
		}
		
		while(!dagQueue.isEmpty()){
			Vertex v = dagQueue.remove();
			if(vertices.contains(v))
				continue;
			for(ExpressionEdgeImpl e : v.getOutgoingEdges()){
				Vertex destVertex = e.getDestinationVertex();
				int currentIndegree = indegreeMap.get(destVertex) - 1;
				indegreeMap.put(destVertex, currentIndegree);
				if(currentIndegree == 0){
					dagQueue.add(destVertex);
				}
			}
		}
		
		for(Vertex v : vertices){
			for(ExpressionEdgeImpl e : v.getIncomingEdges()){
				Vertex sourceVertex = e.getSourceVertex();
				if(!vertices.contains(sourceVertex) && indegreeMap.get(sourceVertex) > 0)
					throw new IllegalArgumentException("MetaNode not allowed. It creates a loop in parent graph."
							+ "Vertices "+ v + " and " + sourceVertex + " form a loop.");;
			}
		}
		return true;
	}
	
	public static boolean canCreateMetaNode(List<Vertex> vertices, Graph parentGraph){
		return verticesExistInGraph(vertices, parentGraph) &&
				verticesDontCreateLoop(vertices, parentGraph);
	}
	
	@Override
	public String toString(){
		return "meta: "+ label +", " + "id: " + id;
	}
	
	public void mergeWithParent(){
		if(parentGraph == null){
			throw new IllegalStateException("Cannot merge a metanode for the second time. Parent Graph is null");
		}
		
		Vertex inputsVertex = subGraph.getVertexFromString(ROOT);
		List<ExpressionEdgeImpl> inputEdges = inputsVertex.getOutgoingEdges();
		
		for(int i = 0; i < inputEdges.size(); i++)
			for(int j = incomingEdges.size()-1; j >= 0; j--){
				if(inputEdges.get(i).edgeName == incomingEdges.get(j).edgeName){
					incomingEdges.get(j).setDestinationVertex(inputEdges.get(i).getDestinationVertex());
					inputEdges.get(i).getDestinationVertex().removeIncomingEdge(inputEdges.get(i));
					subGraph.edges.remove(inputEdges.get(i));
					break;
				}
			}
		Vertex outputsVertex = subGraph.getVertexFromString(SINK);
		List<ExpressionEdgeImpl> outputEdges = outputsVertex.getIncomingEdges();

		for(int i = 0; i < outputEdges.size(); i++)
			for(int j = outgoingEdges.size()-1; j >= 0 ; j--){
				if(outputEdges.get(i).edgeName == outgoingEdges.get(j).edgeName){
					outgoingEdges.get(j).setSourceVertex(outputEdges.get(i).getSourceVertex());
					outputEdges.get(i).getSourceVertex().removeOutgoingEdge(outputEdges.get(i));
					subGraph.edges.remove(outputEdges.get(i));
					break;
				}
			}
		
		parentGraph.vertices.putAll(subGraph.vertices);
		parentGraph.edges.addAll(subGraph.edges);
		
		parentGraph = null;
	}
	
	
	public String getSubGraph(){
		return subGraph.toString();
	}
	
}
