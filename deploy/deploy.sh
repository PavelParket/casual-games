#!/usr/bin/env bash
# Full redeploy on VPS:
#   git pull -> publishToMavenLocal -> prepare-build -> docker compose build -> up -d
#
# Usage:
#   bash deploy/deploy.sh
#   bash deploy/deploy.sh --skip-publish   # if common-utils was not changed

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.prod.yaml"
COMMON_UTILS_DIR="$PROJECT_ROOT/backend/common-utils"

# --- Colors ------------------------------------------------------------------
CYAN='\033[0;36m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
step() { echo -e "\n${CYAN}[$(date +%H:%M:%S)] $*${NC}"; }
ok()   { echo -e "${GREEN}  v $*${NC}"; }
warn() { echo -e "${YELLOW}  ! $*${NC}"; }
fail() { echo -e "${RED}  x $*${NC}"; exit 1; }

# --- Flags -------------------------------------------------------------------
SKIP_PUBLISH=false
for arg in "$@"; do
  [[ "$arg" == "--skip-publish" ]] && SKIP_PUBLISH=true
done

echo -e "\n${CYAN}  casual-games - full redeploy${NC}"

# --- 1. git pull -------------------------------------------------------------
step "1/5  git pull"
cd "$PROJECT_ROOT"
git pull
ok "up to date"

# --- 2. publishToMavenLocal --------------------------------------------------
if [ "$SKIP_PUBLISH" = false ]; then
  step "2/5  publishToMavenLocal (common-utils)"
  cd "$COMMON_UTILS_DIR"
  ./gradlew publishToMavenLocal -q
  cd "$PROJECT_ROOT"
  ok "published"
else
  warn "Step 2 skipped (--skip-publish)"
fi

# --- 3. prepare-build --------------------------------------------------------
step "3/5  Preparing build context"
bash "$SCRIPT_DIR/prepare-build.sh"

# --- 4. docker compose build -------------------------------------------------
step "4/5  Building images"
cd "$PROJECT_ROOT"
docker compose -f "$COMPOSE_FILE" build
ok "images built"

# --- 5. up -d ----------------------------------------------------------------
step "5/5  Starting services"
docker compose -f "$COMPOSE_FILE" up -d --remove-orphans
ok "services started"

# --- Status ------------------------------------------------------------------
echo ""
docker compose -f "$COMPOSE_FILE" ps
echo ""
echo -e "${GREEN}  Done.${NC}"
