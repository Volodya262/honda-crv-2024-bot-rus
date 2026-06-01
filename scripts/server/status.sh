#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/honda-crv-2024-bot-rus}"

cd "${APP_DIR}"

docker compose ps
docker compose logs --tail=100 bot
