#!/usr/bin/env bash
# Full redeploy on VPS by immutable git tag.
#
# Usage:
#   bash deploy/deploy.sh v1.4.1

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
[[ -z "$TAG" ]]    && fail "Usage: bash deploy/deploy.sh <tag>  (e.g. v1.4.1)"
[[ "$TAG" != v* ]] && fail "Tag must start with 'v'  (e.g. v1.4.1)"

# Derive image version from tag: v1.4.1 → 1.4.1
APP_VERSION="${TAG#v}"
export APP_VERSION

echo -e "\n${CYAN}  casual-games — deploying ${TAG} (images: ${APP_VERSION})${NC}"

# --- 1. Checkout tag ---------------------------------------------------------
step "1/3  git checkout ${TAG}"
cd "$PROJECT_ROOT"
git fetch --tags
git checkout "$TAG"
ok "on ${TAG}"

# --- 2. docker compose pull ---------------------------------------------------
step "2/3  Pulling images"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" pull
ok "images pulled"

# --- 3. up -d ----------------------------------------------------------------
step "3/3  Starting services"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d --remove-orphans
ok "services started"

# --- Status ------------------------------------------------------------------
echo ""
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps
echo ""
echo -e "${GREEN}  Done. Running: ${TAG}${NC}"