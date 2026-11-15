#!/bin/bash
# Usage: prepare-neighborhoods-variant.sh QUICKSTART VARIANT QUICKSTART_DIR
# Copies the explicit move configuration of QUICKSTART into QUICKSTART_DIR
# and prints the Maven flag that selects VARIANT (selectors or neighborhoods).
set -euo pipefail

src="$(cd "$(dirname "$0")" && pwd)/neighborhoods-benchmark/$1"
case "$2" in
  selectors|neighborhoods) ;;
  *) echo "Unknown variant ($2); expected selectors or neighborhoods." >&2; exit 1 ;;
esac
[ -d "$src" ] || { echo "No configuration for quickstart ($1) in $src." >&2; exit 1; }

# Both variants get all files, so their builds differ only in the solver config XML.
pkg_dir=$(sed -n 's/^package \(.*\);/\1/p' "$src/BenchmarkNeighborhoodProvider.java" | tr . /)
cp "$src"/*-solverConfig.xml "$3/src/main/resources/"
cp "$src/BenchmarkNeighborhoodProvider.java" "$3/src/main/java/$pkg_dir/"
echo "-Dquarkus.timefold.solver-config-xml=$2-solverConfig.xml"
