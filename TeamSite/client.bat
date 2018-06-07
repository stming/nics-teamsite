@echo off
cls
SETLOCAL
SET JAVA_HOME=C:\Progra~1\Java\java_5_EE\SDK\jdk
REM SET JAVA_HOME=C:\Progra~1\Java\jdk1.5.0_16
ANT %1 %2 %3 %4 %5

ENDLOCAL
