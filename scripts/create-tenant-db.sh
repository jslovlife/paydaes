#!/usr/bin/env bash

set -euo pipefail

CONTAINER="paydaes-mysql-corehr"
ROOT_PASSWORD="rootpassword"
DB_NAME=""
DB_USER=""
DB_PASSWORD=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --db-name)      DB_NAME="$2";     shift 2 ;;
        --db-user)      DB_USER="$2";     shift 2 ;;
        --db-password)  DB_PASSWORD="$2"; shift 2 ;;
        *)
            echo "ERROR: Unknown option '$1'" >&2
            exit 1
            ;;
    esac
done

MISSING=()
[[ -z "$DB_NAME"     ]] && MISSING+=("--db-name")
[[ -z "$DB_USER"     ]] && MISSING+=("--db-user")
[[ -z "$DB_PASSWORD" ]] && MISSING+=("--db-password")

if [[ ${#MISSING[@]} -gt 0 ]]; then
    echo "ERROR: Missing required option(s): ${MISSING[*]}" >&2
    echo "Usage: $0 --db-name <name> --db-user <user> --db-password <pass>"
    exit 1
fi

run_sql() {
    docker exec -i "$CONTAINER" \
        mysql -uroot -p"$ROOT_PASSWORD" --silent --skip-column-names \
        -e "$1" 2>/dev/null
}

echo "Connecting to $CONTAINER ..."
run_sql "SELECT 1;" > /dev/null
echo "Connected."
echo ""

echo "[1/3] Creating database '$DB_NAME' ..."
run_sql "CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
echo "      Done."

echo "[2/3] Creating user '$DB_USER'@'%' ..."
run_sql "CREATE USER IF NOT EXISTS '$DB_USER'@'%' IDENTIFIED BY '$DB_PASSWORD';"
echo "      Done."

echo "[3/3] Granting privileges ..."
run_sql "GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$DB_USER'@'%';"
run_sql "FLUSH PRIVILEGES;"
echo "      Done."

echo ""
echo "====================================================="
echo " Database : $DB_NAME"
echo " User     : $DB_USER"
echo "====================================================="
