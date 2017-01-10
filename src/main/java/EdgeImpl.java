package main;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EdgeImpl implements Edge {

    public String edgeName;
    private Vertex sourceVertex;
    private Vertex destinationVertex;
    public String dataType;
       
    private static int GLOBAL_EDGE_ID = 0;

    public Vertex getSourceVertex() {
        return sourceVertex;
    }

    public Vertex getDestinationVertex() {
        return destinationVertex;
    }

    public EdgeImpl(String edgeName, Vertex sourceVertex, Vertex destinationVertex, String dataType) {
        this.edgeName = edgeName;
        this.sourceVertex = sourceVertex;
        this.destinationVertex = destinationVertex;
        this.dataType = dataType;
        
        sourceVertex.addOutgoingEdge(this);
        destinationVertex.addIncomingEdge(this);
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
    
	public static EdgeImpl fromString(String edgeString, Vertex sourceVertex, Vertex destinationVertex) {
		Pattern edgePattern = Pattern.compile("(.*)\\[(.*)\\](.*)");
		Matcher edgeMatcher = edgePattern.matcher(edgeString);
		if(edgeMatcher.find()){
			String edgeName = edgeMatcher.group(1);
			String dataType = edgeMatcher.group(2);
			return new EdgeImpl(edgeName, sourceVertex, destinationVertex, dataType);
		} else {
			throw new IllegalArgumentException("Cannot parse edge from String: " + edgeString);
		}
	}

    @Override
    public String toString() {
        return edgeName + "[" + dataType.toString() + "] = " + dataSize(dataType);
    }
   
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + edgeName.hashCode();
        result = 31 * result + sourceVertex.hashCode();
        result = 31 * result + destinationVertex.hashCode();
        result = 31 * result + dataType.hashCode();
        return result;
    }
    
    @Override
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof EdgeImpl)) {
            return false;
        }

        EdgeImpl edge = (EdgeImpl) o;

        return edge.edgeName.equals(edgeName) &&
        		edge.sourceVertex.equals(sourceVertex) &&
        		edge.destinationVertex.equals(destinationVertex) &&
        		edge.dataType.equals(dataType);
    }
 
    
    
    public static int getNextEdgeID(){
    	return GLOBAL_EDGE_ID ++;
    }

}