import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionEdgeImpl implements Edge {

    private String edgeName;
    private Vertex sourceVertex;
    private Vertex destinationVertex;
    private String dataType;


    public static List<ExpressionEdgeImpl> edgeList = new ArrayList<>();

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

    public static ExpressionEdgeImpl createEdge(String edgeName, Vertex sourceVertex, Vertex destinationVertex, String dataType) {
        ExpressionEdgeImpl edgeImpl = new ExpressionEdgeImpl();
        edgeImpl.edgeName = edgeName;
        edgeImpl.sourceVertex = sourceVertex;
        edgeImpl.destinationVertex = destinationVertex;
        edgeImpl.dataType = dataType;
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

    @Override
    public String toString() {
        return edgeName + "[" + dataType.toString() + "] = " + dataSize(dataType);
    }

}