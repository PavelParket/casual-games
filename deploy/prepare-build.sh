#!/usr/bin/env bash
# Подготавливает common-utils для Docker-билда.
# Запускать ПЕРЕД docker build / docker compose build из корня проекта.
#
# Использование:
#   bash deploy/prepare-build.sh

set -euo pipefail

M2_SOURCE="${HOME}/.m2/repository/com/casualgames"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DEST="${SCRIPT_DIR}/../backend/.m2-common-utils"

if [ ! -d "$M2_SOURCE" ]; then
  echo "ERROR: $M2_SOURCE not found."
  echo ""
  echo "Run first (from backend/common-utils):"
  echo "  ./gradlew publishToMavenLocal"
  exit 1
fi

echo "-> Cleaning $DEST"
rm -rf "$DEST"
mkdir -p "$DEST"

echo "-> Copying common-utils artifacts from $M2_SOURCE"
cp -r "$M2_SOURCE"/* "$DEST/"

echo "OK. Ready to docker build."
