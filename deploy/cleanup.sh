#!/usr/bin/env bash
# Docker cleanup: dangling images + build cache + stopped containers.
# Running containers and volumes are NOT affected.
#
# Usage:
#   bash deploy/cleanup.sh          - safe cleanup (with confirmation)
#   bash deploy/cleanup.sh --force  - skip confirmation

set -euo pipefail

CYAN='\033[0;36m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
step() { echo -e "\n${CYAN}[$(date +%H:%M:%S)] $*${NC}"; }
ok()   { echo -e "${GREEN}  v $*${NC}"; }

FORCE=false
[[ "${1:-}" == "--force" ]] && FORCE=true

echo -e "\n${YELLOW}  Will be removed:${NC}"
echo "    * stopped containers"
echo "    * dangling images (untagged)"
echo "    * build cache"
echo -e "\n  ${YELLOW}Running containers and volumes are NOT affected.${NC}\n"

if [ "$FORCE" = false ]; then
  read -r -p "  Continue? [y/N] " CONFIRM
  [[ "$CONFIRM" =~ ^[yY]$ ]] || { echo "  Aborted."; exit 0; }
fi

step "Removing stopped containers"
docker container prune -f
ok "done"

step "Removing dangling images"
docker image prune -f
ok "done"

step "Removing build cache"
docker builder prune -f
ok "done"

step "Docker disk usage after cleanup"
docker system df
echo ""
