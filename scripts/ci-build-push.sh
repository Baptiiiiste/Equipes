#!/usr/bin/env bash
set -euo pipefail

# ── Configuration ────────────────
IMAGE_NAME="${IMAGE_NAME:-babonneau/equipes_server}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
CI_REGISTRY="${CI_REGISTRY:-docker.isima.fr}"
ENFORCE_MASTER_ONLY="${ENFORCE_MASTER_ONLY:-0}"
DRY_RUN="${DRY_RUN:-0}"
PLATFORMS="${PLATFORMS:-linux/amd64}"
BUILDER_NAME="${BUILDER_NAME:-equipes-builder}"
DOCKER_USERNAME="${DOCKER_USERNAME:-babonneau}"
DOCKER_PASSWORD="${DOCKER_PASSWORD:-}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

# ── Helpers ──────────────────────────────────────────────────────
log() {
  printf '[%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$*"
}

run() {
  if [[ "$DRY_RUN" == "1" ]]; then
    printf '[DRY RUN]'
    for arg in "$@"; do
      printf ' %q' "$arg"
    done
    printf '\n'
    return 0
  fi
  "$@"
}

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Error: missing required command '$1'" >&2
    exit 1
  fi
}

current_branch() {
  if [[ -n "${CI_COMMIT_BRANCH:-}" ]]; then
    printf '%s' "$CI_COMMIT_BRANCH"
    return
  fi
  git rev-parse --abbrev-ref HEAD 2>/dev/null || printf 'unknown'
}

print_help() {
  cat <<'EOF'
Usage: ./scripts/ci-build-push.sh

Builds and pushes the Docker image for the Equipes server to the registry.

Environment variables:
  IMAGE_NAME              Default: babonneau/equipes_server
  IMAGE_TAG               Default: latest
  CI_REGISTRY             Default: docker.isima.fr
  PLATFORMS               Default: linux/amd64 (example: linux/amd64,linux/arm64)
  BUILDER_NAME            Default: equipes-builder
  DOCKER_USERNAME         Default: babonneau
  DOCKER_PASSWORD         Required unless DRY_RUN=1
  ENFORCE_MASTER_ONLY     Default: 0 (set to 1 to restrict push to master only)
  DRY_RUN                 Default: 0 (set to 1 to print commands only)
EOF
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  print_help
  exit 0
fi

# ── Pre-flight checks ───────────────────────────────────────────
require_command docker

if ! docker buildx version >/dev/null 2>&1; then
  echo "Error: docker buildx is required but unavailable." >&2
  exit 1
fi

if [[ ! -f "$PROJECT_ROOT/Dockerfile" ]]; then
  echo "Error: Dockerfile not found at '$PROJECT_ROOT/Dockerfile'." >&2
  exit 1
fi

BRANCH="$(current_branch)"
if [[ "$ENFORCE_MASTER_ONLY" == "1" && "$BRANCH" != "master" ]]; then
  echo "Error: push is restricted to 'master' (current branch: '$BRANCH')." >&2
  echo "Set ENFORCE_MASTER_ONLY=0 to bypass this check." >&2
  exit 1
fi

if [[ "$DRY_RUN" != "1" ]]; then
  if [[ -z "${DOCKER_PASSWORD:-}" ]]; then
    echo "Error: DOCKER_PASSWORD must be set." >&2
    echo "Export it before running: export DOCKER_PASSWORD='your-password'" >&2
    exit 1
  fi
fi

IMAGE_REF="$CI_REGISTRY/$IMAGE_NAME:$IMAGE_TAG"

# ── Buildx setup ────────────────────────────────────────────────
log "Preparing buildx builder: $BUILDER_NAME"
if [[ "$DRY_RUN" == "1" ]]; then
  printf '[DRY RUN] docker buildx inspect %q >/dev/null 2>&1 || docker buildx create --name %q --use\n' "$BUILDER_NAME" "$BUILDER_NAME"
  printf '[DRY RUN] docker buildx use %q\n' "$BUILDER_NAME"
  printf '[DRY RUN] docker buildx inspect --bootstrap\n'
else
  if ! docker buildx inspect "$BUILDER_NAME" >/dev/null 2>&1; then
    docker buildx create --name "$BUILDER_NAME" --use
  fi
  docker buildx use "$BUILDER_NAME"
  docker buildx inspect --bootstrap >/dev/null
fi

# ── Registry login ───────────────────────────────────────────────
log "Logging in to Docker registry: $CI_REGISTRY"
if [[ "$DRY_RUN" == "1" ]]; then
  printf '[DRY RUN] echo "$DOCKER_PASSWORD" | docker login %s -u "%s" --password-stdin\n' "$CI_REGISTRY" "$DOCKER_USERNAME"
else
  echo "$DOCKER_PASSWORD" | docker login "$CI_REGISTRY" -u "$DOCKER_USERNAME" --password-stdin
fi

# ── Build & push ─────────────────────────────────────────────────
log "Building and pushing Docker image: $IMAGE_REF"
run docker buildx build \
  --platform "$PLATFORMS" \
  -f "$PROJECT_ROOT/Dockerfile" \
  -t "$IMAGE_REF" \
  --push \
  "$PROJECT_ROOT"

log "Done. Image pushed: $IMAGE_REF (platforms: $PLATFORMS)"
