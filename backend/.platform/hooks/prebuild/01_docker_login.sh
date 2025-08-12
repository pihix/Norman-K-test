#!/usr/bin/env bash
set -euo pipefail

# Par défaut on cible GHCR
REGISTRY="${DOCKER_REGISTRY_SERVER:-ghcr.io}"

# On accepte GHCR_* ou DOCKER_* (compat)
USER="${GHCR_USERNAME:-${DOCKER_USERNAME:-}}"
PASS="${GHCR_TOKEN:-${DOCKER_PASSWORD:-}}"

echo "Logging into ${REGISTRY}…"

if [[ -z "${USER}" || -z "${PASS}" ]]; then
  echo "ERROR: missing credentials. Set GHCR_USERNAME and GHCR_TOKEN in EB env properties." >&2
  exit 1
fi

echo "${PASS}" | docker login "${REGISTRY}" -u "${USER}" --password-stdin
echo "Login OK."
