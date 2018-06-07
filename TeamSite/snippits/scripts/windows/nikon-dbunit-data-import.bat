@echo off
CLS
SETLOCAL

SET LIB_PATH=C:\development\DBUnitExport
SET CP=%CP%;%LIB_PATH%\dbunit-tools.jar
SET CP=%CP%;..\..\data
SET CP=%CP%;%LIB_PATH%\lib\dbunit-2.3.0.jar
SET CP=%CP%;%LIB_PATH%\lib\log4j-1.2.15.jar
SET CP=%CP%;%LIB_PATH%\lib\slf4j-api-1.5.3.jar
SET CP=%CP%;%LIB_PATH%\lib\slf4j-log4j12-1.5.3.jar
SET CP=%CP%;%LIB_PATH%\lib\sqljdbc.jar

REM call the import class with the following parameters
REM %1 - jdbc Driver Class 
REM %2 - jdbc URL
REM %3 - database username
REM %3 - database password
REM %3 - dbunit input file location

ECHO %CP%

java -cp %CP% com.interwoven.nikon.scratch.dbunit.DatabaseImport "com.microsoft.sqlserver.jdbc.SQLServerDriver" "jdbc:sqlserver://192.168.8.130:1433;databaseName=LSDev" "sa" "nikonsa" %1 

ENDLOCAL



