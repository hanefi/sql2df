package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import main.App;
import main.Graph;
import main.GraphUtils;
import main.MetaVertex;
import main.Vertex;
import main.VertexImpl;

public class GraphUtilsTester {
	
	public static void test1() throws Exception{        
		String param1 = "res/create_table.sql";
		String param2 = "res/q1.sql";
		String[] args = {param1, param2};
		App.main(args);
		
		GraphUtils utils = new GraphUtils(App.graph); 
        Vertex vertex1 = utils.graph.vertices.get("meta: FILTER_SUBGRAPH, id: 55");
        Vertex vertex2 = utils.graph.vertices.get("GROUP, id: 80");

        utils.printPaths("lineitem", "l_tax");
        List<Vertex> list = new LinkedList<>();
        
        list.add(vertex1);
        list.add(vertex2);
        
        MetaVertex metaVertex = new MetaVertex(list, utils.graph, "META_VERTEX");
        
       // metaVertex.mergeWithParent();
        
        System.out.println(vertex1.getFanIn()+" "+vertex1.getFanOut());
        System.out.println(utils.graph);
        System.out.println(metaVertex.printSubGraph());
	}
	
	
	
	public static void test2() throws IOException{
        Graph graph = new Graph();
        
        Vertex v1 = new VertexImpl("v1");
        Vertex v2 = new VertexImpl("v2");
        Vertex v3 = new VertexImpl("v3");
        Vertex v4 = new VertexImpl("v4");
        
        graph.putVertex(v1);
        graph.putVertex(v2);
        graph.putVertex(v3);
        graph.putVertex(v4);

        graph.putEdge("E", v1, v2, "");
        graph.putEdge("E", v1, v3, "");
        graph.putEdge("E", v2, v3, "");
        graph.putEdge("E", v3, v4, "");

        
        System.out.println("First Graph:");
        System.out.println(graph);
        
        List<Vertex> lv = new ArrayList<>();
        lv.add(v2);
        lv.add(v3);
        MetaVertex mv = new MetaVertex(lv, graph, "META");
        
        System.out.println("Second Graph:");
        System.out.println(graph);
        
        System.out.println("Subgraph:");
        System.out.println(mv.printSubGraph());
        
        //for(ExpressionEdgeImpl e : graph.edges)
        //	System.out.println(e.hashCode());
        
        mv.mergeWithParent();
        
        System.out.println("Third Graph:");
        System.out.println(graph);
	}
	
	public static void main(String[] args) throws Exception{
		test1();
		//test2();
	}
}
