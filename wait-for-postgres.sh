#!/bin/sh

host="$1"
port="$2"
shift 2

echo "⏳ Waiting for Postgres at $host:$port..."

until nc -z "$host" "$port"; do
  echo "🔁 Still waiting..."
  sleep 2
done

echo "✅ Postgres is up. Starting app..."
exec "$@"
