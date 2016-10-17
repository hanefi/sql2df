import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEdgeImpl implements Edge {

    public String edgeName;
    private Vertex sourceVertex;
    private Vertex destinationVertex;
    public String dataType;


    public static List<ExpressionEdgeImpl> edgeList = new ArrayList<>();

    public Vertex getSourceVertex() {
        return sourceVertex;
    }

    public void setSourceVertex(Vertex sourceVertex) {
    	this.sourceVertex.removeOutgoingEdge(this);
        this.sourceVertex = sourceVertex;
        sourceVertex.addOutgoingEdge(this);
    }

    public Vertex getDestinationVertex() {
        return destinationVertex;
    }

    public void setDestinationVertex(Vertex destinationVertex) {
    	this.destinationVertex.removeIncomingEdge(this);
        this.destinationVertex = destinationVertex;
        destinationVertex.addIncomingEdge(this);
    }

    public static ExpressionEdgeImpl createEdge(String edgeName, Vertex sourceVertex, Vertex destinationVertex, String dataType) {
        ExpressionEdgeImpl edgeImpl = new ExpressionEdgeImpl();
        edgeImpl.edgeName = edgeName;
        edgeImpl.sourceVertex = sourceVertex;
        edgeImpl.destinationVertex = destinationVertex;
        edgeImpl.dataType = dataType;
        
        sourceVertex.addOutgoingEdge(edgeImpl);
        destinationVertex.addIncomingEdge(edgeImpl);
        
        edgeList.add(edgeImpl);
        return edgeImpl;
    }


    public static int dataSize(String dataType) {
        String bareTypeName = dataType;
        int length = 1;
        Pattern arrayPattern = Pattern.compile("(\\w+)\\s*\\(\\s*(\\d+)");
        Matcher arrayMatcher = arrayPattern.matcher(dataType);
        
        if (arrayMatcher.find()) {
            bareTypeName = arrayMatcher.group(1);
            length = Integer.parseInt(arrayMatcher.group(2));
        }
    
        if (App.storageSizeMap.containsKey(bareTypeName)) {
            return App.storageSizeMap.get(bareTypeName) * length;
        } else {
            return -1;
        }
    }
    
	public static ExpressionEdgeImpl fromString(String edgeString, Vertex sourceVertex, Vertex destinationVertex) {
		Pattern edgePattern = Pattern.compile("(.*)\\[(.*)\\](.*)");
		Matcher edgeMatcher = edgePattern.matcher(edgeString);
		if(edgeMatcher.find()){
			String edgeName = edgeMatcher.group(1);
			String dataType = edgeMatcher.group(2);
			return createEdge(edgeName, sourceVertex, destinationVertex, dataType);
		} else {
			throw new IllegalArgumentException("Cannot parse edge from String: " + edgeString);
		}
	}

    @Override
    public String toString() {
        return edgeName + "[" + dataType.toString() + "] = " + dataSize(dataType);
    }

}