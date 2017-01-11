package test;

import java.util.List;

import main.App;
import main.ListScheduler;
import main.MetaVertex;
import main.Vertex;

public class AppTester {
	
	public static void test1() throws Exception{
		String param1 = "res/create_table.sql";
		String param2 = "res/q1.sql";
		String[] args = {param1, param2};
		App.main(args);
		
		App.graph.collapseAllMetaVertices();
		String graphString1 = App.graph.toString();
	
		ListScheduler scheduler = new ListScheduler(App.graph);
		List<List<Vertex>> schedule = scheduler.schedule();

		for(int i = schedule.size()-1; i>=0; i--){
			List<Vertex> group = schedule.get(i);
			new MetaVertex(group, App.graph, "META_INSTR");
		}
		
		App.graph.collapseAllMetaVertices();
		String graphString2 = App.graph.toString();
		
		System.out.println(graphString1);
		System.out.println(graphString2);
	}
		

	public static void test2() throws Exception{
		String param1 = "res/create_table.sql";
		String param2 = "res/q5.sql";
		String[] args = {param1, param2};
		App.main(args);
		
		App.graph.collapseAllMetaVertices();
		
		ListScheduler scheduler = new ListScheduler(App.graph);
		List<List<Vertex>> schedule = scheduler.schedule();
		
		for(int i = schedule.size()-1; i>=0; i--){
			List<Vertex> group = schedule.get(i);
			new MetaVertex(group, App.graph, "META_INSTR");
		}
		
		App.graph.collapseAllMetaVertices();
		String graphString2 = App.graph.toString();
		
		
		//System.out.println(schedule);
		System.out.println(App.graph.printWithSchedule(schedule));
	}
	
	public static void testReport() throws Exception{
		String param1 = "res/create_table.sql";
		String param2 = "res/demoq1.sql";
		String[] args = {param1, param2};
		App.main(args);
		
		//ListScheduler scheduler = new ListScheduler(App.graph);
		//List<List<Vertex>> schedule = scheduler.schedule();
		//System.out.println(schedule);
		//System.out.println(App.graph.printAndCollapse());
	}
	
	public static void main(String[] args) throws Exception{
		test1();
		//test2();
		//testReport();
	}
	
	
}
