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

  # Regular Linux (including Fedora, Ubuntu, etc.)
  if [[ "$OSTYPE" == "linux-gnu"* ]] || [[ "$OSTYPE" == "linux"* ]] || uname -s | grep -qi linux; then
    echo "[INFO] Linux detected"
    
    # Check if running via SSH (no GUI available)
    if [[ -n "${SSH_CONNECTION:-}" ]] || [[ -n "${SSH_CLIENT:-}" ]]; then
      echo "[ERROR] SSH session detected - GUI applications cannot run via SSH"
      echo "[ERROR] Run this script on your LOCAL machine, not on a remote server"
      exit 1
    fi
    
    # Check if DISPLAY is set
    if [[ -z "${DISPLAY:-}" ]]; then
      # Try common defaults
      if [[ -n "${WAYLAND_DISPLAY:-}" ]]; then
        # Wayland session (Fedora/GNOME default)
        export DISPLAY=:0
        echo "[INFO] Wayland detected, setting DISPLAY=:0"
      else
        # X11 session
        export DISPLAY=:0
        echo "[INFO] DISPLAY not set, trying DISPLAY=:0"
      fi
    fi
    
    # Verify display is accessible (try without xdpyinfo if not available)
    if command -v xdpyinfo >/dev/null 2>&1; then
      if ! xdpyinfo >/dev/null 2>&1; then
        echo "[ERROR] No X server found at DISPLAY=${DISPLAY}"
        echo "[ERROR] Solutions:"
        echo "  - Make sure you're running on a Linux desktop (not server/SSH)"
        echo "  - If on Fedora/GNOME with Wayland: install xorg-x11-server-Xwayland"
        echo "  - Try: export DISPLAY=:0 or export DISPLAY=:1"
        echo "  - If headless: xvfb-run ./scripts/run-client.sh"
        exit 1
      fi
    else
      echo "[WARN] xdpyinfo not found, cannot verify X server - proceeding anyway"
    fi
    return 0
  fi

  # Unknown platform
  echo "[WARN] Unknown platform: $OSTYPE (trying anyway)"
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
