#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$ROOT_DIR/.env"
ENV_EXAMPLE_FILE="$ROOT_DIR/.env.example"

BACKEND_DIR="$ROOT_DIR/backend/hotel-api"
FRONTEND_DIR="$ROOT_DIR/frontend"

BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-3000}"
RESET_DB=false
LOAD_EXTRA_SEED=false

function usage() {
  cat <<'EOF'
Usage: ./run.sh [--reset] [--seed]

  --reset   Drop and recreate the local MySQL demo database before startup
  --seed    Load richer optional demo seed data after the baseline demo data
EOF
}

function print_info() {
  printf "\n[hotel-app] %s\n" "$1"
}

function require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "[hotel-app] Missing required command: $1"; exit 1; }
}

function is_port_in_use() {
  lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1
}

function next_free_port() {
  local port="$1"
  while is_port_in_use "$port"; do port=$((port + 1)); done
  echo "$port"
}

function prepare_env() {
  if [[ ! -f "$ENV_FILE" ]]; then
    print_info ".env not found. Creating from .env.example"
    cp "$ENV_EXAMPLE_FILE" "$ENV_FILE"
  fi
  set -a
  # shellcheck source=/dev/null
  source "$ENV_FILE"
  set +a
}

function prepare_db() {
  local db_user="${SPRING_DATASOURCE_USERNAME:-root}"
  local db_pass="${SPRING_DATASOURCE_PASSWORD:-sujith@2204$}"
  local db_name="${MYSQL_DATABASE:-hotel_booking}"

  if [[ "$RESET_DB" == true ]]; then
    print_info "Resetting database $db_name"
    MYSQL_PWD="$db_pass" mysql -u "$db_user" -e "DROP DATABASE IF EXISTS \`$db_name\`; CREATE DATABASE \`$db_name\`;"
  else
    print_info "Ensuring database $db_name exists"
    MYSQL_PWD="$db_pass" mysql -u "$db_user" -e "CREATE DATABASE IF NOT EXISTS \`$db_name\`;"
  fi

  print_info "Loading baseline demo schema and seed data"
  MYSQL_PWD="$db_pass" mysql -u "$db_user" "$db_name" < "$BACKEND_DIR/scripts/mysql-init-demo.sql"

  if [[ "$LOAD_EXTRA_SEED" == true ]]; then
    print_info "Loading richer optional demo seed data"
    MYSQL_PWD="$db_pass" mysql -u "$db_user" "$db_name" < "$BACKEND_DIR/scripts/mysql-seed-more-data.sql"
  fi
}

function start_backend() {
  if is_port_in_use "$BACKEND_PORT"; then
    echo "[hotel-app] Port $BACKEND_PORT is already in use. Stop the existing process or set BACKEND_PORT."
    exit 1
  fi
  print_info "Starting backend on port $BACKEND_PORT"
  (
    cd "$BACKEND_DIR"
    SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:mysql://localhost:3306/hotel_booking?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}" \
    SPRING_FLYWAY_ENABLED="${SPRING_FLYWAY_ENABLED:-false}" \
    SPRING_JPA_HIBERNATE_DDL_AUTO="${SPRING_JPA_HIBERNATE_DDL_AUTO:-update}" \
    PORT="$BACKEND_PORT" \
    ./mvnw spring-boot:run
  ) > "$ROOT_DIR/.backend.log" 2>&1 &
  BACKEND_PID=$!
}

function wait_backend() {
  print_info "Waiting for backend health"
  for _ in {1..90}; do
    if curl -s "http://localhost:${BACKEND_PORT}/api/hotels" >/dev/null 2>&1; then
      print_info "Backend is up"
      return
    fi
    sleep 1
  done
  echo "[hotel-app] Backend failed to start. Check .backend.log"
  exit 1
}

function start_frontend() {
  local requested="$FRONTEND_PORT"
  FRONTEND_PORT="$(next_free_port "$FRONTEND_PORT")"
  if [[ "$requested" != "$FRONTEND_PORT" ]]; then
    print_info "Port $requested is busy, using frontend port $FRONTEND_PORT"
  fi
  print_info "Starting frontend on port $FRONTEND_PORT"
  (
    cd "$FRONTEND_DIR"
    python3 -m http.server "$FRONTEND_PORT"
  ) > "$ROOT_DIR/.frontend.log" 2>&1 &
  FRONTEND_PID=$!
}

function cleanup() {
  print_info "Stopping services"
  [[ -n "${BACKEND_PID:-}" ]] && kill "$BACKEND_PID" >/dev/null 2>&1 || true
  [[ -n "${FRONTEND_PID:-}" ]] && kill "$FRONTEND_PID" >/dev/null 2>&1 || true
}

require_cmd mysql
require_cmd python3
require_cmd curl
require_cmd lsof

while [[ $# -gt 0 ]]; do
  case "$1" in
    --reset)
      RESET_DB=true
      ;;
    --seed)
      LOAD_EXTRA_SEED=true
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "[hotel-app] Unknown option: $1"
      usage
      exit 1
      ;;
  esac
  shift
done

prepare_env
prepare_db
trap cleanup EXIT INT TERM
start_backend
wait_backend
start_frontend

print_info "Application started"
echo "Frontend: http://localhost:${FRONTEND_PORT}"
echo "Backend : http://localhost:${BACKEND_PORT}"
echo "Press Ctrl+C to stop both services"
wait
