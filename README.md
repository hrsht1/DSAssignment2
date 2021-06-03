The following steps are required in order for successful running the application:

1. Run DbLoad which loads csv file and convert it into a heap file.
Command to run on cmd: java DbLoad -p <page size> <file path>
For Instance: java DbLoad -p 4096 Pedestrian_Counting_System_-_Monthly__counts_per_hour_.csv

2. Run TreeLoad which creates a B+tree BPTree and writes all data into file with name "tree.4096"
Command to run on cmd: java -Xmx900m TreeLoad <page size>
For Instance: "java -Xmx900m TreeLoad 4096"

3. Run TreeQuery which read B+tree BPTree from tree.4096 and queries all indexes to fetch data.
How to run: java -Xmx900m TreeQuery <keyword> <page size>
For Instance: "java -Xmx900m TreeQuery 27-08/30/2016 04:00:00 AM", will return matched records from indexed file.