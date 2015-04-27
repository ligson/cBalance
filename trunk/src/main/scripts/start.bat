@echo off
set SERVER_HOME=%~dp0..
set LOCALCLASSPATH=%SERVER_HOME%\.;%SERVER_HOME%\conf

for %%f in (%SERVER_HOME%\lib\*.jar) do call %SERVER_HOME%\bin\cbalance %%f
for %%f in (%SERVER_HOME%\conf\*.*) do call %SERVER_HOME%\bin\cbalance %%f

:execute
echo java -classpath %LOCALCLASSPATH% %1 %2 %3 %4 %5 %6 %7 %8 %9
java -classpath %LOCALCLASSPATH% com.boful.cbalance.server.CBalanceServer

pause