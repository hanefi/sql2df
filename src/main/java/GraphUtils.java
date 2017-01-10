package main;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GraphUtils{
    public Graph graph;
    
    public GraphUtils(Graph graph){
    	this.graph = graph;
    }
      
    private void cloneAndAppend(List<List<Vertex>> destinationList, List<List<Vertex>> sourceList, Vertex element){
    	for(List<Vertex> path : sourceList){
    		List<Vertex> cloneList = new LinkedList<Vertex>(path);
    		cloneList.add(0, element);
    		destinationList.add(cloneList);
    	}
    }

    private List<List<Vertex>> getPathsWithColumn(String columnLabel, Vertex startVertex, Map<Vertex, List<List<Vertex>>> visited){
    	List<List<Vertex>> paths = new LinkedList<>();
    	visited.put(startVertex, paths);
    	for(EdgeImpl edge: startVertex.getOutgoingEdges()){
    		if(edge.edgeName.equals(columnLabel)){
    			Vertex destination = edge.getDestinationVertex();
    			if(destination == startVertex)
    				continue;
    			else if(!visited.containsKey(destination))
    				 getPathsWithColumn(columnLabel, edge.getDestinationVertex(), visited);
    			cloneAndAppend(paths, visited.get(destination), startVertex);
    		}
    	}
    	if(paths.size() == 0){
    		List<Vertex> singularList = new LinkedList<>();
    		singularList.add(startVertex);
    		paths.add(singularList);
    	}
    	return paths;
    }

    /**
     * Prints all paths of a given table column.
     * @param tableName Name of the table.
     * @param columnLabel Name of the column.
     */
    public void printPaths(String tableName, String columnLabel){
    	Vertex startVertex = TableVertex.getTableVertex(tableName);
    	List<List<Vertex>> paths = 
    			getPathsWithColumn(columnLabel, startVertex, new HashMap<Vertex, List<List<Vertex>>>());
    	for(List<Vertex> path : paths){
    		System.out.println(path);
    	}
    	
    }
}
