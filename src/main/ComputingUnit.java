package main;

import java.util.List;

/**
 * A class to describe computing resources available to a scheduler.
 */
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
	
	/** 
	 * Finds the coefficient in front of N.
	 * @return The coefficient in front of N.
	 */
	public int getLatencyCoefficient(){
		String[] prefix = latency.split("[a-zA-Z]");
		try{
			return Integer.parseInt(prefix[0]);
		} catch(Exception e) {
			return 1;
		}
	}
	
	/**
	 * Finds the degree of the polynomial.
	 * It is implemented simply at this stage, since it is assumed to be 0 or 1.
	 * @return Power of N.
	 */
	public int getDegree(){
		try{
			Integer.parseInt(latency);
			return 0;
		} catch(Exception e) {
			return 1;
		}
	}
	
	
	/**
	 * Whether this computing unit can process the given vertex.
	 * @param v The vertex to be processed.
	 * @return Whether this computing unit can process the vertex.
	 */
	public boolean canProcessVertex(Vertex v){
		return operations.contains(v.vertexName);
	}
	
	public String toString(){
		return "UNIT:" + name + " " + " AMOUNT: "+ availableAmount + " LATENCY: " + latency 
				+ " OPERATIONS: " + operations;
	}
	
}
