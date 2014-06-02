:: Reads a CSV file (first argument) where each record contains two fields:
:: URL, local path name relative to TE_BASE/scripts
:: Examples:
:: http://svn.example.org/scripts/alpha/1.0.0/tags/r2,alpha/1.0.0
:: http://search.maven.org/remotecontent?filepath=org/example/beta/1.0/beta-1.0.zip,beta-1.0.zip
::
:: Note: PowerShell 3.0 or higher is required to download Maven artifacts.

@echo off
setlocal

set home=%~dp0cls
if exist "%home%setenv.bat" call "%home%setenv.bat"
if "%JAVA_HOME%"=="" echo JAVA_HOME must be set. & goto end

set csvfile=%~f1

for /F "usebackq tokens=1,2 delims=," %%a in ("%csvfile%") do (
  echo %%a | findstr -i -c:"filepath=" >nul && (
    powershell -Command "Invoke-WebRequest %%a -OutFile %TE_BASE%\scripts\%%b
  ) || (
    svn -q export %%a %TE_BASE%\scripts\%%b
  )
)

cd /d %TE_BASE%\scripts
for %%f in (*.zip) do ("%JAVA_HOME%"\bin\jar xf %%f & del %%f)

:end
