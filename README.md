* "StackServer" java application, manages TCP connections and byte bufferes using java NIO

Build Instructions
------------------
* Uses gradle as build tool. Will work with gradle version 6.3
* build.gradle file is used for the build and creates a uber jar
* Build command "gradle build"
* The build artifacts are placed in build/libs folder
* The name of the runnable jar is "philoSS-all.jar"

Run instruction
---------------
* The name of the runnable har is "philoSS-all.jar"
* Run the jar with the command "java -jar philoSS-all.jar"
* The application is hardcoded to run with capacity of 100 by default
* Configure heap if needed "java -Xmx512m -jar philoSS-all.jar"

Limitations
-----------

* Application has been tested for 100 concurrent TCP clients
* Applicaton may not be able to handle handle a higher load with additional configuration
* The stack-test.rb suite runs for about 3 minutes, so please wait for atleast 3 minutes before killing the suite
* If the suite is aborted, please restart the server before running the suite again 
