@echo off
setlocal
set home=%~dp0
set TE_HOME=%~dp0\..\..

if exist "%home%setenv.bat" call "%home%setenv.bat"
set cp="%TE_HOME%\resources"

for %%a in ("%TE_HOME%\lib"\*.jar) do call :addtocp "%%~fa"
set cp=%cp%;%TE_BASE%\resources
for /d %%a in ("%TE_BASE%\resources"\*) do call :addcomponent "%%a"
for /d %%a in ("%TE_BASE%\scripts"\*) do call :addscript "%%a"

set java="%JAVA_HOME%\bin\java.exe"
if not exist %java% set java=java
%java% -cp %cp% -Djava.protocol.handler.pkgs=com.occamlab.te.util.protocols %JAVA_OPTS% com.occamlab.te.Test -cmd=%0 %*
endlocal
goto:eof

:addcomponent
for %%b in (%1\*.jar) do call :addtocp "%%~fb"
goto:eof

:addscript
set cp=%cp%;%1\resources
goto:eof

:addtocp
set cp=%cp%;%1