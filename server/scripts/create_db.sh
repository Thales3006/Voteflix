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
sqlite3 -column -header "$DB_PATH" <<'SQL'
SELECT id, username, is_admin FROM users;
SELECT id, title, year FROM movies;
SELECT id, name FROM genres;
SELECT mg.movie_id, m.title AS movie, g.name AS genre
  FROM movie_genres mg
  JOIN movies m ON m.id = mg.movie_id
  JOIN genres g ON g.id = mg.genre_id
  ORDER BY mg.movie_id;
SELECT r.id, m.title AS movie, u.username, r.rating, r.title AS review_title
  FROM reviews r
  JOIN movies m ON m.id = r.movie_id
  JOIN users u ON u.id = r.user_id;
SQL

echo "Checking integrity..."
INTEGRITY=$(sqlite3 "$DB_PATH" "PRAGMA integrity_check;")
if [ "$INTEGRITY" = "ok" ]; then
  echo "Integrity check: ok"
else
  echo "Integrity check: FAILED" >&2
  echo "$INTEGRITY" >&2
  exit 4
fi

FK_VIOLATIONS=$(sqlite3 "$DB_PATH" "PRAGMA foreign_key_check;")
if [ -z "$FK_VIOLATIONS" ]; then
  echo "Foreign key check: ok"
else
  echo "Foreign key violations found:" >&2
  echo "$FK_VIOLATIONS" >&2
  exit 5
fi

echo "Done"
exit 0
