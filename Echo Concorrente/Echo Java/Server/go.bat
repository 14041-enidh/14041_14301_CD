@echo off
echo off

set JAVA_HOME=C:\Java\jdk-21

call mvn exec:java

rem call mvn exec:java -Dexec.args="12346"

pause