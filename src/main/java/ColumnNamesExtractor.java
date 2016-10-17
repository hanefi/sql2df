package main;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Set of the column names used in the expression that accepts an instance of ColumnNamesExtractor
 * can be retrieved by getColumns method.
 */
public class ColumnNamesExtractor extends ExpressionVisitorAdapter {
	private Set<String> columns;
    private Set<String> tables;


    /**
     * Creates an ExpressionVisitor that can be used to extract column names.
     * @param tables Name of the tables used in the SELECT query that will be visited by this
     */
	public ColumnNamesExtractor(Set<String> tables) {
		super();
		columns = new HashSet<>();
		this.tables = new HashSet<>(tables);
	}

	@Override
	public void visit(Column column) {
		super.visit(column);
		columns.add(column.getColumnName());
	}

	@Override
	public void visit(AllColumns allColumns) {
		super.visit(allColumns);
		for (String table : tables) {
			columns.addAll(App.columnsOfTableMap.get(table));
		}
	}

	@Override
	public void visit(AllTableColumns allTableColumns) {
		super.visit(allTableColumns);
		String table = allTableColumns.getTable().getName().toLowerCase(Locale.ENGLISH);
		columns.addAll(App.columnsOfTableMap.get(table));
	}


    /**
     * Returns the set of column names that are used in the visited expression.
     * @return Set of column name strings
     */
	public Set<String> getColumns() {
		return new HashSet<>(columns);
	}
}
