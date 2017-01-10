package main;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class MetaVertex extends Vertex {
	
	public static final String ROOT = "INPUTS";
	public static final String SINK = "OUTPUTS";	
	
	public Vertex rootVertex;
	public Vertex sinkVertex;
	protected Graph parentGraph; //Exists if we ever want to collapse a graph
	protected Graph subGraph;
	
	//private String vertexName;
	//private int id;
	
	public MetaVertex(String vertexName){
		super(vertexName);
		subGraph = new Graph();
		
		rootVertex = new VertexImpl(ROOT);
		sinkVertex = new VertexImpl(SINK);
		subGraph.putVertex(rootVertex);
		subGraph.putVertex(sinkVertex);
	}
	
	public MetaVertex(List<Vertex> vertices, Graph parentGraph, String vertexName){
		this(vertexName);
		MetaVertex.canCreateMetaNode(vertices, parentGraph);
		
		this.parentGraph = parentGraph;
		putVertices(vertices, parentGraph);
		putEdges(vertices, parentGraph);
		
		parentGraph.putVertex(this);
	}
	
	/*
	public void changeRoot(Vertex root){
		this.subGraph.vertices.remove(this.rootVertex.toString());
		this.rootVertex = root;
		subGraph.putVertex(root);
	}
	*/
	
	private void putVertices(List<Vertex> vertices, Graph parentGraph){
		for(Vertex vertex : vertices){
			subGraph.putVertex(vertex);
			parentGraph.vertices.remove(vertex.toString());
		}
	}
		
	private void putEdges(List<Vertex> vertices, Graph parentGraph){
		for(Vertex vertex: vertices){
			
			List<EdgeImpl> outgoingEdges = new LinkedList<>(vertex.getOutgoingEdges());
			for(EdgeImpl edge: outgoingEdges){
				Vertex destinationVertex = edge.getDestinationVertex();
				/* Handle outgoing edges from meta-vertex. */
				if(!subGraph.vertices.containsKey(destinationVertex.toString())){
					subGraph.putEdge(edge.edgeName, vertex, sinkVertex, edge.dataType);
					parentGraph.putEdge(edge.edgeName, this, destinationVertex, edge.dataType);
					parentGraph.removeEdge(edge);
				}
				/* Intermediary Edges */
				else{ 
					parentGraph.removeEdge(edge);
					subGraph.edges.add(edge);
				}
			}
			/* Handle incoming edges to meta-vertex. */
			List<EdgeImpl> incomingEdges = new LinkedList<>(vertex.getIncomingEdges());
			for(EdgeImpl edge: incomingEdges){
				Vertex sourceVertex = edge.getSourceVertex();
				if(!subGraph.vertices.containsKey(sourceVertex.toString())){
					subGraph.putEdge(edge.edgeName, rootVertex, vertex, edge.dataType);
					parentGraph.putEdge(edge.edgeName, sourceVertex, this, edge.dataType);
					parentGraph.removeEdge(edge);
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
			for(EdgeImpl e : v.getOutgoingEdges()){
				Vertex destVertex = e.getDestinationVertex();
				int currentIndegree = indegreeMap.get(destVertex) - 1;
				indegreeMap.put(destVertex, currentIndegree);
				if(currentIndegree == 0){
					dagQueue.add(destVertex);
				}
			}
		}
		
		for(Vertex v : vertices){
			for(EdgeImpl e : v.getIncomingEdges()){
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
		return "meta: "+ vertexName +", " + "id: " + id;
	}
	
	public void mergeWithParent(){
		if(parentGraph == null){
			throw new IllegalStateException("Cannot merge a metanode for the second time. Parent Graph is null");
		}
		
		Vertex inputsVertex = rootVertex;
		List<EdgeImpl> inputEdges = inputsVertex.getOutgoingEdges();

		Vertex outputsVertex = sinkVertex;
		List<EdgeImpl> outputEdges = outputsVertex.getIncomingEdges();
		
		
		for(int i = inputEdges.size()-1; i >= 0; i--){
			for(int j = incomingEdges.size()-1; j >= 0; j--){
				EdgeImpl inputEdge = inputEdges.get(i);
				EdgeImpl incomingEdge = incomingEdges.get(j);
				if(inputEdge.edgeName.equals(incomingEdge.edgeName)){
					EdgeImpl newEdge = new EdgeImpl(incomingEdge.edgeName, 
							incomingEdge.getSourceVertex(), inputEdge.getDestinationVertex(), incomingEdge.dataType);
					if(inputEdge.getDestinationVertex() != sinkVertex){
						parentGraph.edges.add(newEdge);
					}
					else{
						subGraph.edges.add(newEdge);
					}
				}
			}
		}

		for(int j = incomingEdges.size()-1; j >= 0; j--){
			parentGraph.removeEdge(incomingEdges.get(j));
		}
		for(int j = inputEdges.size()-1; j >= 0; j--){
			subGraph.removeEdge(inputEdges.get(j));
		}
		
		for(int i = outputEdges.size()-1 ; i >= 0; i--){
			for(int j = outgoingEdges.size()-1; j >= 0 ; j--){
				EdgeImpl outputEdge = outputEdges.get(i);
				EdgeImpl outgoingEdge = outgoingEdges.get(j);
				if(outputEdge.edgeName.equals(outgoingEdge.edgeName)){
					parentGraph.putEdge(outgoingEdge.edgeName, 
							outputEdge.getSourceVertex(), outgoingEdge.getDestinationVertex(), outgoingEdge.dataType);
				}
			}

		}
		

		for(int j = outgoingEdges.size()-1; j >= 0; j--){
			parentGraph.removeEdge(outgoingEdges.get(j));
		}
		for(int j = outputEdges.size()-1; j >= 0; j--){
			subGraph.removeEdge(outputEdges.get(j));
		}
		
		subGraph.vertices.remove(rootVertex.toString());
		subGraph.vertices.remove(sinkVertex.toString());
		
		parentGraph.vertices.remove(this.toString());
		parentGraph.vertices.putAll(subGraph.vertices);
		parentGraph.edges.addAll(subGraph.edges);
		
		parentGraph = null;
	}
	
	public void createIncomingConnections(Vertex vertex){
		System.out.println("Creating Incoming Connections");
		Vertex inputsVertex = rootVertex;
		List<EdgeImpl> inputEdges = inputsVertex.getOutgoingEdges();
		for(int i = inputEdges.size()-1; i >= 0; i--){
			EdgeImpl inputEdge = inputEdges.get(i);
			Boolean matches = false;
			for(int j = incomingEdges.size()-1; j >= 0; j--){
				EdgeImpl incomingEdge = incomingEdges.get(j);
				if(inputEdge.edgeName.equals(incomingEdge.edgeName)){
					matches = true;
					break;
				}
			}
			if(!matches){
				//System.out.println("Incoming Connection Found "+inputEdge);
				parentGraph.putEdge(inputEdge.edgeName, 
						vertex, this, inputEdge.dataType);
				//System.out.println(newEdge);
				//System.out.println(parentGraph);
			}
		}
	}
	
	public void edgifySources(){
		List<String> toRemove = new LinkedList<>();
		for(String s : subGraph.vertices.keySet()){
			Vertex v = subGraph.vertices.get(s);
			if( v != rootVertex && v.getIncomingEdges().isEmpty()){
				for(EdgeImpl e : v.getOutgoingEdges()){
					String edgeName = v.vertexName;
					if(!e.edgeName.contains("Unnamed"))
						edgeName = e.edgeName;
					subGraph.putEdge(edgeName, rootVertex, e.getDestinationVertex(), e.dataType);
					subGraph.removeEdge(e);
				}
				toRemove.add(s);
			}
		}
		for(String s : toRemove)
			subGraph.vertices.remove(s);
			
	}
	
	public String printSubGraph(){
		return subGraph.toString();
	}
}

