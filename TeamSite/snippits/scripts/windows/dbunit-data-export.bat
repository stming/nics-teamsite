@echo off
CLS
SETLOCAL

SET LIB_PATH=c:\temp
SET CP=%LIB_PATH%\DBExp.jar
SET CP=%CP%;.
SET CP=%LIB_PATH%\dbunit-2.3.0.jar
SET CP=%LIB_PATH%\log4j-1.2.15.jar
SET CP=%LIB_PATH%\slf4j-api-1.5.3.jar
SET CP=%LIB_PATH%\slf4j-log4j12-1.5.3.jar
SET CP=%LIB_PATH%\sqljdbc.jar

REM call the import class with the following parameters
REM %1 - jdbc Driver Class 
REM %2 - jdbc URL
REM %3 - database username
REM %3 - database password
REM %3 - dbunit input file location

java -cp %CP% com.interwoven.nikon.scratch.dbunit.DatabaseImport %1 %2 %3 %4 %5

ENDLOCAL



