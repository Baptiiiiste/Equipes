#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_ROOT"

# ── Detect platform and setup DISPLAY ────────────────────────────
setup_display() {
  # macOS - always has GUI
  if [[ "$OSTYPE" == "darwin"* ]]; then
    return 0
  fi

  # WSL (Windows Subsystem for Linux)
  if grep -qEi "(Microsoft|WSL)" /proc/version 2>/dev/null; then
    echo "[INFO] WSL detected"
    
    # WSL2 with WSLg (Windows 11) - automatic display
    if [[ -n "${WAYLAND_DISPLAY:-}" ]] || [[ -n "${DISPLAY:-}" ]]; then
      echo "[INFO] WSLg detected, using native display"
      return 0
    fi
    
    # WSL1 or WSL2 without WSLg - need VcXsrv/Xming
    if [[ -z "${DISPLAY:-}" ]]; then
      WINDOWS_HOST=$(cat /etc/resolv.conf | grep nameserver | awk '{print $2}' | head -n1)
      export DISPLAY="${WINDOWS_HOST}:0"
      echo "[INFO] Setting DISPLAY=${DISPLAY}"
      echo "[WARN] Make sure VcXsrv or Xming is running on Windows"
    fi
    return 0
  fi

  # Regular Linux
  if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Check if DISPLAY is set
    if [[ -z "${DISPLAY:-}" ]]; then
      # Try to set a default
      export DISPLAY=:0
      echo "[INFO] DISPLAY not set, trying DISPLAY=:0"
    fi
    
    # Verify X server is accessible
    if ! command -v xdpyinfo >/dev/null 2>&1; then
      echo "[WARN] xdpyinfo not found, cannot verify X server"
    elif ! xdpyinfo >/dev/null 2>&1; then
      echo "[ERROR] No X server found at DISPLAY=${DISPLAY}"
      echo "[ERROR] Solutions:"
      echo "  - If using SSH: run this script on your LOCAL machine, not via SSH"
      echo "  - If on Linux desktop: make sure X11 is running"
      echo "  - If headless: install Xvfb and run: xvfb-run ./scripts/run-client.sh"
      exit 1
    fi
    return 0
  fi

  # Unknown platform
  echo "[WARN] Unknown platform: $OSTYPE"
}

# ── Build ─────────────────────────────────────────────────────────
echo "[INFO] Building JAR..."
mvn clean package -DskipTests -q

# ── Setup display ─────────────────────────────────────────────────
setup_display

# ── Launch client ─────────────────────────────────────────────────
echo ""
echo "=== Lancement du client Equipes ==="
echo ""

# Run the client
java -jar target/Equipes-1.0-SNAPSHOT.jar client
