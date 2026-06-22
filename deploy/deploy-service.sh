#!/usr/bin/env bash
# Rebuild and restart one or more services.
#
# Usage:
#   bash deploy/deploy-service.sh bank-service
#   bash deploy/deploy-service.sh bank-service user-service
#   bash deploy/deploy-service.sh bank-service --publish-common

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.prod.yaml"
COMMON_UTILS_DIR="$PROJECT_ROOT/backend/common-utils"

CYAN='\033[0;36m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
step() { echo -e "\n${CYAN}[$(date +%H:%M:%S)] $*${NC}"; }
ok()   { echo -e "${GREEN}  v $*${NC}"; }
warn() { echo -e "${YELLOW}  ! $*${NC}"; }
fail() { echo -e "${RED}  x $*${NC}"; exit 1; }

VALID_SERVICES=(
  "api-gateway" "security-service" "user-service"
  "bank-service" "game-service" "websocket-hub-service" "frontend"
)

# --- Parse arguments ---------------------------------------------------------
SERVICES=()
PUBLISH_COMMON=false

for arg in "$@"; do
  if [[ "$arg" == "--publish-common" ]]; then
    PUBLISH_COMMON=true
  else
    SERVICES+=("$arg")
  fi
done

if [ ${#SERVICES[@]} -eq 0 ]; then
  echo -e "${RED}Usage:${NC} $0 <service> [<service>...] [--publish-common]"
  echo -e "Valid services: ${VALID_SERVICES[*]}"
  exit 1
fi

# --- Validate service names --------------------------------------------------
for svc in "${SERVICES[@]}"; do
  if ! printf '%s\n' "${VALID_SERVICES[@]}" | grep -qx "$svc"; then
    fail "Unknown service: '$svc'. Valid: ${VALID_SERVICES[*]}"
  fi
done

echo -e "\n${CYAN}  casual-games - selective redeploy: ${SERVICES[*]}${NC}"

# --- Optional: republish common-utils ----------------------------------------
if [ "$PUBLISH_COMMON" = true ]; then
  step "publishToMavenLocal (common-utils)"
  cd "$COMMON_UTILS_DIR"
  ./gradlew publishToMavenLocal -q
  cd "$PROJECT_ROOT"
  bash "$SCRIPT_DIR/prepare-build.sh"
  ok "common-utils republished"
else
  warn "common-utils not republished. If changed, add --publish-common"
fi

# --- Build -------------------------------------------------------------------
step "Building: ${SERVICES[*]}"
cd "$PROJECT_ROOT"
docker compose -f "$COMPOSE_FILE" build "${SERVICES[@]}"
ok "built"

# --- Up ----------------------------------------------------------------------
step "Restarting: ${SERVICES[*]}"
docker compose -f "$COMPOSE_FILE" up -d "${SERVICES[@]}"
ok "restarted"

echo ""
docker compose -f "$COMPOSE_FILE" ps "${SERVICES[@]}"
