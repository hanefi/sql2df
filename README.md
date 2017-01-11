# Folder structure

- [**doc**](https://github.com/cmpe492-cg/sql2df/tree/master/doc) folder contains JavaDoc documentation generated from the source code comments. They describe the functionality of classes and functions.
- [**res**](https://github.com/cmpe492-cg/sql2df/tree/master/res) folder contains non-code resources such as data type definitions, CREATE TABLE queries, SELECT queries, function declarations, etc.
- [**src/main/java**](https://github.com/cmpe492-cg/sql2df/tree/master/src/main/java) folder contains the source code. It is heavily commented and JavaDoc documentation generated from this comments can be found in *res* folder.
- [**.idea**](https://github.com/cmpe492-cg/sql2df/tree/master/.idea) folder contains IDE related configuration files. It is safe to ignore it, if you are not using Intellij IDEA.

# Overall explanation of the framework

**Edge** is an interface defined in Edge.java to represent dataflow between two vertices. One class implements it:
- *EdgeImpl*: Used for data flow representation in the data-flow graph. It holds the data type as a string, from which the data size can be extracted.

**Vertex** is an abstract class used to represent operations on or sources of data flows. It has three implementing classes:
- *TableVertex*: Used for representing a table as the origin of a data flow. Used through static getTableVertex method. When this method is used, if a vertex is already created for a table, it is returned. Otherwise a new vertex is created.
- *VertexImpl*: Used for representing all other vertices. It guarantees the uniqueness of each vertex by adding an unique id to each created instance.
- *MetaVertex*: Used for representing encapsulated sub-graphs. 

**FunctionDef** is a class to store function declarations. It identifies a function by its name, the type of its parameters, and the type of its return value.

**ColumnNamesExtractor** is an *`ExpressionVisitor`* class used to extract the column names from an expression. To extract the column names from an Expression `e`, a ColumnNamesExtractor `c` is used as: `e.accept(c)`. Then the return value of `c.getColumns()` will be the set of column names used in `e`.

**ExpressionGraphGenerator** is also an *ExpressionVisitor* class. It is used to generate the low level data-flow graph of an expression. On an Expression `e`, an **ExpressionGraphGenerator** `g` is called as: `e.accept(g)`. After this call `g.rootVertex` will be the last (root) vertex of the generated data-flow graph (or tree in most cases), and `g.dataType` will be the data type of the expression. This class contains a visit function for each possible Expression type. Not all of them are completed, but most of the SQL standard is implemented.

**SelectGraphGenerator** is a *SelectItemVisitor*. It is used to generate the low level data-flow graph of a select item. As all select items should be in the same low level graph, they're connected to a dummy END node. This node should be provided to this class' constructor.

**App** class is the main body of the application. It includes many utility functions. Explanations and some pointers to most of them are given here:
- *readStorageSizes*: Reads the storage sizes of data types from res/data-types folder. Each file in this folder is parsed.
- *readFunctions*: Reads the function declarations from res/function-declarations folder. Each file in this folder is parsed.
- *readQueriesFromFile*: Read multiple queries from a file. Queries must be separated by a semicolon. Returns a List of Strings.
- *learnTableSchema*: Gets a CREATE TABLE query. Extracts the schema information from this query. Updates 3 maps:
  - `tableOfColumnMap`: Key is column name, value is its table name
  - `columnsOfTableMap`: Key is table name, value is the set of its column names
  - `dataTypeOfColumnMap`: Key is column name, value is its data type
  Assumes that no two columns with the same name exist.
- *processQuery*: Processes a query. First of all, it parses a query. Then creates a high level data-flow graph with table name, FILTER, GROUP, SELECT, and ORDER nodes. Two low level graphs are also created. One for filtering and one for selection. As this method is the most of the application, detailed explanation is done at the source code file as the source code comments.

# Other material
The detailed **project report** for this framework can be found [here](https://www.dropbox.com/s/lawg5c1x8qbxnnp/Grad_Project___Final_Report.pdf?dl=0).

The **project report** of the previous project can be found [here](https://www.dropbox.com/s/x7vsl4mf3v7nbgy/son.pdf?dl=0).
An explanation **video** can be watched [here](https://www.youtube.com/watch?v=u81K82FrYAo)
An explanation **video** of the previous project can be watched [here](https://youtu.be/r7FHBXGyH4E).
