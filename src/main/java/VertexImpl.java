package main;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Vertex implementation used for all non table nodes/
 */
public class VertexImpl extends Vertex {

	//private String vertexName;
	//private int id;
	//private static int cnt = 0;

	public VertexImpl() {
		this("Vertex");
	}

    /**
     * Creates a new vertex with the name in the parameter supported by the unique id.
     * @param vertexName Name of the vertex
     */
	public VertexImpl(String vertexName) {
		super(vertexName);
	}

	public VertexImpl(String vertexName, int id){
		super(vertexName, id);
	}
	
	public VertexImpl(String vertexName, String inputCardinality, String outputCardinality){
		super(vertexName);
		this.inputCardinality = inputCardinality;
		this.outputCardinality = outputCardinality;
	}


	@Override
	public String toString() {
		return vertexName + ", id: " + id;
	}
	
	public static VertexImpl fromString(String vertexString) {
		Pattern vertexPattern = Pattern.compile("(.*)"+", id: "+"(.*)");
		Matcher vertexMatcher = vertexPattern.matcher(vertexString);
		if(vertexMatcher.find()){
			String vertexName = vertexMatcher.group(1);
			int id = Integer.parseInt(vertexMatcher.group(2));
			return new VertexImpl(vertexName, id);
		} else {
			throw new IllegalArgumentException("Cannot parse VertexImpl from String: " + vertexString);
		}
	}
}
