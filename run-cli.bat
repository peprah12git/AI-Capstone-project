@echo off
echo Running OrderCLI...
mvn compile exec:java -Dexec.mainClass="com.AI.OrderCLI"
pause
