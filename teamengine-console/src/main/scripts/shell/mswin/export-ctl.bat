:: Reads a CSV file (first argument) where each record contains two fields:
:: Subversion tag URL, local path name relative to TE_BASE/scripts
:: Example:
:: http://svn.example.org/scripts/alpha/1.0.0/tags/r2,alpha/1.0.0

@echo off
setlocal

set home=%~dp0
if exist "%home%setenv.bat" call "%home%setenv.bat"
if "%JAVA_HOME%"=="" echo JAVA_HOME must be set. & goto end

set csvfile=%~f1

for /F "usebackq tokens=1,2 delims=," %%a in ("%csvfile%") do (
  svn -q export %%a %TE_BASE%\scripts\%%b
)

cd /d %TE_BASE%\scripts
for %%f in (*.zip) do ("%JAVA_HOME%"\bin\jar xf %%f & del %%f)

:end
