#!/bin/bash
set -euo pipefail
echo "Logging into GHCR..."
docker login "${DOCKER_REGISTRY_SERVER}" -u "${DOCKER_REGISTRY_USERNAME}" -p "${DOCKER_REGISTRY_PASSWORD}"
echo "Logged into ${DOCKER_REGISTRY_SERVER}"
