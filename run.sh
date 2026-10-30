#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if [ ! -f fantasy-strategy.jar ]; then sh build.sh; fi
exec java -Dfile.encoding=UTF-8 -jar fantasy-strategy.jar "$@"
