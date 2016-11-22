package main;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

import java.util.Locale;

/**
 * Creates a sub-graph for visited select item.
 * This sub-graph is connected to the dummy END node provided in the constructor.
 */
public class SelectGraphGenerator implements SelectItemVisitor{
    //private Vertex endVertex;
	MetaVertex metaVertex;
    /**
     * Creates a SelectItemVisitor that can be used to generate a select sub-graph that is connected to the dummy END node.
     * @param endVertex Dummy end node
     */
    public SelectGraphGenerator(MetaVertex metaVertex){
    	this.metaVertex = metaVertex;
    }
    
   // public SelectGraphGenerator(){
   // 	super("UNKNOWN");
   // }
	
	//public SelectGraphGenerator(Vertex endVertex) {
    //    this.endVertex = endVertex;
    //}

    @Override
    public void visit(AllColumns allColumns) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        ExpressionGraphGenerator expressionGraphGenerator = new ExpressionGraphGenerator("SELECT_META", metaVertex.subGraph);
        selectExpressionItem.getExpression().accept(expressionGraphGenerator);
        String name = "";
        if (selectExpressionItem.getAlias() != null) {
            name = selectExpressionItem.getAlias().getName();
        }
        metaVertex.putVertex(expressionGraphGenerator);
        expressionGraphGenerator.putEdge(name, expressionGraphGenerator.rootVertex, metaVertex.sinkVertex, expressionGraphGenerator.dataType);		
        //Should be to SINK
    }
}
