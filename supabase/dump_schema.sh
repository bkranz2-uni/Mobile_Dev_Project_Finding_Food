#!/bin/sh
# Exports the database structure (tables, RLS policies, functions, indexes) to supabase/schema.sql.
# No data and no passwords are exported. pg_dump asks for the database password.
# Usage:  sh supabase/dump_schema.sh   (works from any folder)

cd "$(dirname "$0")/.." || exit 1

PG_DUMP=/opt/homebrew/opt/libpq/bin/pg_dump
DB_URL="postgresql://postgres.wnnctqopfttqoejvllsa@aws-1-eu-west-3.pooler.supabase.com:5432/postgres"

"$PG_DUMP" "$DB_URL" \
  --schema-only --schema=public --no-owner --no-privileges \
  -f supabase/schema.sql && echo "Done: supabase/schema.sql"
