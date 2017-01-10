package main;

/**
 * A Vertex implementation used for all non table nodes/
 */
public class VertexImpl extends Vertex {
	
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
}
