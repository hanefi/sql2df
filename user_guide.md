# Dependencies
First of all, you need to install [Java Runtime Environment](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [GraphViz](http://www.graphviz.org/Download.php) to your computer. The code is written and tested using Java version 8. JRE automatically adds its executables to the system's path but you may need to add GraphViz to your PATH manually. In Windows, you may need to enter *Control Panel*, *System and Security*, *System*. In the right bar you can select *Advanced System Settings*, and click on *Environment Variables*. In the *System variables* list select the *Path* and click on the *Edit* button. Then add bin directory of the GraphViz at the end of the Path string separated with a semicolon (;).

# How to run
sql2df.jar is an executable jar archive. It contains all necessary Java dependencies. It should be run with two command line arguments:

`java -jar Sql2Df2-all-1.0.jar <create table query file> <select query file>`

1. SQL file containing CREATE TABLE queries to extract table schema
2. SQL file containing SELECT query to be converted.

An example invokation:

`java -jar Sql2Df2-all-1.0.jar res/create_table.sql res/q1.sql`.

On Windows OS, instead of "/", you may need to use "\".

This application assumes that a `res` folder resides next to it and this `res` folder contains `data-types` and `function-declarations` sub-folders. These are the folders that contains storage sizes for data types and function declarations.

After invoking the application, two new files are generated. For the above example these are:
- `q1.sql.dot`: High level data-flow graph in DOT language.
- `q1.sql.filter.dot`: Low level filtering and selection sub-graphs in DOT language.

# Visualization

Even though the DOT language is designed to be human readable, a graphical representation should be better for most. GraphViz is a very popular graph visualization platform that visualizes graphs described in the DOT language. It can called with the following command:

- `cd res`
- `dot -Tpng q1.sql.dot -o q1.sql.png`
- `dot -Tpng q1.sql.filter.dot -o q1.sql.filter.png`

After these `dot` invocations, two image files *q1.sql.png* and *q1.sql.filter.png* should be present. They are the high level data-flow graph and the low level filtering / selection graphs respectively.
