package main;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Implementation of Hu's Scheduling algorithm.
 * @author kaan
 */
public class HuScheduler implements Scheduler{
	
	public static final int LATENCY = 1;
	
	Graph graph;
	
	public HuScheduler(Graph graph){
		this.graph = graph;
	}
	
	/**
	 * Schedules according to Hu's algorithm.
	 * @return The schedule.
	 */
	public List<List<Vertex>> schedule(){
		Map<Vertex, Integer> labels = computeDAG();
		int alpha = getMaximumLabel(labels);
		List<List<Vertex>> p = findLabelVertices(labels, alpha);
		
		int resources = findMinResources(alpha, p);
		System.out.println("MIN RESOURCES: " + resources);
		
		List<List<Vertex>> schedule = new LinkedList<List<Vertex>>();
		Set<Vertex> visited = new HashSet<>();
		
		while(visited.size() < graph.vertices.size()){
			List<Vertex> instruction = new LinkedList<>();
			for(int i = alpha; instruction.size() < resources && i >= 0; i--){
				for(Vertex v : p.get(i)){
					if(!visited.contains(v)){
						boolean neighborsVisited = true;
						for(EdgeImpl e : v.getIncomingEdges())
							if(!visited.contains(e.getSourceVertex()))
								neighborsVisited = false;
						if(neighborsVisited){
							instruction.add(v);
							if(instruction.size() == resources)
								break;
						}
					}		
				}
			}
			schedule.add(instruction);
			for(Vertex v : instruction)
				visited.add(v);
		}
		return schedule;
		
	}
	
	/**
	 * Finds the minimum number of resources needed to achieve minimal latency.
	 * Uses Hu's theorem
	 * @param alpha Maximum label among vertices.
	 * @param p The vertices, grouped according to their labels.
	 * @return The lower bound on resources needed.
	 */
	public int findMinResources(int alpha, List<List<Vertex>> p){
		int maxGamma = -1;
		int maxValue = Integer.MIN_VALUE;
		
		for(int gamma = 1; gamma <= alpha; gamma ++){
			double value = 0;
			for(int j = 1; j <= gamma; j++)
				value += p.get(alpha + 1 - j).size();
			value /= gamma + LATENCY - alpha;
			
			int intValue = (int) Math.ceil(value);
			if(intValue > maxValue){
				maxValue = intValue;
				maxGamma = gamma;
			}
		}
		
		return maxGamma;
	}
	
	public int getMaximumLabel(Map<Vertex, Integer> labels){
		int maxLabel = 0;
		
		for(int label : labels.values())
			maxLabel = Math.max(maxLabel, label);
		
		return maxLabel;
	}
	
	public List<List<Vertex>> findLabelVertices(Map<Vertex, Integer> labels, int maxLabel){
		List<List<Vertex>> labelVertices = new ArrayList<List<Vertex>>();
		
		for(int i = 0; i <= maxLabel; i++)
			labelVertices.add(new ArrayList<Vertex>());
		
		for(Vertex v : labels.keySet())
			labelVertices.get(labels.get(v)).add(v);
		
		return labelVertices;
	}
	
	
	/**
	 * Performs DAG in reverse order.
	 * @return The label of each vertex
	 */
	public Map<Vertex, Integer> computeDAG(){
		Queue<Vertex> DAGQueue = new LinkedList<>();
		Map<Vertex, Integer> indegrees = computeIndegrees();
		Map<Vertex, Integer> labels = new HashMap<>();
		
		for(Vertex v: indegrees.keySet())
			if(indegrees.get(v) == 0)
				DAGQueue.add(v);
		
		while(DAGQueue.size() > 0){
			Vertex v = DAGQueue.remove();
			
			int label = 0;
			for(EdgeImpl e : v.getOutgoingEdges())
				label = Math.max(label, labels.get(e.getDestinationVertex()) + 1);
			
			labels.put(v, label);
			
			for(EdgeImpl e : v.getIncomingEdges()){
				Vertex neighbor = e.getSourceVertex();
				int newIndegree = indegrees.get(neighbor) - 1;
				indegrees.put(neighbor, newIndegree);
				if(newIndegree == 0)
					DAGQueue.add(neighbor);
			}
		}
		return labels;
	}
	
	/**
	 * Computes indegrees of each vertex.
	 * @return Indegrees of each vertex.
	 */
	private Map<Vertex, Integer> computeIndegrees(){
		Map<Vertex, Integer> indegrees = new HashMap<>();
		
		for(Vertex v : graph.vertices.values()){
			indegrees.put(v, v.getOutgoingEdges().size());
		}
	
		return indegrees;
	}
}
