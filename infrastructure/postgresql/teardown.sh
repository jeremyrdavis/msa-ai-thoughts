#!/usr/bin/env bash
#
# Tears down the PostgreSQL infrastructure created by setup.sh.
# Removes resources in reverse order: service, deployment, PVC, secret.
#
# Usage:
#   ./teardown.sh [NAMESPACE]
#
# Arguments:
#   NAMESPACE   Target namespace (default: positive-thoughts)
#
# Environment variables:
#   REMOVE_NAMESPACE  Set to "true" to also delete the namespace (default: false)

set -euo pipefail

NAMESPACE="${1:-positive-thoughts}"
REMOVE_NAMESPACE="${REMOVE_NAMESPACE:-false}"

log() { echo "==> $*"; }

command -v oc >/dev/null 2>&1 || { echo "ERROR: oc CLI not found" >&2; exit 1; }
oc whoami >/dev/null 2>&1 || { echo "ERROR: Not logged in to OpenShift" >&2; exit 1; }

# -------------------------------------------------------------------
# Step 1: Delete Service
# -------------------------------------------------------------------
log "Deleting PostgreSQL service..."
oc delete service postgresql -n "${NAMESPACE}" --ignore-not-found=true

# -------------------------------------------------------------------
# Step 2: Delete Deployment
# -------------------------------------------------------------------
log "Deleting PostgreSQL deployment..."
oc delete deployment postgresql -n "${NAMESPACE}" --ignore-not-found=true

log "Waiting for PostgreSQL pod to terminate..."
oc wait pod -l app=postgresql -n "${NAMESPACE}" \
    --for=delete --timeout=60s 2>/dev/null || true

# -------------------------------------------------------------------
# Step 3: Delete PVC
# -------------------------------------------------------------------
log "Deleting persistent volume claim..."
oc delete pvc postgresql-data -n "${NAMESPACE}" --ignore-not-found=true

# -------------------------------------------------------------------
# Step 4: Delete Secret
# -------------------------------------------------------------------
log "Deleting credentials secret..."
oc delete secret postgresql-credentials -n "${NAMESPACE}" --ignore-not-found=true

# -------------------------------------------------------------------
# Step 5: Remove namespace (optional)
# -------------------------------------------------------------------
if [ "${REMOVE_NAMESPACE}" = "true" ]; then
    log "Deleting namespace '${NAMESPACE}'..."
    oc delete namespace "${NAMESPACE}" --ignore-not-found=true
else
    log "Skipping namespace removal (set REMOVE_NAMESPACE=true to remove)"
fi

echo ""
log "PostgreSQL teardown complete."
