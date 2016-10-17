package main;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphUtils{
    private static String edgePatternFormat = "\\\"(.*)\\\"\\s*->\\s*\\\"(.*)\\\"\\[label=\\\"(.*)\\\"\\]";
    public Graph graph;

    private List<String> readFile(String filename) throws IOException{
        InputStream inputStream = new FileInputStream(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        List<String> lines = new LinkedList<>();;
        String line = "";
        while(true){
            line = bufferedReader.readLine();
            if(line == null)
                break;
            lines.add(line);
        }
        bufferedReader.close();
        return lines;
    }
    /*
        Remove the first and last lines.
     */
    private void filterLines(List<String> lines){
        lines.remove(lines.size()-1);
        lines.remove(0);
    }

    private void parseGraph(List<String> lines){
        graph = new Graph();
        for(String line : lines)
            parseEdge(line);
    }

    private void parseEdge(String line){
        Pattern edgePattern = Pattern.compile(edgePatternFormat);
        Matcher lineMatcher = edgePattern.matcher(line);
        if(lineMatcher.find()){
            Vertex sourceVertex  = graph.getVertexFromString(lineMatcher.group(1));
            Vertex destinationVertex  = graph.getVertexFromString(lineMatcher.group(2));
            ExpressionEdgeImpl edge = ExpressionEdgeImpl.fromString(lineMatcher.group(3), sourceVertex, destinationVertex);
            graph.edges.add(edge);
        }
    }

    public GraphUtils(String filename) throws IOException{
        List<String> lines = readFile(filename);
        filterLines(lines);
        parseGraph(lines);
        System.out.println("File " + filename + " successfully parsed");
    }
    
    //Move from here
    public static void init(){
    	App.readStorageSizes();
    }
    
    private void cloneAndAppend(List<List<Vertex>> destinationList, List<List<Vertex>> sourceList, Vertex element){
    	for(List<Vertex> path : sourceList){
    		List<Vertex> cloneList = new LinkedList<Vertex>(path);
    		cloneList.add(0, element);
    		destinationList.add(cloneList);
    	}
    }
    
    //Doesn't loop if cycles exist, but is not deterministic
    public List<List<Vertex>> getPathsWithColumn(String columnLabel, Vertex startVertex, Map<Vertex, List<List<Vertex>>> visited){
    	List<List<Vertex>> paths = new LinkedList<>();
    	visited.put(startVertex, paths);
    	for(ExpressionEdgeImpl edge: startVertex.getOutgoingEdges()){
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

    public void printPaths(String tableName, String columnLabel){
    	Vertex startVertex = TableVertex.getTableVertex(tableName);
    	List<List<Vertex>> paths = 
    			getPathsWithColumn(columnLabel, startVertex, new HashMap<Vertex, List<List<Vertex>>>());
    	for(List<Vertex> path : paths){
    		System.out.println(path);
    	}
    	
    }
    
    public static void main(String[] args) throws IOException{
    	init();
        GraphUtils utils = new GraphUtils("res/q1.sql.dot");
        //Vertex vertex1 = utils.graph.vertices.get("FILTER, id: 48");
        //Vertex vertex2 = utils.graph.vertices.get("GROUP, id: 49");
        //Vertex vertex3 = utils.graph.vertices.get("SELECT, id: 50");

        //utitestPaths(utils, "lineitem", "l_tax");
        List<Vertex> list = new LinkedList<>();
        
        //list.add(vertex1);
        //list.add(vertex3);
        
        //MetaVertex metaVertex = new MetaVertex(list, utils.graph, "META_VERTEX");
        
       // System.out.println(vertex1.getFanIn()+" "+vertex1.getFanOut());
        System.out.println(utils.graph);
        //System.out.println(metaVertex.getSubGraph());
    }
}
