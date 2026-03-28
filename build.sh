#!/bin/bash
# ============================================================
#  CAMS Build & Run Script  (Linux / macOS)
# ============================================================
set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_ROOT/src/main/java"
OUT_DIR="$PROJECT_ROOT/out"
LIB_DIR="$PROJECT_ROOT/lib"
MAIN_CLASS="com.cams.CAMSApplication"

# ── Colour helpers ───────────────────────────────────────────
GREEN='\033[0;32m'; CYAN='\033[0;36m'; RED='\033[0;31m'; NC='\033[0m'
ok()   { echo -e "${GREEN}[OK]${NC}  $1"; }
info() { echo -e "${CYAN}[--]${NC}  $1"; }
fail() { echo -e "${RED}[ERR]${NC} $1"; exit 1; }

echo ""
echo "  ╔══════════════════════════════════════════════════╗"
echo "  ║         CAMS Build Script  v1.0                 ║"
echo "  ╚══════════════════════════════════════════════════╝"
echo ""

# ── Check Java ───────────────────────────────────────────────
command -v java  >/dev/null 2>&1 || fail "Java not found. Install JDK 11+."
command -v javac >/dev/null 2>&1 || fail "javac not found. Install JDK 11+."
ok "Java: $(java -version 2>&1 | head -1)"

# ── Check MySQL connector jar ─────────────────────────────────
CONNECTOR=$(ls "$LIB_DIR"/mysql-connector*.jar 2>/dev/null | head -1)
if [ -z "$CONNECTOR" ]; then
    fail "MySQL connector JAR not found in lib/.\n  Download from: https://dev.mysql.com/downloads/connector/j/\n  Copy the .jar into the lib/ directory."
fi
ok "MySQL connector: $(basename "$CONNECTOR")"

# ── Create output directory ───────────────────────────────────
mkdir -p "$OUT_DIR"

# ── Compile ───────────────────────────────────────────────────
info "Compiling sources..."
find "$SRC_DIR" -name "*.java" > /tmp/cams_sources.txt
javac -cp "$LIB_DIR/*" -d "$OUT_DIR" @/tmp/cams_sources.txt
ok "Compilation successful → out/"

# ── Run ───────────────────────────────────────────────────────
info "Starting CAMS..."
echo ""
java -cp "$OUT_DIR:$LIB_DIR/*" "$MAIN_CLASS"
