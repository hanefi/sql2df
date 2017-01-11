package main;
import java.util.HashMap;
import java.util.Map;

/**
 * A Vertex implementation for representation of the tables.
 * Instances of this class should be created with the provided static method: getTableVertex
 */
public class TableVertex extends Vertex {
	
	//private String tableName;
	public static Map<String, TableVertex> tableVertexMap = new HashMap<>();
	

	public TableVertex(String tableName) {
		super(tableName);
	}
	
	public TableVertex(String tableName, int id) {
		super(tableName, id);
	}

	@Override
	public String toString() {
		return "table: " + vertexName;
	}
	
    /**
     * Returns the vertex for the specified table.
     *
     * If it doesn't exist, creates it first.
     * @param tableName Name of the table
     * @return Vertex for the table
     */
	public static TableVertex getTableVertex(String tableName) {
		if (!tableVertexMap.containsKey(tableName)) {
			tableVertexMap.put(tableName, new TableVertex(tableName));
		}
		
		return tableVertexMap.get(tableName);
	}
}
