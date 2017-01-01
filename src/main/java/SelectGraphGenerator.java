package main;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Creates a sub-graph for visited select item.
 * This sub-graph is connected to the dummy END node provided in the constructor.
 */
public class SelectGraphGenerator implements SelectItemVisitor{
    //private Vertex endVertex;
	MetaVertex metaVertex;
	FromItem fromItem;
	Set<String> tables;
    /**
     * Creates a SelectItemVisitor that can be used to generate a select sub-graph that is connected to the dummy END node.
     * @param endVertex Dummy end node
     */
    public SelectGraphGenerator(MetaVertex metaVertex, FromItem fromItem, Set<String> tables){
    	this.metaVertex = metaVertex;
    	this.fromItem = fromItem;
    	this.tables = tables;
    }
    
   // public SelectGraphGenerator(){
   // 	super("UNKNOWN");
   // }
	
	//public SelectGraphGenerator(Vertex endVertex) {
    //    this.endVertex = endVertex;
    //}

    @Override
    public void visit(AllColumns allColumns) {
        System.out.println("I am visiting allcolumns in SELECTGRAPHGENERATOR "+allColumns);
      
    	for(String table : tables)
    		for(String column : App.columnsOfTableMap.get(table)){
    			metaVertex.putEdge(column, metaVertex.rootVertex, metaVertex.sinkVertex, App.dataTypeOfColumnMap.get(column));
    		}
        
        //  SelectGraphGenerator selectGraphGenerator = new SelectGraphGenerator(metaVertex);
       // allColumns.accept(selectGraphGenerator);
        //String name = "";
        //Should be to SINK
       // visit((AllTableColumns)allColumns);
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        System.out.println("I am visiting alltablecolumns in SELECTGRAPHGENERATOR");
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        System.out.println("I am visiting in SELECTGRAPHGENERATOR");
    	ExpressionGraphGenerator expressionGraphGenerator = new ExpressionGraphGenerator(metaVertex, tables);
        selectExpressionItem.getExpression().accept(expressionGraphGenerator);
        String name = "";
        if (selectExpressionItem.getAlias() != null) {
            name = selectExpressionItem.getAlias().getName();
        }
        metaVertex.putEdge(name, expressionGraphGenerator.rootVertex, metaVertex.sinkVertex, expressionGraphGenerator.dataType);		
        //Should be to SINK
    }
}
