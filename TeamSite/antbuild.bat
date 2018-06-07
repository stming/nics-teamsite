@echo off
cls
SETLOCAL
SET JAVA_HOME=D:\Interwoven\TeamSite\tools\java

D:\Interwoven\TeamSite\tools\ant\bin\ANT -Denv=%1 %2 %3 %4 %5

ENDLOCAL
