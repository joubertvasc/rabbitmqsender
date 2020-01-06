#!/usr/bin/env sh

./gradlew fatJar
echo O executável está em build/libs!
cd build/libs
ls -la
