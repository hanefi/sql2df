package main;

import java.util.List;

public class ComputingUnit {

	public String name;
	public int availableAmount;
	public String latency;
	public List<String> operations;
	
	public ComputingUnit(String name, int availableAmount, String latency, List<String> operations){
		this.name = name;
		this.availableAmount = availableAmount;
		this.latency = latency;
		this.operations = operations;
	}
	
	public int getLatencyCoefficient(){
		String[] prefix = latency.split("[a-zA-Z]");
		try{
			return Integer.parseInt(prefix[0]);
		} catch(Exception e) {
			return 1;
		}
	}
	
	public int getDegree(){
		try{
			Integer.parseInt(latency);
			return 0;
		} catch(Exception e) {
			return 1;
		}
	}
	
	public boolean canParseVertex(Vertex v){
		return operations.contains(v.vertexName);
	}
	
	public String toString(){
		return "UNIT:" + name + " " + " AMOUNT: "+ availableAmount + " LATENCY: " + latency 
				+ " OPERATIONS: " + operations;
	}
	
}
