package main;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.select.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class App {

    public static Map<String, String> tableOfColumnMap = new HashMap<>();
    public static Map<String, Set<String>> columnsOfTableMap = new HashMap<>();
    public static Map<String, String> dataTypeOfColumnMap = new HashMap<>();
    public static Map<String, Map<String, String>> functionsMap = new HashMap<>();
    public static Map<String, Integer> storageSizeMap = new HashMap<>();
    public static List<FunctionDef> functionDefs = new LinkedList<>();


    /**
     * Generates the filter sub-graph.
     * @param filterExpression Expression that is used in filtering, WHERE clause of the query.
     * @param selectColumns Columns that are used in SELECT clause of the query.
     */
    public static void generateFilterGraph(Expression filterExpression, Set<String> selectColumns) {
        // Generates a sub-graph with one boolean filter output.
        ExpressionGraphGenerator expressionGraphGenerator = new ExpressionGraphGenerator();
        filterExpression.accept(expressionGraphGenerator);
        Vertex filterRootVertex = expressionGraphGenerator.rootVertex;

        Vertex endVertex = new VertexImpl("END_FILTER");    // Creates a dummy END vertex to connect all filtered columns.

        /**
         * Creates a selection vertex for each column that is used in SELECT clause.
         * The boolean filtering output, filterRootVertex, and the column to be filtered is connected to the selection vertex.
         * Output of the selection vertex, filtered column, is connected to the dummy END vertex.
         */
        for (String selectColumn : selectColumns) {
            Vertex selectVertex = new VertexImpl("Selection");
            String columnDataType = dataTypeOfColumnMap.get(selectColumn).toString().toLowerCase(Locale.ENGLISH);
            ExpressionEdgeImpl.createEdge("", new VertexImpl(selectColumn), selectVertex, columnDataType);
            ExpressionEdgeImpl.createEdge("", filterRootVertex, selectVertex, "boolean");
            ExpressionEdgeImpl.createEdge(selectColumn + "'", selectVertex, endVertex, columnDataType);
        }
    }


    /**
     * Generates the select sub-graph.
     * @param plainSelect Body of a plain select query
     */
    public static void generateSelectGraph(PlainSelect plainSelect) {
        Vertex endVertex = new VertexImpl("END_SELECT");    // Creates a dummy END vertex to connect all produced data.

        // A data-flow graph whose root is connected to the dummy END vertex is generated for each select item.
        for (SelectItem selectItem : plainSelect.getSelectItems()) {
            SelectGraphGenerator selectGraphGenerator = new SelectGraphGenerator(endVertex);
            selectItem.accept(selectGraphGenerator);
        }
    }


    /**
     * Finds the return type of a defined function.
     * @param name Name of the function
     * @param parameters Ordered list of function's parameters
     * @return Return type of the given function
     */
    public static String getReturnTypeOfFunctionWithParameters(String name, List<String> parameters) {
        // Searches given function in the defined functions list.
        for (FunctionDef functionDef : functionDefs) {
            if (functionDef.name.equals(name)) {
                if (functionDef.parameters.equals(parameters)) {
                    return functionDef.returnType;
                } else if (!functionDef.parameters.isEmpty() &&
                        functionDef.parameters.get(0).equals("any")) {
                    return functionDef.returnType;
                }
            }
        }

        // If the function with the given parameters is not in the list, then the return type is the type of the first parameter.
        if (!parameters.isEmpty()) {
            return parameters.get(0);
        }

        /**
         * If the function with given parameters doesn't match any defined function
         * and it doesn't have any argument then throw an exception.
         */
        try {
            throw new Exception("unsupported function: " + name);
        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }


    public static void main(String[] args) throws Exception {
        readStorageSizes(); // Reads storage size of data types.
        readFunctions();    // Reads the signatures of functions.
        List<String> createQueries = readQueriesFromFile(new File(args[0]));    // Reads create table queries.

        // Extracts table schemas from each CREATE TABLE query.
        for (String query : createQueries) {
            System.out.println("The table create query is: " + query); //DEBUG
            learnTableSchema(query);
        }

        // Reads SELECT queries from the file specified in the first argument.
        List<String> selectQueries = readQueriesFromFile(new File(args[1]));
        // Processes each SELECT query to generate data-flow graphs.
        for (String selectQuery : selectQueries) {
            System.out.println("A SELECT query: " + selectQuery);
            processQuery(selectQuery, args[1]);
        }
    }


    /**
     * Reads function declarations from their directory.
     *
     * Each file in the res/function-declarations directory is parsed.
     * @throws FileNotFoundException
     */
    public static void readFunctions() throws FileNotFoundException {
        try {
        	  Stream<Path> stream = Files.walk(Paths.get("res/data-types"));
              Iterator<Path> iterator = stream.iterator();
              while(iterator.hasNext())
              {
            	Path filePath = iterator.next();
                if (Files.isRegularFile(filePath)) {
                    try {
                        readFunctionFromFile(filePath.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
              }
        } catch (IOException e) {
            e.printStackTrace();
        }
        readFunctionsFromFile(new File("res/functions.csv"));
    }


    /**
     * Reads multiple function declarations from the given file.
     *
     * Each line should declare a function.
     * Function's name, return type, and argument types (if they exist) should be separated by commas.
     * @param filePath Path of the declaration file.
     * @throws FileNotFoundException
     */
    public static void readFunctionFromFile(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        System.out.println(filePath);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] tokens = line.split(",");
            String name = tokens[0];
            String returnType = tokens[1];
            List<String> parameters = Arrays.asList(Arrays.copyOfRange(tokens, 2, tokens.length));
            FunctionDef functionDef = new FunctionDef(name, returnType, parameters);
            functionDefs.add(functionDef);
        }
        scanner.close();
    }


    /**
     * Reads multiple function declarations from the given file.
     *
     * Each line should declare a function.
     * Function's name, argument type, and return type should be separated by commas.
     * @param file File object of the declaration file.
     * @throws FileNotFoundException
     */
    public static void readFunctionsFromFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] tokens = line.split(",");
            System.out.println(Arrays.asList(tokens));
            String functionName = tokens[0];
            String parameter = tokens[1];
            String result = tokens[2];
            if (!functionsMap.containsKey(functionName)) {
                functionsMap.put(functionName, new HashMap<String, String>());
            }
            Map<String, String> functionMap = functionsMap.get(functionName);
            functionMap.put(parameter, result);
        }
        scanner.close();
    }


    /**
     * Processes a query.
     *
     * Processing includes parsing,
     * high level data-flow graph generation,
     * low level filter sub-graph generation,
     * and low level select sub-graph generation.
     * These graphs are printed to separate files.
     * @param selectQuery String of the select query.
     * @param queryName Name of the query file.
     * @throws Exception
     */
    public static void processQuery(String selectQuery, String queryName) throws Exception {
        Select select = (Select) CCJSqlParserUtil.parse(selectQuery);   // Parses a SELECT query.
        SelectBody selectBody = select.getSelectBody();

        System.out.println("Processing query: " + selectQuery); //DEBUG

        Set<String> tables = extractTables(selectBody); // Extracts table names from the SELECT query.

        // Extracts column names from the SELECT query.
        Set<String> selectColumns = extractSelectColumns(selectBody, tables);
        Set<String> whereColumns = extractWhereColumns(selectBody, tables);
        Set<String> allColumns = new HashSet<>();
        allColumns.addAll(selectColumns);
        allColumns.addAll(whereColumns);

        Map<String, Vertex> lastVertexOfColumn = new HashMap<>();   // Keeps the Vertex that used a column last.

        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            System.out.println("Plain Select Body: " + plainSelect); //DEBUG
            System.out.println("Before generating SELECT Graph"); //DEBUG
            generateSelectGraph(plainSelect);   // Generates the SELECT sub-graph of the query.

            System.out.println("Before generating WHERE clause"); //DEBUG
            Expression expression = plainSelect.getWhere(); // WHERE clause of the SELECT query.
            System.out.println("WHERE Clause: " + expression); //DEBUG
            if (expression != null) {
                System.out.println("Before generating FILTER Graph"); //DEBUG
                generateFilterGraph(expression, selectColumns); // Generates the FILTER sub-graph of the query.

                Vertex filterVertex = new VertexImpl("FILTER"); // Creates the FILTER node in the high level graph.

                // Connects each column edge from their tables to FILTER node.
                for (String column : allColumns) {
                    String table = tableOfColumnMap.get(column);
                    String dataType = dataTypeOfColumnMap.get(column);
                    EdgeImpl.createEdge(column, TableVertex.getTableVertex(table), filterVertex, dataType);
                    lastVertexOfColumn.put(column, filterVertex);
                }
            }

            List<Expression> groupByColumns = plainSelect.getGroupByColumnReferences(); // Columns used for grouping
            if (groupByColumns != null) {
                Vertex groupVertex = new VertexImpl("GROUP");   // Creates the GROUP node in the high level graph.

                // Connects each column that is used for grouping from their last vertex to GROUP vertex.
                for (Expression groupByColumn : groupByColumns) {
                    String columnName = groupByColumn.toString().toLowerCase(Locale.ENGLISH);
                    String dataType = getDataType(groupByColumn);
                    Vertex lastVertex = lastVertexOfColumn.get(columnName);
                    EdgeImpl.createEdge(columnName, lastVertex, groupVertex, dataType);
                    lastVertexOfColumn.put(columnName, groupVertex);
                }
            }

            Vertex selectVertex = new VertexImpl("SELECT"); // Creates the SELECT node in the high level graph.

            // Connects each column that is used in SELECT clause from their last vertex to SELECT vertex.
            for (String column : selectColumns) {
                System.out.println("Connecting column " + column + " to select vertex"); //DEBUG
                String dataType = dataTypeOfColumnMap.get(column);
                Vertex lastVertex = lastVertexOfColumn.get(column);
                System.out.println("Column info: "+ dataType + " " + lastVertex); //DEBUG
                if(lastVertex != null)
                    EdgeImpl.createEdge(column, lastVertex, selectVertex, dataType);
                else{
                    String table = tableOfColumnMap.get(column);
                    EdgeImpl.createEdge(column, TableVertex.getTableVertex(table), selectVertex, dataType);
                }
            }

            List<SelectItem> selectItems = plainSelect.getSelectItems();    // Selected items in the SELECT clause
            /**
             * An item in the SELECT clause may create a new data by aliasing an expression.
             * If this is the case, the new data's last vertex is set as SELECT.
             */
            for (SelectItem selectItem : selectItems) {
                if (selectItem instanceof SelectExpressionItem) {
                    SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                    Alias alias = selectExpressionItem.getAlias();
                    String name = selectItem.toString();
                    if (alias != null) {
                        name = alias.getName();
                    }
                    Vertex lastVertex = lastVertexOfColumn.get(name);
                    if (lastVertex == null) {
                        lastVertexOfColumn.put(name, selectVertex);
                    }
                }
            }

            List<OrderByElement> orderByColumns = plainSelect.getOrderByElements(); // Columns used in ordering
            if (orderByColumns != null) {
                Vertex orderVertex = new VertexImpl("ORDER");   // Creates the ORDER vertex.

                // Connects each column used for ordering from their last vertex to ORDER vertex.
                for (OrderByElement orderByColumn : orderByColumns) {
                    Expression orderByExpression = orderByColumn.getExpression();
                    String columnName = orderByExpression.toString().toLowerCase(Locale.ENGLISH);
                    String dataType = getDataType(orderByExpression);
                    Vertex lastVertex = lastVertexOfColumn.get(columnName);
                    EdgeImpl.createEdge(columnName, lastVertex, orderVertex, dataType);
                    lastVertexOfColumn.put(columnName, orderVertex);
                }

                /**
                 * Only some of data is used as the key of the ordering.
                 * They're connected to ORDER vertex in the previous loop.
                 * But all items in the SELECT clause is required by the ORDER vertex.
                 * Because these are the data to be ordered.
                 * So all non key SELECT items are connected to ORDER vertex from their last vertex.
                 */
                for (SelectItem selectItem : selectItems) {
                    if (selectItem instanceof SelectExpressionItem) {
                        SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;
                        Alias alias = selectExpressionItem.getAlias();
                        String name = selectItem.toString();
                        if (alias != null) {
                            name = alias.getName();
                        }
                        Vertex lastVertex = lastVertexOfColumn.get(name);

                        if (lastVertex != orderVertex) {
                            String dataType = getDataType(selectExpressionItem.getExpression());
                            EdgeImpl.createEdge(name, selectVertex, orderVertex, dataType);
                            lastVertexOfColumn.put(name, orderVertex);
                        }
                    }
                }
            }
        }

        // All graphs are printed to files
        generateGraphFile(new File(queryName + ".dot"));
        generateSubGraphsFile(new File(queryName + ".filter.dot"));
    }


    /**
     * Prints the high level data-flow graph to a file.
     *
     * Graph is defined in DOT language.
     * @param file File to which graph will be printed.
     * @throws FileNotFoundException
     */
    public static void generateGraphFile(File file) throws FileNotFoundException {
        PrintStream stream = new PrintStream(file);
        stream.println("digraph {");
        for (Edge edge : EdgeImpl.edgeList) {
             System.out.println(edge+ " "+ edge.getSourceVertex());
            stream.println('"' + edge.getSourceVertex().toString() + '"' + " -> " + '"'
                    + edge.getDestinationVertex().toString() + '"' + "[label=\"" + edge.toString() + "\"]");
        }
        stream.println("}");
        stream.close();
    }


    /**
     * Prints select and filter sub-graphs to a file.
     *
     * Graphs are defined in DOT language.
     * @param file File to which graphs will be printed.
     * @throws FileNotFoundException
     */
    public static void generateSubGraphsFile(File file) throws FileNotFoundException {
        PrintStream stream = new PrintStream(file);
        stream.println("digraph {");
        for (Edge edge : ExpressionEdgeImpl.edgeList) {
            System.out.println(edge+ " "+ edge.getSourceVertex());
            stream.println('"' + edge.getSourceVertex().toString() + '"' + " -> " + '"'
                    + edge.getDestinationVertex().toString() + '"' + "[label=\"" + edge.toString() + "\"]");
        }
        stream.println("}");
        stream.close();
    }


    /**
     * Extracts table names from a SELECT query.
     * @param selectBody Body of the SELECT query
     * @return Set of table names
     */
    public static Set<String> extractTables(SelectBody selectBody) {
        Set<String> tables = new HashSet<>();

        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;

            // Adds the first table to the result set.
            tables.add(plainSelect.getFromItem().toString().toLowerCase(Locale.ENGLISH));

            // Adds other joining tables to the result set.
            List<Join> joins = plainSelect.getJoins();
            if (joins != null) {
                for (Join join : joins) {
                    tables.add(join.getRightItem().toString().toLowerCase(Locale.ENGLISH));
                }
            }
        }

        return tables;
    }


    /**
     * Extracts the column names that are used in WHERE clause of the SELECT query.
     * @param selectBody Body of the SELECT query.
     * @param tables Tables used in the SELECT query.
     * @return Set of column names
     */
    public static Set<String> extractWhereColumns(SelectBody selectBody, Set<String> tables) {
        Set<String> columns = new HashSet<>();

        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;

            ColumnNamesExtractor columnNamesExtractor = new ColumnNamesExtractor(tables);
            Expression expression = plainSelect.getWhere(); // Expression of the WHERE clause
            if (expression != null) {
                // Extracts column names in the expression.
                expression.accept(columnNamesExtractor);

                // Adds these column names to the result set.
                columns.addAll(columnNamesExtractor.getColumns());
            }
        }

        return columns;
    }


    /**
     * Extracts the column names that are used in SELECT clause of the SELECT query.
     * @param selectBody Body of the SELECT query.
     * @param tables Tables used in the SELECT query.
     * @return Set of column names
     */
    public static Set<String> extractSelectColumns(SelectBody selectBody, Set<String> tables) {
        Set<String> columns = new HashSet<>();

        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            for (SelectItem selectItem : plainSelect.getSelectItems()) {
                ColumnNamesExtractor columnNamesExtractor = new ColumnNamesExtractor(tables);
                selectItem.accept(columnNamesExtractor);
                columns.addAll(columnNamesExtractor.getColumns());
            }
        }

        return columns;
    }


    /**
     * Reads multiple SQL queries from a file.
     *
     * Each query should be separated by semicolons.
     * @param file File that contains the queries.
     * @return List of queries that are represented as strings.
     * @throws FileNotFoundException
     */
    public static List<String> readQueriesFromFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        StringBuffer queryBuffer = new StringBuffer();
        List<String> queries = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            int length = nextLine.length();
            if (length > 0) {
                queryBuffer.append(nextLine);
                queryBuffer.append('\n');
                if (nextLine.charAt(nextLine.length() - 1) == ';') {
                    queries.add(queryBuffer.toString());
                    queryBuffer = new StringBuffer();
                }
            }
        }
        scanner.close();
        return queries;
    }


    /**
     * Extracts the schema of a table from a CREATE TABLE query.
     *
     * This extraction may be queried in three ways:
     * tableOfColumnMap stores the table of each column.
     * dataTypeOfColumnMap stores the type of each column.
     * columnsOfTableMap stores the set of columns for each table.
     * @param createTableQueryString String representation of the CREATE TABLE query.
     * @throws JSQLParserException
     */
    public static void learnTableSchema(String createTableQueryString) throws JSQLParserException {
        CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(createTableQueryString);

        String tableName = createTable.getTable().getName().toLowerCase(Locale.ENGLISH);

        System.out.println("In table:" + tableName); //DEBUG

        Set<String> columns = new HashSet<>();

        for (ColumnDefinition columnDefinition : createTable.getColumnDefinitions()) {
            String columnName = columnDefinition.getColumnName().toLowerCase(Locale.ENGLISH);
            ColDataType dataType = columnDefinition.getColDataType();
            System.out.println(tableName + " " + columnName + " " + dataType ); //DEBUG:
            tableOfColumnMap.put(columnName, tableName);
            dataTypeOfColumnMap.put(columnName, dataType.toString().toLowerCase(Locale.ENGLISH));
            columns.add(columnName);
        }

        columnsOfTableMap.put(tableName, columns);
    }


    /**
     * Reads the storage sizes of data types from a directory.
     *
     * Each file in res/data-types directory is traversed.
     */
    public static void readStorageSizes() {

        try {
            Stream<Path> stream = Files.walk(Paths.get("res/data-types"));
            Iterator<Path> iterator = stream.iterator();
            while(iterator.hasNext()){
            	Path filePath = iterator.next();
                if (Files.isRegularFile(filePath)) {
                    try {
                        readStorageSizeFromFile(filePath.toString());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Reads storage size of data types from a file.
     *
     * Type name and the storage size in bits should be separated by commas.
     * For types that take parameters between parenthesis, size of one should be specified.
     * @param filePath Path of the declaration file.
     * @throws FileNotFoundException
     */
    public static void readStorageSizeFromFile(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        System.out.println(filePath);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] tokens = line.split(",");
            String typeName = tokens[0];
            Integer storageSize = Integer.parseInt(tokens[1]);
            storageSizeMap.put(typeName, storageSize);
        }
        scanner.close();

    }


    /**
     * Finds the data type of an expression.
     * @param expression Expression of which data type will be found.
     * @return Data type
     * @throws Exception
     */
    public static String getDataType(Expression expression) throws Exception {
        if (expression instanceof Column) {
            Column column = (Column) expression;
            String columnName = column.getColumnName().toLowerCase(Locale.ENGLISH);
            return dataTypeOfColumnMap.get(columnName).toString().toLowerCase(Locale.ENGLISH);

        } else if (expression instanceof Function) {
            Function function = (Function) expression;
            String functionName = function.getName();
            Map<String, String> functionMap = functionsMap.get(functionName);

            ExpressionList parameterList = function.getParameters();
            if (parameterList != null) {
                Expression parameter = parameterList.getExpressions().get(0);
                String parameterType = getDataType(parameter);
                if (functionMap.containsKey(parameterType)) {
                    return functionMap.get(parameterType);
                } else if (functionMap.containsKey("any")) {
                    return functionMap.get("any");
                } else {
                    return parameterType;
                }
            } else {
                if (functionMap.containsKey("any")) {
                    return functionMap.get("any");
                } else {
                    throw new Exception("Function " + functionName + " does not have a declaration.");
                }
            }

        } else if (expression instanceof Addition ||
                expression instanceof Subtraction ||
                expression instanceof Multiplication) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            String leftType = getDataType(binaryExpression.getLeftExpression());
            String rightType = getDataType(binaryExpression.getRightExpression());
            if (getDataSize(rightType) > getDataSize(leftType)) {
                return rightType;
            } else {
                return leftType;
            }

        } else if (expression instanceof Parenthesis) {
            Parenthesis parenthesis = (Parenthesis) expression;
            return getDataType(parenthesis.getExpression());
        } else if (expression instanceof LongValue) {
            LongValue longValue = (LongValue) expression;
            long value = longValue.getValue();
            if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                return "integer";
            } else {
                return "bigint";
            }
        } else {
            System.out.println(expression.getClass());
            throw new Exception();
        }
    }


    /**
     * Returns the storage size of the given type.
     * @param dataType Data type
     * @return Storage size in bytes.
     */
    public static int getDataSize(String dataType) {
        Pattern decimalPattern = Pattern.compile("decimal\\s*\\(\\s*(\\d+)\\s*,\\s*\\d+\\s*\\)");
        Matcher decimalMatcher = decimalPattern.matcher(dataType);
        if (dataType.equals("integer")) {
            return 4;
        } else if (decimalMatcher.find()) {
            int scale = Integer.parseInt(decimalMatcher.group(1));
            int size = (int) Math.ceil(scale / 4) + 8;
            return size;
        } else {

            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(dataType);
                return -1;
            }
        }
    }
}
