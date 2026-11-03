#!/usr/bin/env bash
set -euo pipefail
repo="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
dc() { docker compose -f "$repo/compose.yaml" "$@"; }
action="${1:-up}"
if (($#)); then shift; fi
require_path() {
  [[ "${1:-}" =~ ^practice/[a-zA-Z0-9_./-]+$ && "$1" != *..* ]] || { echo 'Нужен путь practice/... без ..' >&2; exit 2; }
}
case "$action" in
  up) dc up -d --wait db ;;
  reset) dc up -d --wait db; dc exec -T db psql -X -U plm_owner -d plm -v ON_ERROR_STOP=1 -f /workspace/practice/postgresql/schema/reset.sql ;;
  sql) require_path "${1:-}"; dc exec -T db psql -X -U plm_owner -d plm -v ON_ERROR_STOP=1 -f "/workspace/$1" ;;
  psql) dc exec db psql -X -U plm_owner -d plm ;;
  python) require_path "${1:-}"; dc run --rm python "$@" ;;
  java) require_path "${1:-}"; dc build java; dc run --rm java "$@" ;;
  backup) dc exec -T db pg_dump -U plm_owner -d plm -Fc -f /artifacts/plm.dump ;;
  restore) dc exec -T db dropdb -U plm_owner --if-exists plm_restore; dc exec -T db createdb -U plm_owner -T template0 plm_restore; dc exec -T db pg_restore -U plm_owner -d plm_restore --exit-on-error /artifacts/plm.dump ;;
  down) dc down --remove-orphans ;;
  clean)
    dc --profile tools down --volumes --remove-orphans --rmi local
    output="$repo/practice/output"
    [[ "$output" == "$repo/practice/output" && -d "$output" ]] || exit 2
    find "$output" -mindepth 1 -maxdepth 1 ! -name .gitkeep -exec rm -rf -- {} + ;;
  *) echo 'Команды: up reset sql psql python java backup restore down clean' >&2; exit 2 ;;
esac
