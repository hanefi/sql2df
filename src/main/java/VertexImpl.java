import java.util.List;

/**
 * A Vertex implementation used for all non table nodes/
 */
public class VertexImpl implements Vertex {

	private String vertexName;
	private int id;
	private List<Edge> outgoingEdges;
	private static int cnt = 0;

	public VertexImpl() {
		this("Vertex");
	}

    /**
     * Creates a new vertex with the name in the parameter supported by the unique id.
     * @param vertexName Name of the vertex
     */
	public VertexImpl(String vertexName) {
		this.vertexName = vertexName;
        this.id = cnt;
		cnt++;
	}

	@Override
	public List<Edge> getOutgoingEdges() {
		return outgoingEdges;
	}

	@Override
	public String toString() {
		return vertexName + ", id: " + id;
	}

}
