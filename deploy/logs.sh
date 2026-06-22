#!/usr/bin/env bash
# View logs for a specific service.
#
# Usage:
#   bash deploy/logs.sh bank-service          - follow (real-time, default)
#   bash deploy/logs.sh bank-service 100      - last 100 lines (static)
#   bash deploy/logs.sh bank-service -f       - follow (explicit)

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.prod.yaml"

RED='\033[0;31m'; CYAN='\033[0;36m'; NC='\033[0m'
fail() { echo -e "${RED}  x $*${NC}"; exit 1; }

ALL_SERVICES=(
  "api-gateway" "security-service" "user-service"
  "bank-service" "game-service" "websocket-hub-service"
  "frontend" "nginx" "postgres" "redis" "kafka" "minio"
)

SERVICE="${1:-}"
MODE="${2:--f}"

if [ -z "$SERVICE" ]; then
  echo -e "${RED}Usage:${NC} $0 <service> [lines|-f]"
  echo -e "Services: ${ALL_SERVICES[*]}"
  exit 1
fi

if ! printf '%s\n' "${ALL_SERVICES[@]}" | grep -qx "$SERVICE"; then
  fail "Unknown service: '$SERVICE'"
fi

if [[ "$MODE" == "-f" || "$MODE" == "--follow" ]]; then
  echo -e "${CYAN}  Following $SERVICE (Ctrl+C to stop)...${NC}\n"
  docker compose -f "$COMPOSE_FILE" logs -f --tail=100 "$SERVICE"
elif [[ "$MODE" =~ ^[0-9]+$ ]]; then
  docker compose -f "$COMPOSE_FILE" logs --tail="$MODE" "$SERVICE"
else
  fail "Second arg must be a number or -f"
fi
