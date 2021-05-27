# DSAssignment2

What is it?
- This is my assignment work for a database course, the object is to compare and analysis the performance between different type of databases.
- 3 part of analysis: 1. use Derby database as relational database 2. use MongoDB as non-SQL database 3. use my own B+ tree indexing database that based on JAVA.
- Note that the environment for testing was in AWS EC2 instance which was running UNIX. The instruction below was only suitable for that environment.

- HOW TO USE:
	1.  RUN "JAVAC *.JAVA", because there are some unsafe cast in b plus node class.
	2.  RUN DBLOAD AS SAME IN ASSIGNMENT 1: "JAVA DBLOAD -P PAGESIZE DATAFILE"
	3.  RUN 'Java TreeSave pagesize'
	4.  RUN 'Java Treequery query1 pagesize tree_file_name'
	4.a OR RUN 'Java Treequery query1 query2 pagesize tree_file_name linked_list_file_name' FOR RANGE SEARCH.
