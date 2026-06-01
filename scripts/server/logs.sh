#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/honda-crv-2024-bot-rus}"
TAIL="${TAIL:-200}"

cd "${APP_DIR}"

docker compose logs --tail="${TAIL}" -f bot
