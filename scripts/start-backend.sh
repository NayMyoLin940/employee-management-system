#!/bin/zsh

set -e

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
PROJECT_ROOT="$(dirname -- "$SCRIPT_DIR")"
ENV_FILE="$PROJECT_ROOT/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing $ENV_FILE. Copy .env.example to .env and add your local database values."
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

required_variables=(POSTGRES_DB DB_URL DB_USERNAME DB_PASSWORD)
for variable_name in "${required_variables[@]}"; do
  if [[ -z "${(P)variable_name}" ]]; then
    echo "Missing required variable $variable_name in $ENV_FILE."
    exit 1
  fi
done

cd "$PROJECT_ROOT/backend"
exec ./mvnw spring-boot:run
