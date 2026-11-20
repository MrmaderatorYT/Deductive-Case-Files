#!/bin/sh
# Verifies all case files on a desktop JVM: unique solutions, engine solvability, complete texts.
set -e
cd "$(dirname "$0")/.."
OUT=$(mktemp -d)
javac -encoding UTF-8 -d "$OUT" app/src/main/java/com/ccs/deductive_case_files/game/*.java tools/Verifier.java
java -Dfile.encoding=UTF-8 -cp "$OUT" Verifier app/src/main/assets
rm -rf "$OUT"
