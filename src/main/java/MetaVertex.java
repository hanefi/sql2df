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
	private static int metaCnt = 0;
	
	
	public Vertex rootVertex;
	public Vertex sinkVertex;
	protected Graph parentGraph; //Exists if we ever want to collapse a graph
	protected Graph subGraph;
	
	//private String vertexName;
	private int id;
	
	public MetaVertex(String vertexName){
		this.vertexName = vertexName;
		id = metaCnt ++;
		subGraph = new Graph();
		
		rootVertex = new VertexImpl(ROOT);
		sinkVertex = new VertexImpl(SINK);
		putVertex(rootVertex);
		putVertex(sinkVertex);
	}
	
	public MetaVertex(List<Vertex> vertices, Graph parentGraph, String vertexName){
		
		MetaVertex.canCreateMetaNode(vertices, parentGraph);
		
		this.parentGraph = parentGraph;
		subGraph = new Graph();
		putVertices(vertices, parentGraph);
		putEdges(vertices, parentGraph);
		
		this.vertexName = vertexName;
		id = metaCnt ++;
		
		parentGraph.vertices.put(toString(), this);
	}
	
	public void changeRoot(Vertex root){
		this.subGraph.vertices.remove(this.rootVertex.toString());
		this.rootVertex = root;
		subGraph.putVertex(root);
	}
	
	private void putVertices(List<Vertex> vertices, Graph parentGraph){
		rootVertex = new TableVertex(ROOT);
		sinkVertex = new TableVertex(SINK);
		putVertex(rootVertex);
		putVertex(sinkVertex);
		
		for(Vertex vertex : vertices){
			subGraph.vertices.put(vertex.toString(), vertex);
			parentGraph.vertices.remove(vertex.toString());
		}
	}
		
	private void putEdges(List<Vertex> vertices, Graph parentGraph){
		for(Vertex vertex: vertices){
			
			List<ExpressionEdgeImpl> outgoingEdges = new LinkedList<>(vertex.getOutgoingEdges());
			for(ExpressionEdgeImpl edge: outgoingEdges){
				Vertex destinationVertex = edge.getDestinationVertex();
				if(!subGraph.vertices.containsKey(destinationVertex.toString())){
					ExpressionEdgeImpl newSubgraphEdge = ExpressionEdgeImpl.createEdge(edge.edgeName, vertex, sinkVertex, edge.dataType);
					ExpressionEdgeImpl newParentEdge = ExpressionEdgeImpl.createEdge(edge.edgeName, this, destinationVertex, edge.dataType);
					subGraph.edges.add(newSubgraphEdge);
					parentGraph.edges.add(newParentEdge);
					parentGraph.removeEdge(edge);
				}
				else{ //TEST THIS
					parentGraph.removeEdge(edge);
					subGraph.edges.add(edge);
				}
			}
			/* Don't add the intermediary edges twice */
			List<ExpressionEdgeImpl> incomingEdges = new LinkedList<>(vertex.getIncomingEdges());
			for(ExpressionEdgeImpl edge: incomingEdges){
				Vertex sourceVertex = edge.getSourceVertex();
				if(!subGraph.vertices.containsKey(sourceVertex.toString())){
					ExpressionEdgeImpl newSubgraphEdge = ExpressionEdgeImpl.createEdge(edge.edgeName, rootVertex, vertex, edge.dataType);
					ExpressionEdgeImpl newParentEdge = ExpressionEdgeImpl.createEdge(edge.edgeName, sourceVertex, this, edge.dataType);
					subGraph.edges.add(newSubgraphEdge);
					parentGraph.edges.add(newParentEdge);
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
		return "meta: "+ vertexName +", " + "id: " + id;
	}
	
	public void mergeWithParent(){
		if(parentGraph == null){
			throw new IllegalStateException("Cannot merge a metanode for the second time. Parent Graph is null");
		}
		
		Vertex inputsVertex = rootVertex;
		List<ExpressionEdgeImpl> inputEdges = inputsVertex.getOutgoingEdges();

		Vertex outputsVertex = sinkVertex;
		List<ExpressionEdgeImpl> outputEdges = outputsVertex.getIncomingEdges();
		
		
		for(int i = inputEdges.size()-1; i >= 0; i--){
			Boolean isRemoved = false;
			for(int j = incomingEdges.size()-1; j >= 0; j--){
				ExpressionEdgeImpl inputEdge = inputEdges.get(i);
				ExpressionEdgeImpl incomingEdge = incomingEdges.get(j);
				Boolean nameEqualsCondition = (inputEdge.edgeName.equals("*") || inputEdge.edgeName.equals(incomingEdge.edgeName) 
								|| incomingEdge.edgeName.equals("*"));
				if(nameEqualsCondition){
					ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(incomingEdge.edgeName, 
							incomingEdge.getSourceVertex(), inputEdge.getDestinationVertex(), incomingEdge.dataType);
					if(inputEdge.getDestinationVertex() != sinkVertex){
						parentGraph.edges.add(newEdge);
					}
					//isRemoved = true;
				}
			}
			if(isRemoved){
				//inputEdges.get(i).getDestinationVertex().removeIncomingEdge(inputEdges.get(i));
				//subGraph.removeEdge(inputEdges.get(i));
			}
		}

		for(int j = incomingEdges.size()-1; j >= 0; j--){
			parentGraph.removeEdge(incomingEdges.get(j));
		}
		for(int j = inputEdges.size()-1; j >= 0; j--){
			subGraph.removeEdge(inputEdges.get(j));
		}
		
		for(int i = outputEdges.size()-1 ; i >= 0; i--){
			Boolean isRemoved = false;
			for(int j = outgoingEdges.size()-1; j >= 0 ; j--){
				ExpressionEdgeImpl outputEdge = outputEdges.get(i);
				ExpressionEdgeImpl outgoingEdge = outgoingEdges.get(j);
				Boolean nameEqualsCondition = (outputEdge.edgeName.equals("*") || outputEdge.edgeName.equals(outgoingEdge.edgeName) 
								|| outgoingEdge.edgeName.equals("*"));
				if(nameEqualsCondition){
					ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(outgoingEdge.edgeName, 
							outputEdge.getSourceVertex(), outgoingEdge.getDestinationVertex(), outgoingEdge.dataType);
					parentGraph.edges.add(newEdge);
					//isRemoved = true;
				}
			}
			if(isRemoved){
				//outputEdges.get(i).getSourceVertex().removeOutgoingEdge(outputEdges.get(i));
				//subGraph.removeEdge(outputEdges.get(i));
			}

		}
		

		for(int j = outgoingEdges.size()-1; j >= 0; j--){
			parentGraph.removeEdge(outgoingEdges.get(j));
		}
		for(int j = outputEdges.size()-1; j >= 0; j--){
			subGraph.removeEdge(outputEdges.get(j));
		}
				
		//for(int j = sinkVertex.getIncomingEdges().size()-1; j >=0 ; j--){
		//	subGraph.removeEdge(sinkVertex.getIncomingEdges().get(j));
		//}
		
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
		List<ExpressionEdgeImpl> inputEdges = inputsVertex.getOutgoingEdges();
		for(int i = inputEdges.size()-1; i >= 0; i--){
			ExpressionEdgeImpl inputEdge = inputEdges.get(i);
			Boolean matches = false;
			for(int j = incomingEdges.size()-1; j >= 0; j--){
				ExpressionEdgeImpl incomingEdge = incomingEdges.get(j);
				Boolean nameEqualsCondition = (inputEdge.edgeName.equals("*") || inputEdge.edgeName.equals(incomingEdge.edgeName) 
						|| incomingEdge.edgeName.equals("*")) ;
				if(nameEqualsCondition){
					matches = true;
					break;
				}
			}
			if(!matches){
				//System.out.println("Incoming Connection Found "+inputEdge);
				ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(inputEdge.edgeName, 
						vertex, this, inputEdge.dataType);
				//System.out.println(newEdge);
				parentGraph.edges.add(newEdge);
				//System.out.println(parentGraph);
			}
		}
	}
	
	
	public void recursiveMerge(){
		collapseChildren();
		mergeWithParent();
	}
	
	public void collapseChildren(){
		Collection<Vertex> childVertices = subGraph.vertices.values();
		Boolean isModified = true;
		while(isModified){
			isModified = false;
			Iterator<Vertex> iter = childVertices.iterator();
			while(iter.hasNext()){
				Vertex v = iter.next();
				if(v instanceof MetaVertex){
					System.out.println(this+" "+v);
					((MetaVertex)v).recursiveMerge();
					isModified = true;
					break;
				}
			}
		}
	}
	
	public void putVertex(Vertex vertex){
		subGraph.vertices.put(vertex.toString(), vertex);
	}
	
	public void putEdge(String edgeName, Vertex sourceVertex, Vertex destinationVertex, String dataType){		
		//System.out.println("DEBUG EDGE "+ vertexName + " " + sourceVertex + " " + destinationVertex + " " +dataType);

		Boolean isSourceInMetaNode = subGraph.vertices.containsKey(sourceVertex.toString());
		Boolean isDestinationInMetaNode = subGraph.vertices.containsKey(destinationVertex.toString());
		if(isSourceInMetaNode && isDestinationInMetaNode){
			if(edgeName.equals(""))
				edgeName = "Unnamed_" + ExpressionEdgeImpl.getNextEdgeID();
			ExpressionEdgeImpl insideEdge = ExpressionEdgeImpl.createEdge(edgeName, sourceVertex, destinationVertex, dataType);
			subGraph.edges.add(insideEdge);
			return;
		}
		else{
			System.out.println("Edge doesn't belong here!");
		}
	}
	
	public void edgifySources(){
		List<String> toRemove = new LinkedList<>();
		for(String s : subGraph.vertices.keySet()){
			Vertex v = subGraph.vertices.get(s);
			if( v != rootVertex && v.getIncomingEdges().isEmpty()){
				for(ExpressionEdgeImpl e : v.getOutgoingEdges()){
					String edgeName = v.vertexName;
					if(!e.edgeName.contains("Unnamed"))
						edgeName = e.edgeName;
					putEdge(edgeName, rootVertex, e.getDestinationVertex(), e.dataType);
					subGraph.removeEdge(e);
				}
				toRemove.add(s);
			}
		}
		for(String s : toRemove)
			subGraph.vertices.remove(s);
			
	}
	
	public String getSubGraph(){
		return subGraph.toString();
	}
}

