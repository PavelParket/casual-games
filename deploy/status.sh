#!/usr/bin/env bash
# Status of all containers, resources, disk.
#
# Usage: bash deploy/status.sh

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.prod.yaml"

CYAN='\033[0;36m'; YELLOW='\033[1;33m'; NC='\033[0m'
section() { echo -e "\n${YELLOW}> $*${NC}"; }

echo -e "\n${CYAN}  casual-games - system status  $(date '+%Y-%m-%d %H:%M:%S')${NC}"

section "Containers"
docker compose -f "$COMPOSE_FILE" ps

section "Resource usage (CPU / Memory)"
docker stats --no-stream \
  --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"

section "Disk - root filesystem"
df -h / | awk 'NR==2 {printf "  Used: %s / %s  (%s)\n", $3, $2, $5}'

section "Disk - Docker"
docker system df

echo ""
