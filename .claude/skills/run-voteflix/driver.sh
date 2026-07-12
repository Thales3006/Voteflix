#!/usr/bin/env bash
# Voteflix driver — launch, screenshot, click, stop
# Usage: source this file or run commands directly.
# Requires: Xvfb on :99, nix develop env, ImageMagick import, xdotool from nix.
#
# Exported helpers:
#   vfx_start_display    — start Xvfb on :99
#   vfx_start_server     — start server in background
#   vfx_start_client     — start client in background
#   vfx_ss <path>        — screenshot root window to <path>
#   vfx_click <x> <y>    — click at x y
#   vfx_type <text>      — type text into focused widget
#   vfx_wait_window      — wait until java window appears (up to 30s)
#   vfx_stop             — kill server, client, Xvfb

VOTEFLIX_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
DISPLAY="${DISPLAY:-:99}"
export DISPLAY

_XVFB_PID=
_SERVER_PID=
_CLIENT_PID=

vfx_start_display() {
    if ! DISPLAY=$DISPLAY xdpyinfo &>/dev/null 2>&1; then
        Xvfb "$DISPLAY" -screen 0 1280x800x24 &>/dev/null &
        _XVFB_PID=$!
        sleep 1
        echo "[vfx] Xvfb started (PID $_XVFB_PID) on $DISPLAY"
    else
        echo "[vfx] Display $DISPLAY already active"
    fi
}

vfx_start_server() {
    local log="${1:-/tmp/voteflix-server.log}"
    (cd "$VOTEFLIX_DIR" && nix develop --command bash -c "
        export DISPLAY=$DISPLAY
        ./mvnw -pl common install -DskipTests -q
        ./mvnw -pl server exec:java
    " > "$log" 2>&1) &
    _SERVER_PID=$!
    echo "[vfx] Server starting (PID $_SERVER_PID), log: $log"
    # wait for "Server Online!"
    local i=0
    while [ $i -lt 30 ]; do
        grep -q "Server Online" "$log" 2>/dev/null && break
        sleep 1; i=$((i+1))
    done
    grep -q "Server Online" "$log" 2>/dev/null \
        && echo "[vfx] Server is online" \
        || echo "[vfx] WARNING: server may not have started — check $log"
}

vfx_start_client() {
    local log="${1:-/tmp/voteflix-client.log}"
    (cd "$VOTEFLIX_DIR" && nix develop --command bash -c "
        export DISPLAY=$DISPLAY
        ./mvnw -pl common install -DskipTests -q
        ./mvnw -pl client javafx:run
    " > "$log" 2>&1) &
    _CLIENT_PID=$!
    echo "[vfx] Client starting (PID $_CLIENT_PID), log: $log"
}

vfx_wait_window() {
    local i=0
    echo "[vfx] Waiting for java window..."
    while [ $i -lt 30 ]; do
        nix shell nixpkgs#xdotool --command xdotool search --class "java" &>/dev/null && break
        sleep 1; i=$((i+1))
    done
    WIN_ID=$(nix shell nixpkgs#xdotool --command xdotool search --class "java" 2>/dev/null | head -1)
    [ -n "$WIN_ID" ] \
        && echo "[vfx] Window found: $WIN_ID" \
        || echo "[vfx] WARNING: no java window found after ${i}s"
    export VFX_WIN_ID="$WIN_ID"
}

vfx_ss() {
    local out="${1:-/tmp/vfx-screenshot.png}"
    DISPLAY=$DISPLAY import -window root "$out" 2>/dev/null
    echo "[vfx] Screenshot → $out"
}

vfx_click() {
    local x=$1 y=$2
    nix shell nixpkgs#xdotool --command bash -c "
        xdotool mousemove --sync $x $y
        xdotool click 1
    " 2>/dev/null
}

vfx_type() {
    nix shell nixpkgs#xdotool --command xdotool type --clearmodifiers "$1" 2>/dev/null
}

vfx_stop() {
    [ -n "$_CLIENT_PID" ] && kill "$_CLIENT_PID" 2>/dev/null && echo "[vfx] Client stopped"
    [ -n "$_SERVER_PID" ] && kill "$_SERVER_PID" 2>/dev/null && echo "[vfx] Server stopped"
    [ -n "$_XVFB_PID"  ] && kill "$_XVFB_PID"  2>/dev/null && echo "[vfx] Xvfb stopped"
    # also kill stray maven/java processes
    pkill -f "exec:java" 2>/dev/null || true
    pkill -f "javafx:run" 2>/dev/null || true
}

# ── Interactive flow helpers ──────────────────────────────────────────────────

# vfx_login <username> <password>
# Assumes the login page is visible and NOT yet connected to the server.
# Verified working: admin/admin. Other users' passwords are unknown (bcrypt hashes in SQL ≠ username).
# Connect button coords: ~(495, 658); username: ~(640, 339); password: ~(640, 415); signin: ~(562, 466)
vfx_login() {
    local user=$1 pass=$2
    sleep 0.5
    vfx_click 495 658   # Connect
    sleep 1.5
    vfx_click 640 339   # Username field
    sleep 0.3
    nix shell nixpkgs#xdotool --command bash -c "xdotool key --clearmodifiers ctrl+a" 2>/dev/null
    vfx_type "$user"
    vfx_click 640 415   # Password field
    sleep 0.3
    nix shell nixpkgs#xdotool --command bash -c "xdotool key --clearmodifiers ctrl+a" 2>/dev/null
    vfx_type "$pass"
    vfx_click 562 466   # Sign In
    sleep 2
    echo "[vfx] Login attempted as $user"
}

# vfx_nav <tab>   where tab is "Reviews" | "Movies" | "Users" | "Logout"
vfx_nav() {
    case "$1" in
        Reviews) vfx_click 169 81 ;;
        Movies)  vfx_click 255 81 ;;
        Users)   vfx_click 335 81 ;;
        Logout)  vfx_click 1113 81 ;;
        *) echo "[vfx] Unknown tab: $1" ;;
    esac
    sleep 1.5
}
