sql2df.jar is an executable jar archive. It contains all necessary dependencies. It should be run with two command line arguments:
- SQL file containing CREATE TABLE queries to extract table schema
- SQL file containing SELECT query to be converted.
An example invokation: `java -jar sql2df.jar res/create_table.sql res/q1.sql`.
On Windows OS, instead of "/", you may need to use "\".

This application assumes that a `res` folder resides next to it and this `res` folder contains `data-types` and `function-declarations` sub-folders. These are the folders that contains storage sizes for data types and function declarations.

After invoking the application, two new files are generated. For the above example these are:
- `q1.sql.dot`: High level data-flow graph in DOT language.
- `q1.sql.filter.dot`: Low level filtering and selection sub-graphs in DOT language.
Even though the DOT language is designed to be human readable, a graphical representation should be better for most. GraphViz is a very popular graph visualization platform that visualizes graphs described in the DOT language. It can called with the following command: `dot -Tpng q1.sql.dot -o q1.sql.png` and `dot -Tpng q1.sql.filter.dot -o q1.sql.filter.png`

After these `dot` invokations, two image files `q1.sql.png` and `q1.sql.filter.png` should be present. They are the high level data-flow graph and the low level filtering / selection graphs respectively.