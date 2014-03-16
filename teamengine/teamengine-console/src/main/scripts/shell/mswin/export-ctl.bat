:: Reads a CSV file (first argument) where each record contains two fields:
:: Subversion tag URL, local path name relative to TE_BASE/scripts
:: Example:
:: http://svn.example.org/scripts/alpha/1.0.0/trunk,alpha/1.0.0

@echo off
setlocal

set home=%~dp0
if exist "%home%setenv.bat" call "%home%setenv.bat"

set csvfile=%~f1

for /F "usebackq tokens=1,2 delims=," %%a in ("%csvfile%") do (
  svn -q export %%a %TE_BASE%\scripts\%%b
)
