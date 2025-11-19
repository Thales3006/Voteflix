#!/usr/bin/env bash
set -euo pipefail

# delete_db.sh
# Deletes the SQLite database file used by the server.
# Usage: ./delete_db.sh [path-to-db]

DB_PATH="${1:-$(dirname "$0")/../data/voteflix.db}"

if [ -z "$DB_PATH" ]; then
  echo "Database path is empty" >&2
  exit 1
fi

if [ -f "$DB_PATH" ]; then
  echo "Deleting database: $DB_PATH"
  rm -f "$DB_PATH"
  echo "Deleted."
else
  echo "No database found at: $DB_PATH"
fi

exit 0
