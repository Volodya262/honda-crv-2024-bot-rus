#!/usr/bin/env bash
set -Eeuo pipefail

APP_DIR="${APP_DIR:-/opt/honda-crv-2024-bot-rus}"
REPO_URL="${REPO_URL:-}"
BRANCH="${BRANCH:-main}"

if [[ -z "${REPO_URL}" ]]; then
  echo "REPO_URL is required, for example:"
  echo "  REPO_URL=git@github.com:owner/honda-crv-2024-bot-rus.git sudo -E bash scripts/server/bootstrap-ubuntu.sh"
  exit 1
fi

if [[ "${EUID}" -ne 0 ]]; then
  echo "Run this script as root or with sudo."
  exit 1
fi

apt-get update
apt-get install -y ca-certificates curl git gnupg

install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

. /etc/os-release
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu ${VERSION_CODENAME} stable" \
  > /etc/apt/sources.list.d/docker.list

apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

systemctl enable --now docker

mkdir -p "$(dirname "${APP_DIR}")"
if [[ ! -d "${APP_DIR}/.git" ]]; then
  git clone --branch "${BRANCH}" "${REPO_URL}" "${APP_DIR}"
fi

cd "${APP_DIR}"
git fetch --prune origin
git checkout "${BRANCH}"
git pull --ff-only origin "${BRANCH}"

if [[ ! -f .env ]]; then
  cp .env.example .env
  chmod 600 .env
  echo "Created ${APP_DIR}/.env. Fill it with production secrets, then run scripts/server/deploy.sh."
else
  chmod 600 .env
fi
