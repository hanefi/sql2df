import net.sf.jsqlparser.statement.create.table.ColDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * The Edge implementation used for high level data-flow graph.
 * All edges can be accessed from edgeList.
 */
public class EdgeImpl implements Edge {
	private String edgeName;
	private Vertex sourceVertex;
	private Vertex destinationVertex;
    private String dataType;

    /**
     * List of all edges that is used in the high level data-flow graph.
     */
    public static List<EdgeImpl> edgeList = new ArrayList<>();

	public Vertex getSourceVertex() {
		return sourceVertex;
	}
	
	public void setSourceVertex(Vertex sourceVertex) {
		this.sourceVertex = sourceVertex;
	}
	
	public Vertex getDestinationVertex() {
		return destinationVertex;
	}
	
	public void setDestinationVertex(Vertex destinationVertex) {
		this.destinationVertex = destinationVertex;
	}


    /**
     * Creates an edge between two vertices.
     * @param edgeName Name of the edge. Will be displayed on the visual graph.
     * @param sourceVertex Source vertex
     * @param destinationVertex Destination vertex
     * @param dataType Data type of the edge
     * @return Newly created edge
     */
	public static EdgeImpl createEdge(String edgeName, Vertex sourceVertex, Vertex destinationVertex, String dataType) {
		EdgeImpl edgeImpl = new EdgeImpl();
		edgeImpl.edgeName = edgeName;
		edgeImpl.sourceVertex = sourceVertex;
		edgeImpl.destinationVertex = destinationVertex;
        edgeImpl.dataType = dataType;
		edgeList.add(edgeImpl);
		return edgeImpl;
	}

	@Override
	public String toString() {
		return edgeName + "[" + dataType.toString() + "]";
	}

}
