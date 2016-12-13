package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ListScheduler extends HuScheduler{

	public static final String RESOURCE_CONFIG_PATH = "res/computing-units/scheduler-resources.csv";
	//public static final String[] WHITELISTED = {"CONSTANT", "table"}; 
	private List<ComputingUnit> resources;
	
	private Map<Vertex, Integer> labels;
	private int alpha;
	private List<List<Vertex>> p;
	private Set<Vertex> visited;
	
	
	public ListScheduler(Graph graph) throws FileNotFoundException{
		super(graph);
		readResourcesFromFile();
	}
	
	
	private void readResourcesFromFile() throws FileNotFoundException{
		resources = new ArrayList<>();
		Scanner scanner = new Scanner(new File(RESOURCE_CONFIG_PATH));
        scanner.nextLine(); //Throw away the header line
		while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] tokens = line.split(",");
            String name = tokens[0];
            int availableAmount = Integer.parseInt(tokens[1]);
            String latency = tokens[2]; 
            List<String> operations = Arrays.asList(Arrays.copyOfRange(tokens, 3, tokens.length));
            resources.add(new ComputingUnit(name, availableAmount, latency, operations));
        }
        scanner.close();
       // System.out.println(resources);
	}
	
	public List<List<Vertex>> schedule(){
		labels = computeDAG();
		alpha = getMaximumLabel(labels);
		p = findLabelVertices(labels, alpha);
		visited = new HashSet<>();
		
		List<List<Vertex>> schedule = new LinkedList<List<Vertex>>();
				
		scheduleFirstInstruction(schedule, visited);
		schedule.addAll(schedule(1));
		return schedule;
	}
	
	public List<List<Vertex>> schedule(int depth){
		
		List<List<Vertex>> schedule = new LinkedList<List<Vertex>>();
		
		if(depth < 0)
			return schedule;
		
		Map<Vertex, Integer> inProgress = new HashMap<Vertex, Integer>();
		
		boolean update = true;
		while(update){
			update = false;
			Iterator<Vertex> iter = inProgress.keySet().iterator();
			while(iter.hasNext()){
				Vertex v = iter.next();
				int prevTick = inProgress.get(v);
				int currTick = prevTick - 1;
				if (currTick > 0){
					update = true;
					inProgress.put(v, currTick);
				}
				else {
					visited.add(v);
					iter.remove();
				}
			}
			
			List<Vertex> instruction = new LinkedList<>();
			for(ComputingUnit resource : resources){
				if(resource.getDegree() != depth)
					continue;
				int usedResourceCount = 0;
				for(int i = alpha; usedResourceCount < resource.availableAmount && i >= 0; i--){
					for(Vertex v : p.get(i)){
						if(!inProgress.containsKey(v) && !visited.contains(v) && resource.canParseVertex(v)){
							boolean neighborsVisited = true;
							for(ExpressionEdgeImpl e : v.getIncomingEdges())
								if(!visited.contains(e.getSourceVertex()))
									neighborsVisited = false;
							if(neighborsVisited){
								update = true;
								instruction.add(v);
								inProgress.put(v, resource.getLatencyCoefficient());
								usedResourceCount ++;
								if(usedResourceCount == resource.availableAmount)
									break;
							}
						}		
					}
				}
			}
	
			List<List<Vertex>> parentSchedule = schedule(depth - 1);
			System.out.println("DEP: "+ depth);
			System.out.println(parentSchedule);
			System.out.println("INSTR: " + instruction);
			if(parentSchedule.size() > 0){
				update = true;
				instruction.addAll(parentSchedule.get(0));
				parentSchedule.remove(0);
			}
			
			if(instruction.size() == 0){
				//System.out.println("LOL");
			}
			
			schedule.add(instruction);
			schedule.addAll(parentSchedule);
			
		}
		
		schedule.remove(schedule.size()-1);
		return schedule;
		
	}	
	public void scheduleFirstInstruction(List<List<Vertex>> schedule, Set<Vertex> visited){
		List<Vertex> instruction = new LinkedList<>();
		for(Vertex v : graph.vertices.values())
			if(v.getIncomingEdges().size() == 0){
				instruction.add(v);
				visited.add(v);
			}
		schedule.add(instruction);
	}
}
