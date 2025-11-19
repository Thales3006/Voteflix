#!/usr/bin/env bash
set -euo pipefail

# create_db.sh
# Creates the SQLite database and runs SQL scripts to build schema and populate data.
# Usage: ./create_db.sh [path-to-db] [sql-dir]

DB_PATH="${1:-$(dirname "$0")/../data/voteflix.db}"
SQL_DIR="${2:-$(dirname "$0")/../src/main/resources}"

echo "DB path: $DB_PATH"
echo "SQL dir: $SQL_DIR"

mkdir -p "$(dirname "$DB_PATH")"

if [ -f "$DB_PATH" ]; then
  echo "Database already exists at $DB_PATH"
  echo "To recreate from scratch, run delete_db.sh first or remove the file manually."
  exit 1
fi

if ! command -v sqlite3 >/dev/null 2>&1; then
  echo "sqlite3 not found in PATH" >&2
  exit 2
fi

CREATE_SQL="$SQL_DIR/create_tables.sql"
POPULATE_SQL="$SQL_DIR/populate_database.sql"

if [ ! -f "$CREATE_SQL" ]; then
  echo "Missing $CREATE_SQL" >&2
  exit 3
fi

echo "Creating database and schema..."
sqlite3 "$DB_PATH" < "$CREATE_SQL"

if [ -f "$POPULATE_SQL" ]; then
  echo "Populating database..."
  sqlite3 "$DB_PATH" < "$POPULATE_SQL"
else
  echo "No populate script found at $POPULATE_SQL — skipping population"
fi

echo "Verifying contents..."
sqlite3 -column "$DB_PATH" "SELECT 'USERS' as section; SELECT id,username,is_admin FROM users; SELECT 'MOVIES' as section; SELECT id,title,year FROM movies; SELECT 'GENRES' as section; SELECT id,name FROM genres; SELECT 'MOVIE_GENRES' as section; SELECT * FROM movie_genres;"

echo "Done"
exit 0
