#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/honda-crv-2024-bot-rus}"
BRANCH="${BRANCH:-main}"

cd "${APP_DIR}"

git fetch --prune origin
git checkout "${BRANCH}"
git pull --ff-only origin "${BRANCH}"

docker compose build --pull bot
docker compose up -d --remove-orphans
docker compose ps
docker compose logs --tail=100 bot
