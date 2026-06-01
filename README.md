# honda-crv-2024-bot-rus

This project is a Telegram bot that gives answers about Honda CRV 2024 
using OpenAI API and car owners manual.

## Local Docker run

Create `.env` from `.env.example` and fill in the secrets:

```bash
cp .env.example .env
```

Start the bot:

```bash
docker compose up -d --build
```

Check logs:

```bash
docker compose logs -f bot
```

## Ubuntu VPS deployment

The deployment target is a small Ubuntu VPS running Docker Compose. Server-side
commands are kept in scripts under `scripts/server`.

First server setup:

```bash
REPO_URL=git@github.com:OWNER/honda-crv-2024-bot-rus.git BRANCH=main sudo -E bash scripts/server/bootstrap-ubuntu.sh
```

The bootstrap script installs Docker, clones or updates the repository in
`/opt/honda-crv-2024-bot-rus`, creates `.env` from `.env.example` if needed,
and leaves it with `600` permissions.

Edit `/opt/honda-crv-2024-bot-rus/.env` on the server and fill in:

```bash
TELEGRAM_BOT_TOKEN=
OPENAI_API_KEY=
OPENAI_VECTOR_STORE_ID=
```

Deploy or update after secrets are in place:

```bash
sudo APP_DIR=/opt/honda-crv-2024-bot-rus BRANCH=main bash /opt/honda-crv-2024-bot-rus/scripts/server/deploy.sh
```

Useful checks:

```bash
sudo APP_DIR=/opt/honda-crv-2024-bot-rus bash /opt/honda-crv-2024-bot-rus/scripts/server/status.sh
sudo APP_DIR=/opt/honda-crv-2024-bot-rus bash /opt/honda-crv-2024-bot-rus/scripts/server/logs.sh
```
