package main;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A scheduler which partitions the graph into subgraphs of uniform frequency.
 * @author kaan
 *
 */
public class GreedyScheduler implements Scheduler{

	Graph graph;
	
	public GreedyScheduler(Graph graph){
		this.graph = graph;
	}
	
	/**
	 * Partitions the graph into subgraphs of uniform frequency.
	 * @return The partitions.
	 */
	
	public List<List<Vertex>> schedule(){
		Map<Vertex, Integer> inDegrees = getIndegrees();
		Set<Vertex> visited = new HashSet<>();
		List<List<Vertex>> schedule = new LinkedList<>();
		
		while(visited.size() < graph.vertices.size()){
			List<Vertex> instruction = new LinkedList<>();
			Set<Vertex> forbidden = new HashSet<>();
			Queue<Vertex> bfsQueue = new LinkedList<>();
			
			for(Vertex v : graph.vertices.values()){
				if(!visited.contains(v) && inDegrees.get(v) == 0){
					bfsQueue.add(v);
				}
			}
			
			while(bfsQueue.size() > 0){
				Vertex v = bfsQueue.remove();
				instruction.add(v);
				visited.add(v);
				for(EdgeImpl e : v.getOutgoingEdges()){
					Vertex targetVertex = e.getDestinationVertex();
					
					int oldIndegree = inDegrees.get(targetVertex);
					inDegrees.put(targetVertex, oldIndegree-1);
					
					boolean needsBuffer = !v.outputCardinality.equals(targetVertex.inputCardinality);
					if(needsBuffer)
						forbidden.add(targetVertex);
					
					if(!forbidden.contains(targetVertex) && inDegrees.get(targetVertex) == 0)
						bfsQueue.add(targetVertex);
				}
			}
			schedule.add(instruction);
		}
		
		return schedule;
	}
	
	private Map<Vertex, Integer> getIndegrees(){
		Map<Vertex, Integer> inDegrees = new HashMap<>();
		for(Vertex v : graph.vertices.values()){
			inDegrees.put(v, v.getIncomingEdges().size());
		}
		return inDegrees;
	}
	
}
