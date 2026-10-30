@echo off
setlocal
cd /d "%~dp0"
if not exist fantasy-strategy.jar call build.bat
if errorlevel 1 exit /b 1
java -Dfile.encoding=UTF-8 -jar fantasy-strategy.jar %*
endlocal
