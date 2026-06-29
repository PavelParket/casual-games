#!/usr/bin/env bash
# Rebuild and restart one or more services on the current checked-out tag.
# Run deploy.sh first to switch to the right tag.
#
# Usage:
#   bash deploy/deploy-service.sh bank-service
#   bash deploy/deploy-service.sh bank-service user-service

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.prod.yaml"
ENV_FILE="$SCRIPT_DIR/prod.env"

CYAN='\033[0;36m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
step() { echo -e "\n${CYAN}[$(date +%H:%M:%S)] $*${NC}"; }
ok()   { echo -e "${GREEN}  v $*${NC}"; }
fail() { echo -e "${RED}  x $*${NC}"; exit 1; }

VALID_SERVICES=(
  "api-gateway" "security-service" "user-service"
  "bank-service" "game-service" "websocket-hub-service" "frontend"
)

# --- Derive version from current HEAD tag ------------------------------------
# Fails if HEAD is not on an exact tag — prevents deploying untagged code.
TAG=$(git -C "$PROJECT_ROOT" describe --tags --exact-match 2>/dev/null || true)
[[ -z "$TAG" ]] && \
  fail "HEAD is not on a release tag. Checkout a tag first: bash deploy/deploy.sh v1.4.0"
COMMON_UTILS_VERSION="${TAG#v}"

# --- Read GPR credentials from prod.env --------------------------------------
GPR_USER=$(grep '^GPR_USER='  "$ENV_FILE" | cut -d= -f2-)
GPR_TOKEN=$(grep '^GPR_TOKEN=' "$ENV_FILE" | cut -d= -f2-)

[[ -z "$GPR_USER"  ]] && fail "GPR_USER not set in prod.env"
[[ -z "$GPR_TOKEN" ]] && fail "GPR_TOKEN not set in prod.env"

# --- Parse and validate service arguments ------------------------------------
SERVICES=()
for arg in "$@"; do
  SERVICES+=("$arg")
done

if [ ${#SERVICES[@]} -eq 0 ]; then
  echo -e "${RED}Usage:${NC} $0 <service> [<service>...]"
  echo -e "Valid services: ${VALID_SERVICES[*]}"
  exit 1
fi

for svc in "${SERVICES[@]}"; do
  if ! printf '%s\n' "${VALID_SERVICES[@]}" | grep -qx "$svc"; then
    fail "Unknown service: '$svc'. Valid: ${VALID_SERVICES[*]}"
  fi
done

echo -e "\n${CYAN}  casual-games — selective redeploy: ${SERVICES[*]} (${TAG})${NC}"

# --- Build -------------------------------------------------------------------
step "Building: ${SERVICES[*]}"
cd "$PROJECT_ROOT"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" build \
  --build-arg COMMON_UTILS_VERSION="$COMMON_UTILS_VERSION" \
  --build-arg GPR_USER="$GPR_USER" \
  --build-arg GPR_TOKEN="$GPR_TOKEN" \
  "${SERVICES[@]}"
ok "built"

# --- Up ----------------------------------------------------------------------
step "Restarting: ${SERVICES[*]}"
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" up -d "${SERVICES[@]}"
ok "restarted"

echo ""
docker compose -f "$COMPOSE_FILE" --env-file "$ENV_FILE" ps "${SERVICES[@]}"
