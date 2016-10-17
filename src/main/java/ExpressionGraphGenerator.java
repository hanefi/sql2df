package main;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Creates a sub-graph for visited expression.
 * Last (root) vertex is stored in rootVertex.
 * Data type of the visited expression is stored in dataType.
 */
public class ExpressionGraphGenerator implements ExpressionVisitor, ItemsListVisitor {

    /**
     * Root vertex of the visited expression.
     */
    Vertex rootVertex;
    /**
     * Data type of the visited expression.
     */
    String dataType;


    @Override
    public void visit(NullValue nullValue) {
        rootVertex = new VertexImpl("NULL");
        dataType = "boolean";
    }

    @Override
    public void visit(Function function) {
        System.out.println("AAA");
        String name = function.getName().toLowerCase(Locale.ENGLISH);
        rootVertex = new VertexImpl(name);
        List<Expression> expressionList = new ArrayList<>();
        if (function.getParameters() != null) {
            expressionList = function.getParameters().getExpressions();
        }
        List<String> parameterTypes = new ArrayList<>();
        for (Expression expression : expressionList) {
            ExpressionGraphGenerator parameterGraphGenerator = new ExpressionGraphGenerator();
            expression.accept(parameterGraphGenerator);
            String parameterType = parameterGraphGenerator.dataType;
            parameterTypes.add(parameterType);
            ExpressionEdgeImpl.createEdge("", parameterGraphGenerator.rootVertex, rootVertex, parameterType);
        }
        dataType = App.getReturnTypeOfFunctionWithParameters(name, parameterTypes);
    }

    @Override
    public void visit(SignedExpression signedExpression) {
        if (signedExpression.getSign() == '-') {
            rootVertex = new VertexImpl("-");
            ExpressionGraphGenerator expressionGraphGenerator = new ExpressionGraphGenerator();
            signedExpression.getExpression().accept(expressionGraphGenerator);
            dataType = expressionGraphGenerator.dataType;
            ExpressionEdgeImpl.createEdge("", expressionGraphGenerator.rootVertex, rootVertex, dataType);
        } else {
            signedExpression.getExpression().accept(this);
        }
    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {

    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {

    }

    @Override
    public void visit(DoubleValue doubleValue) {
        rootVertex = new VertexImpl("" + doubleValue.getValue());
        dataType = "double precision";
    }

    @Override
    public void visit(LongValue longValue) {
        rootVertex = new VertexImpl(longValue.getStringValue());
        long value = longValue.getValue();
        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
            dataType = "integer";
        } else {
            dataType = "bigint";
        }
    }

    @Override
    public void visit(HexValue hexValue) {

    }

    @Override
    public void visit(DateValue dateValue) {
        rootVertex = new VertexImpl(dateValue.getValue().toString());
        dataType = "date";
    }

    @Override
    public void visit(TimeValue timeValue) {
        rootVertex = new VertexImpl(timeValue.getValue().toString());
        dataType = "time";
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        rootVertex = new VertexImpl(timestampValue.getValue().toString());
        dataType = "timestamp";
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue) {
        String string = stringValue.getValue();
        rootVertex = new VertexImpl(string);
        dataType = "char (" + string.length() + ")";
    }

    @Override
    public void visit(Addition addition) {
        visitBinaryMathematicalOperator(addition);
    }

    @Override
    public void visit(Division division) {
        visitBinaryMathematicalOperator(division);
    }

    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryMathematicalOperator(multiplication);
    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryMathematicalOperator(subtraction);
    }

    private void visitBinaryMathematicalOperator(BinaryExpression binaryExpression) {

        rootVertex = new VertexImpl(binaryExpression.getClass().getSimpleName());
        ExpressionGraphGenerator leftGraphGenerator = new ExpressionGraphGenerator();
        binaryExpression.getLeftExpression().accept(leftGraphGenerator);
        ExpressionGraphGenerator rightGraphGenerator = new ExpressionGraphGenerator();
        binaryExpression.getRightExpression().accept(rightGraphGenerator);

        String leftType = leftGraphGenerator.dataType;
        String rightType = rightGraphGenerator.dataType;

        ExpressionEdgeImpl.createEdge("", leftGraphGenerator.rootVertex, rootVertex, leftType);
        ExpressionEdgeImpl.createEdge("", rightGraphGenerator.rootVertex, rootVertex, rightType);

        if (ExpressionEdgeImpl.dataSize(rightType) > ExpressionEdgeImpl.dataSize(leftType)) {
            dataType = rightType;
        } else {
            dataType = leftType;
        }
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryLogicalOperator(andExpression);
    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryLogicalOperator(orExpression);
    }

    private void visitBinaryLogicalOperator(BinaryExpression binaryExpression) {
        String operator = "";
        if (binaryExpression.getClass() == OrExpression.class) {
            operator = "Or";
        } else if (binaryExpression.getClass() == AndExpression.class) {
            operator = "And";
        }
        rootVertex = new VertexImpl(operator);
        ExpressionGraphGenerator leftGraphGenerator = new ExpressionGraphGenerator();
        binaryExpression.getLeftExpression().accept(leftGraphGenerator);
        ExpressionGraphGenerator rightGraphGenerator = new ExpressionGraphGenerator();
        binaryExpression.getRightExpression().accept(rightGraphGenerator);
        ExpressionEdgeImpl.createEdge("", leftGraphGenerator.rootVertex, rootVertex, leftGraphGenerator.dataType);
        ExpressionEdgeImpl.createEdge("", rightGraphGenerator.rootVertex, rootVertex, rightGraphGenerator.dataType);
        dataType = "boolean";
    }

