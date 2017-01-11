package test;

import java.io.FileNotFoundException;

import main.Graph;
import main.HuScheduler;
import main.ListScheduler;
import main.Vertex;
import main.VertexImpl;

public class SchedulerTester {

	public static void huTest(){
		Graph graph = new Graph();
		Vertex[] vertices = new Vertex[12];
		
		for(int i = 0; i < 12; i++){
			vertices[i] = new VertexImpl("v"+i);
			graph.putVertex(vertices[i]);
		}
		
		graph.putEdge("", vertices[0], vertices[2], "");
		graph.putEdge("", vertices[1], vertices[2], "");
		graph.putEdge("", vertices[2], vertices[3], "");
		graph.putEdge("", vertices[3], vertices[4], "");
		graph.putEdge("", vertices[4], vertices[11], "");
		graph.putEdge("", vertices[5], vertices[6], "");
		graph.putEdge("", vertices[6], vertices[4], "");
		graph.putEdge("", vertices[7], vertices[8], "");
		graph.putEdge("", vertices[8], vertices[11], "");
		graph.putEdge("", vertices[9], vertices[10], "");
		graph.putEdge("", vertices[10], vertices[11], "");
		
		HuScheduler scheduler = new HuScheduler(graph);
		System.out.println(scheduler.schedule());
	}
	
	
	public static void main(String[] args) throws FileNotFoundException{
		huTest();
	}
}
