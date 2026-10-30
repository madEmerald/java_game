@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
if not exist build\classes mkdir build\classes
if not exist build\test-classes mkdir build\test-classes
(for /r src\main\java %%f in (*.java) do @echo "%%f") > build\main-sources.txt
javac -encoding UTF-8 --release 17 -Xlint:all -d build\classes @build\main-sources.txt
if errorlevel 1 exit /b 1
jar --create --file fantasy-strategy.jar --main-class ru.lab5.game.Main -C build\classes .
if errorlevel 1 exit /b 1
(for /r src\test\java %%f in (*.java) do @echo "%%f") > build\test-sources.txt
javac -encoding UTF-8 --release 17 -Xlint:all -cp build\classes -d build\test-classes @build\test-sources.txt
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -Dfile.encoding=UTF-8 -cp build\classes;build\test-classes ru.lab5.game.TestRunner
if errorlevel 1 exit /b 1
endlocal
