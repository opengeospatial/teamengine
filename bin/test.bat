@echo off
setlocal
set home=%~dp0
if exist %home%setenv.bat call %home%setenv.bat
set cp=%home%..\apps\engine\resources
for %%a in (%home%..\apps\engine\lib\*.jar) do call :addtocp %%~fa
for /d %%a in (%home%..\components\*) do call :addcomponent %%a
set java="%JAVA_HOME%\bin\java.exe"
if not exist %java% set java=java
%java% -cp %cp% %JAVA_OPTS% com.occamlab.te.Test -cmd=%0 %*
endlocal
goto:eof

:addcomponent
set cp=%cp%;%1\resources
for %%b in (%1\lib\*.jar) do call :addtocp %%~fb
goto:eof

:addtocp
set cp=%cp%;%1
