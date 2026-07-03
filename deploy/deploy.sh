#!/usr/bin/env bash
# Full redeploy on VPS by immutable git tag.
#
# Usage:
#   bash deploy/deploy.sh v1.4.0

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.prod.yaml"
ENV_FILE="$SCRIPT_DIR/prod.env"

# --- Colors ------------------------------------------------------------------
CYAN='\033[0;36m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
step() { echo -e "\n${CYAN}[$(date +%H:%M:%S)] $*${NC}"; }
ok()   { echo -e "${GREEN}  v $*${NC}"; }
fail() { echo -e "${RED}  x $*${NC}"; exit 1; }

# --- Validate tag argument ---------------------------------------------------
TAG="${1:-}"
[[ -z "$TAG" ]]    && fail "Usage: bash deploy/deploy.sh <tag>  (e.g. v1.4.0)"
[[ "$TAG" != v* ]] && fail "Tag must start with 'v'  (e.g. v1.4.0)"

# Derive starters version from tag: v1.4.0 → 1.4.0
COMMON_UTILS_VERSION="${TAG#v}"

# --- Read GPR credentials from prod.env --------------------------------------
GPR_USER=$(grep '^GPR_USER='  "$ENV_FILE" | cut -d= -f2-)
GPR_TOKEN=$(grep '^GPR_TOKEN=' "$ENV_FILE" | cut -d= -f2-)

[[ -z "$GPR_USER"  ]] && fail "GPR_USER not set in prod.env"
[[ -z "$GPR_TOKEN" ]] && fail "GPR_TOKEN not set in prod.env"

echo -e "\n${CYAN}  casual-games — deploying ${TAG} (starters: ${COMMON_UTILS_VERSION})${NC}"

# --- 1. Checkout tag ---------------------------------------------------------
step "1/3  git checkout ${TAG}"
cd "$PROJECT_ROOT"
git fetch --tags
git checkout "$TAG"
ok "on ${TAG}"

# --- 2. docker compose build -------------------------------------------------
step "2/3  Building images"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build \
  --build-arg COMMON_UTILS_VERSION="$COMMON_UTILS_VERSION" \
  --build-arg GPR_USER="$GPR_USER" \
  --build-arg GPR_TOKEN="$GPR_TOKEN"
ok "images built"

# --- 3. up -d ----------------------------------------------------------------
step "3/3  Starting services"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --remove-orphans
ok "services started"

# --- Status ------------------------------------------------------------------
echo ""
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps
echo ""
echo -e "${GREEN}  Done. Running: ${TAG}${NC}"
