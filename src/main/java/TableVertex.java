package main;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Vertex implementation for representation of the tables.
 * Instances of this class should be created with the provided static method: getTableVertex
 */
public class TableVertex extends Vertex {
	
	//private String tableName;
	public static Map<String, TableVertex> tableVertexMap = new HashMap<>();
	

	public TableVertex(String tableName) {
		this.vertexName = tableName;
	}
	
	public TableVertex(String tableName, int id) {
		this(tableName);
		this.id = id;
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
	
	public static TableVertex fromString(String vertexString) {
		Pattern vertexPattern = Pattern.compile("table: "+"(.*)");
		Matcher vertexMatcher = vertexPattern.matcher(vertexString);
		if(vertexMatcher.find()){
			String vertexName = vertexMatcher.group(1);
			return getTableVertex(vertexName);
		} else {
			throw new IllegalArgumentException("Cannot parse TableVertex from String: " + vertexString);
		}
	}
}
