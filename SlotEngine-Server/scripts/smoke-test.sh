#!/usr/bin/env bash

set -Eeuo pipefail

readonly API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"
readonly SMOKE_PASSWORD="SmokePassword-42!"
readonly SMOKE_EMAIL="smoke-${GITHUB_RUN_ID:-local}-${GITHUB_RUN_ATTEMPT:-1}-${RANDOM}@example.com"

response_body="$(mktemp)"
cookie_jar="$(mktemp)"

cleanup() {
  rm -f "${response_body}" "${cookie_jar}"
}
trap cleanup EXIT

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Required command is unavailable: $1" >&2
    exit 1
  fi
}

request() {
  local expected_status="$1"
  shift

  local actual_status
  actual_status="$(curl \
    --silent \
    --show-error \
    --connect-timeout 5 \
    --max-time 30 \
    --output "${response_body}" \
    --write-out "%{http_code}" \
    "$@")"

  if [[ "${actual_status}" != "${expected_status}" ]]; then
    echo "Expected HTTP ${expected_status}, received ${actual_status}" >&2
    cat "${response_body}" >&2
    exit 1
  fi
}

for command_name in curl docker jq; do
  require_command "${command_name}"
done

cd "$(dirname "${BASH_SOURCE[0]}")/../.."

echo "[smoke] health"
request 200 "${API_BASE_URL}/health"
jq -e '.status == "UP"' "${response_body}" >/dev/null

echo "[smoke] register user"
register_body="$(jq -n \
  --arg name "Smoke User" \
  --arg email "${SMOKE_EMAIL}" \
  --arg password "${SMOKE_PASSWORD}" \
  '{name: $name, email: $email, password: $password}')"
request 201 \
  --header "Content-Type: application/json" \
  --request POST \
  --data "${register_body}" \
  "${API_BASE_URL}/api/auth/register"

echo "[smoke] create booking fixture"
slot_id="$(docker compose exec --no-TTY postgres sh -c \
  'PGPASSWORD="$POSTGRES_PASSWORD" psql --host localhost --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" --no-psqlrc --quiet --tuples-only --no-align --set ON_ERROR_STOP=1' <<'SQL'
WITH smoke_resource AS (
    INSERT INTO resources (name, description)
    VALUES ('CI Smoke Resource', 'Temporary smoke-test fixture')
    RETURNING id
)
INSERT INTO slots (resource_id, slot_start, slot_end)
SELECT
    id,
    CURRENT_TIMESTAMP + INTERVAL '1 day',
    CURRENT_TIMESTAMP + INTERVAL '1 day 30 minutes'
FROM smoke_resource
RETURNING id;
SQL
)"
slot_id="$(echo "${slot_id}" | tr -d '[:space:]')"

if [[ ! "${slot_id}" =~ ^[0-9]+$ ]]; then
  echo "Smoke fixture did not return a valid slot ID: ${slot_id}" >&2
  exit 1
fi

echo "[smoke] login"
login_body="$(jq -n \
  --arg email "${SMOKE_EMAIL}" \
  --arg password "${SMOKE_PASSWORD}" \
  '{email: $email, password: $password}')"
request 200 \
  --cookie "${cookie_jar}" \
  --cookie-jar "${cookie_jar}" \
  --header "Content-Type: application/json" \
  --request POST \
  --data "${login_body}" \
  "${API_BASE_URL}/api/auth/login"
jq -e '.role == "USER"' "${response_body}" >/dev/null

echo "[smoke] refresh"
request 204 \
  --cookie "${cookie_jar}" \
  --cookie-jar "${cookie_jar}" \
  --request POST \
  "${API_BASE_URL}/api/auth/refresh"

echo "[smoke] book slot"
request 201 \
  --cookie "${cookie_jar}" \
  --cookie-jar "${cookie_jar}" \
  --request POST \
  "${API_BASE_URL}/api/slots/${slot_id}/book"
jq -e --argjson slot_id "${slot_id}" \
  '.slotId == $slot_id and .status == "CONFIRMED"' \
  "${response_body}" >/dev/null

echo "[smoke] logout"
request 204 \
  --cookie "${cookie_jar}" \
  --cookie-jar "${cookie_jar}" \
  --request POST \
  "${API_BASE_URL}/api/auth/logout"

echo "[smoke] verify refresh token was revoked"
request 401 \
  --cookie "${cookie_jar}" \
  --cookie-jar "${cookie_jar}" \
  --request POST \
  "${API_BASE_URL}/api/auth/refresh"

echo "[smoke] passed"
