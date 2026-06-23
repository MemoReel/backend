#!/usr/bin/env bash
#
# MemoReel backend 자동 배포 스크립트.
# cron이 5분마다 호출하며, origin/main에 새 커밋이 있을 때만
# Docker Compose 환경을 재빌드 후 재기동한다.

set -euo pipefail

export PATH="/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

PROJECT_DIR="/home/ubuntu/backend"
LOCK_FILE="/tmp/memoreel-auto-deploy.lock"

log() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

# 동시 실행 방지: 락 획득 실패 시 즉시 종료 (다음 cron이 다시 시도)
exec 9>"$LOCK_FILE"
if ! flock -n 9; then
  log "skip: another auto-deploy is in progress"
  exit 0
fi

cd "$PROJECT_DIR"

log "fetch: origin/main"
git fetch origin main

LOCAL_SHA="$(git rev-parse HEAD)"
REMOTE_SHA="$(git rev-parse origin/main)"

if [ "$LOCAL_SHA" = "$REMOTE_SHA" ]; then
  log "no change: HEAD=$LOCAL_SHA"
  exit 0
fi

log "update detected: $LOCAL_SHA -> $REMOTE_SHA"

log "git pull --ff-only"
git pull --ff-only origin main

log "docker compose down"
docker compose down

log "docker compose build --no-cache app"
docker compose build --no-cache app

log "docker compose up -d"
docker compose up -d

log "docker image prune -f"
docker image prune -f

log "deploy success: now at $(git rev-parse HEAD)"
