#!/usr/bin/env zsh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

: "${CLIENT1_USERNAME:=alice}"
: "${CLIENT2_USERNAME:=bob}"
: "${CLIENT_HOST:=localhost}"

if [[ ! -f .env && -f .env.example ]]; then
  cp .env.example .env
fi

if [[ -f .env ]]; then
  set -a
  source .env
  set +a
fi

: "${APP_SERVER_PORT:=8080}"

mkdir -p logs

echo "[1/4] Build project..."
mvn -q -DskipTests package

echo "[2/4] Build runtime classpath..."
mvn -q -DskipTests dependency:build-classpath -Dmdep.outputFile=target/classpath.txt
CLASSPATH="$(cat target/classpath.txt):target/classes"

typeset -a PIDS
cleanup() {
  echo "\nStopping local stack..."
  for pid in "${PIDS[@]:-}"; do
    kill "$pid" 2>/dev/null || true
  done
}
trap cleanup EXIT INT TERM

echo "[3/4] Start server on port ${APP_SERVER_PORT}..."
java -cp "$CLASSPATH" fr.baptiiiiste.Main server > logs/server.log 2>&1 &
PIDS+=("$!")

for _ in {1..30}; do
  if nc -z "$CLIENT_HOST" "$APP_SERVER_PORT" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

if ! nc -z "$CLIENT_HOST" "$APP_SERVER_PORT" >/dev/null 2>&1; then
  echo "Server did not start correctly. Check logs/server.log"
  exit 1
fi

echo "[4/4] Start 2 auto-logged clients (${CLIENT1_USERNAME}, ${CLIENT2_USERNAME})..."
java -cp "$CLASSPATH" fr.baptiiiiste.Main client "$CLIENT1_USERNAME" "$CLIENT_HOST" "$APP_SERVER_PORT" > logs/client1.log 2>&1 &
PIDS+=("$!")

java -cp "$CLASSPATH" fr.baptiiiiste.Main client "$CLIENT2_USERNAME" "$CLIENT_HOST" "$APP_SERVER_PORT" > logs/client2.log 2>&1 &
PIDS+=("$!")

echo "Stack ready."
echo "- Server log: logs/server.log"
echo "- Client logs: logs/client1.log, logs/client2.log"
echo "Press Ctrl+C to stop all launched processes."

wait

