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
	
	private String label;
	private int id;
	
	public MetaVertex(String label){
		this.label = label;
		id = metaCnt ++;
		subGraph = new Graph();
		
		rootVertex = new VertexImpl(ROOT);
		sinkVertex = new VertexImpl(SINK);
		putVertex(rootVertex);
		putVertex(sinkVertex);
	}
	
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
			for(ExpressionEdgeImpl edge: vertex.getOutgoingEdges()){
				Vertex destinationVertex = edge.getDestinationVertex();
				if(!subGraph.vertices.containsKey(destinationVertex.toString())){
					edge.setDestinationVertex(sinkVertex);
					ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(edge.edgeName, this, destinationVertex, edge.dataType, edge.id);
					parentGraph.edges.add(newEdge);
				}
				subGraph.edges.add(edge);
				parentGraph.edges.remove(edge);
			}
			/* Don't add the intermediary edges twice */
			for(ExpressionEdgeImpl edge: vertex.getIncomingEdges()){
				Vertex sourceVertex = edge.getSourceVertex();
				if(!subGraph.vertices.containsKey(sourceVertex.toString())){
					edge.setSourceVertex(rootVertex);
					ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(edge.edgeName, sourceVertex, this, edge.dataType, edge.id);
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
		
		Vertex inputsVertex = rootVertex;
		List<ExpressionEdgeImpl> inputEdges = inputsVertex.getOutgoingEdges();

		Vertex outputsVertex = sinkVertex;
		List<ExpressionEdgeImpl> outputEdges = outputsVertex.getIncomingEdges();
		
		
		for(int i = inputEdges.size()-1; i >= 0; i--){
			Boolean isRemoved = false;
			for(int j = incomingEdges.size()-1; j >= 0; j--){
				ExpressionEdgeImpl inputEdge = inputEdges.get(i);
				ExpressionEdgeImpl incomingEdge = incomingEdges.get(j);
				Boolean nameEqualsCondition = inputEdge.edgeName.length()>0 && incomingEdge.edgeName.length()>0 
						&& (inputEdge.edgeName.equals("*") || inputEdge.edgeName.equals(incomingEdge.edgeName) 
								|| incomingEdge.edgeName.equals("*"));
				Boolean idEqualsCondition = inputEdge.id == incomingEdge.id;
				if(nameEqualsCondition || idEqualsCondition){
					ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(incomingEdge.edgeName, 
							incomingEdge.getSourceVertex(), inputEdge.getDestinationVertex(), incomingEdge.dataType);
					if(inputEdge.getDestinationVertex() != sinkVertex){
						parentGraph.edges.add(newEdge);
					}
					isRemoved = true;
				}
			}
			if(isRemoved){
				inputEdges.get(i).getDestinationVertex().removeIncomingEdge(inputEdges.get(i));
				subGraph.edges.remove(inputEdges.get(i));
			}
		}


		for(int i = outputEdges.size()-1 ; i >= 0; i--){
			Boolean isRemoved = false;
			for(int j = outgoingEdges.size()-1; j >= 0 ; j--){
				ExpressionEdgeImpl outputEdge = outputEdges.get(i);
				ExpressionEdgeImpl outgoingEdge = outgoingEdges.get(j);
				Boolean nameEqualsCondition = outputEdge.edgeName.length()>0 && outgoingEdge.edgeName.length()>0 
						&& (outputEdge.edgeName.equals("*") || outputEdge.edgeName.equals(outgoingEdge.edgeName) 
								|| outgoingEdge.edgeName.equals("*"));
				Boolean idEqualsCondition = outputEdge.id == outgoingEdge.id;
				if(nameEqualsCondition || idEqualsCondition){
					ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(outgoingEdge.edgeName, 
							outputEdge.getSourceVertex(), outgoingEdge.getDestinationVertex(), outgoingEdge.dataType);
					parentGraph.edges.add(newEdge);
					isRemoved = true;
				}
			}
			if(isRemoved){
				outputEdges.get(i).getSourceVertex().removeOutgoingEdge(outputEdges.get(i));
				subGraph.edges.remove(outputEdges.get(i));
			}

		}
		
		for(int j = incomingEdges.size()-1; j >= 0; j--){
			parentGraph.removeEdge(incomingEdges.get(j));
		}
		for(int j = outgoingEdges.size()-1; j >= 0; j--){
			parentGraph.removeEdge(outgoingEdges.get(j));
		}

		
		
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
				Boolean nameEqualsCondition = inputEdge.edgeName.length()>0 && incomingEdge.edgeName.length()>0 
						&& (inputEdge.edgeName.equals("*") || inputEdge.edgeName.equals(incomingEdge.edgeName) 
						|| incomingEdge.edgeName.equals("*")) ;
				Boolean idEqualsCondition = inputEdge.id == incomingEdge.id;
				if(nameEqualsCondition || idEqualsCondition){
					matches = true;
					break;
				}
			}
			if(!matches){
				System.out.println("Incoming Connection Found "+inputEdge);
				ExpressionEdgeImpl newEdge = ExpressionEdgeImpl.createEdge(inputEdge.edgeName, 
						vertex, this, inputEdge.dataType, inputEdge.id);
				System.out.println(newEdge);
				parentGraph.edges.add(newEdge);
				System.out.println(parentGraph);
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
	
	public void putEdge(String label, Vertex sourceVertex, Vertex destinationVertex, String dataType){		
		System.out.println("DEBUG EDGE "+ label + " " + sourceVertex + " " + destinationVertex + " " +dataType);

		Boolean isSourceInMetaNode = subGraph.vertices.containsKey(sourceVertex.toString());
		Boolean isDestinationInMetaNode = subGraph.vertices.containsKey(destinationVertex.toString());
		
		int id = ExpressionEdgeImpl.getNextEdgeID();
		
		Vertex root = rootVertex;
		Vertex sink = sinkVertex;
		for(String s : subGraph.vertices.keySet())
			System.out.println(s);
		System.out.println(isSourceInMetaNode + " " + isDestinationInMetaNode);

		if(isSourceInMetaNode && isDestinationInMetaNode){
			ExpressionEdgeImpl insideEdge = ExpressionEdgeImpl.createEdge(label, sourceVertex, destinationVertex, dataType, id);
			subGraph.edges.add(insideEdge);
			return;
		}
		else if(parentGraph == null){
			System.out.println("Parent Is Null");
			return;
		}
		
		Boolean isSourceInParentNode = parentGraph.vertices.containsKey(sourceVertex.toString());
		Boolean isDestinationInParentNode = parentGraph.vertices.containsKey(destinationVertex.toString());
		
		if(isSourceInParentNode && isDestinationInMetaNode){
			ExpressionEdgeImpl insideEdge = ExpressionEdgeImpl.createEdge(label, root, destinationVertex, dataType, id);
			ExpressionEdgeImpl outsideEdge = ExpressionEdgeImpl.createEdge(label, sourceVertex, this, dataType, id);
			subGraph.edges.add(insideEdge);
			parentGraph.edges.add(outsideEdge);
		}
		else if(isSourceInMetaNode && isDestinationInParentNode){
			ExpressionEdgeImpl insideEdge = ExpressionEdgeImpl.createEdge(label, sourceVertex, sink, dataType, id);
			ExpressionEdgeImpl outsideEdge = ExpressionEdgeImpl.createEdge(label, this, destinationVertex, dataType, id);
			subGraph.edges.add(insideEdge);
			parentGraph.edges.add(outsideEdge);
		}
		else{
			System.out.println("This edge doesn't belong here "+ label + " " + sourceVertex + " " + destinationVertex + " " +dataType);
		}
	}
	
	public void edgifySources(){
		List<String> toRemove = new LinkedList<>();
		for(String s : subGraph.vertices.keySet()){
			Vertex v = subGraph.vertices.get(s);
			if( v != rootVertex && v.getIncomingEdges().isEmpty())
				for(ExpressionEdgeImpl e : v.getOutgoingEdges()){
					String edgeName = v.vertexName;
					if(e.edgeName.length() > 0)
						edgeName = e.edgeName;
					putEdge(edgeName, rootVertex, e.getDestinationVertex(), e.dataType);
					subGraph.removeEdge(e);
				}
			toRemove.add(s);
		}
		for(String s : toRemove)
			subGraph.vertices.remove(s);
			
	}
	
	public String getSubGraph(){
		return subGraph.toString();
	}
}