    @Override
    public void visit(Between between) {
        GreaterThanEquals greaterThanEquals = new GreaterThanEquals();
        greaterThanEquals.setLeftExpression(between.getLeftExpression());
        greaterThanEquals.setRightExpression(between.getBetweenExpressionStart());

        MinorThanEquals minorThanEquals = new MinorThanEquals();
        minorThanEquals.setLeftExpression(between.getLeftExpression());
        minorThanEquals.setRightExpression(between.getBetweenExpressionEnd());

        AndExpression andExpression = new AndExpression(greaterThanEquals, minorThanEquals);
        andExpression.accept(this);
    }

    private void visitBinaryComparisonOperator(OldOracleJoinBinaryExpression oldOracleJoinBinaryExpression) {
        Expression leftExpression = oldOracleJoinBinaryExpression.getLeftExpression();
        Expression rightExpression = oldOracleJoinBinaryExpression.getRightExpression();

        String operator = oldOracleJoinBinaryExpression.getClass().getSimpleName();
        if (leftExpression.getClass() == Column.class && rightExpression.getClass() == Column.class) {
            operator = "JOIN " + operator;
        }

        ExpressionGraphGenerator leftGraphGenerator = new ExpressionGraphGenerator();
        leftExpression.accept(leftGraphGenerator);

        ExpressionGraphGenerator rightGraphGenerator = new ExpressionGraphGenerator();
        rightExpression.accept(rightGraphGenerator);

        rootVertex = new VertexImpl(operator);
        ExpressionEdgeImpl.createEdge("", leftGraphGenerator.rootVertex, rootVertex, leftGraphGenerator.dataType);
        ExpressionEdgeImpl.createEdge("", rightGraphGenerator.rootVertex, rootVertex, rightGraphGenerator.dataType);
        dataType = "boolean";
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitBinaryComparisonOperator(equalsTo);
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        visitBinaryComparisonOperator(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryComparisonOperator(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        Expression leftExpression = inExpression.getLeftExpression();
        List<Expression> expressionList = ((ExpressionList) inExpression.getRightItemsList()).getExpressions();

        Iterator<Expression> expressionIterator = expressionList.iterator();

        EqualsTo equalsTo = new EqualsTo();
        equalsTo.setLeftExpression(leftExpression);
        equalsTo.setRightExpression(expressionIterator.next());

        Expression rootExpression = equalsTo;

        while (expressionIterator.hasNext()) {
            equalsTo = new EqualsTo();
            equalsTo.setLeftExpression(leftExpression);
            equalsTo.setRightExpression(expressionIterator.next());
            rootExpression = new OrExpression(rootExpression, equalsTo);
        }

        rootExpression.accept(this);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

    }

    @Override
    public void visit(LikeExpression likeExpression) {

    }

    @Override
    public void visit(MinorThan minorThan) {
        visitBinaryComparisonOperator(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryComparisonOperator(minorThanEquals);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryComparisonOperator(notEqualsTo);
    }

    @Override
    public void visit(Column tableColumn) {
        rootVertex = new VertexImpl(tableColumn.getColumnName().toLowerCase(Locale.ENGLISH));
        String columnName = tableColumn.getColumnName().toLowerCase(Locale.ENGLISH);
        dataType = App.dataTypeOfColumnMap.get(columnName).toString().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public void visit(SubSelect subSelect) {

    }

    @Override
    public void visit(ExpressionList expressionList) {
        rootVertex = new VertexImpl("values");
        List<Expression> expressions = expressionList.getExpressions();
        for (Expression expression : expressions) {
            ExpressionGraphGenerator subGraphGenerator = new ExpressionGraphGenerator();
            expression.accept(subGraphGenerator);
            ExpressionEdgeImpl.createEdge("", subGraphGenerator.rootVertex, rootVertex, subGraphGenerator.dataType);
        }
        dataType = "list";
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {

    }

    @Override
    public void visit(CaseExpression caseExpression) {

    }

    @Override
    public void visit(WhenClause whenClause) {

    }

    @Override
    public void visit(ExistsExpression existsExpression) {

    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {

    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {

    }

    @Override
    public void visit(Concat concat) {

    }

    @Override
    public void visit(Matches matches) {
        visitBinaryComparisonOperator(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {

    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {

    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {

    }

    @Override
    public void visit(CastExpression cast) {

    }

    @Override
    public void visit(Modulo modulo) {

    }

    @Override
    public void visit(AnalyticExpression aexpr) {

    }

    @Override
    public void visit(WithinGroupExpression wgexpr) {

    }

    @Override
    public void visit(ExtractExpression eexpr) {

    }

    @Override
    public void visit(IntervalExpression iexpr) {
        rootVertex = new VertexImpl(iexpr.toString());
        dataType = "interval";
    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {

    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {

    }

    @Override
    public void visit(JsonExpression jsonExpr) {

    }

    @Override
    public void visit(RegExpMySQLOperator regExpMySQLOperator) {

    }

    @Override
    public void visit(UserVariable var) {

    }

    @Override
    public void visit(NumericBind bind) {

    }

    @Override
    public void visit(KeepExpression aexpr) {

    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {

    }

    @Override
    public void visit(RowConstructor rowConstructor) {

    }
}
