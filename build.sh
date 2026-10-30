#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes build/test-classes
find src/main/java -name '*.java' | sort > build/main-sources.txt
javac -encoding UTF-8 --release 17 -Xlint:all -d build/classes @build/main-sources.txt
jar --create --file fantasy-strategy.jar --main-class ru.lab5.game.Main -C build/classes .
find src/test/java -name '*.java' | sort > build/test-sources.txt
javac -encoding UTF-8 --release 17 -Xlint:all -cp build/classes -d build/test-classes @build/test-sources.txt
java -Djava.awt.headless=true -Dfile.encoding=UTF-8 -cp build/classes:build/test-classes ru.lab5.game.TestRunner
