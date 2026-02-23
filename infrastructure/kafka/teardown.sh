#!/usr/bin/env bash
#
# Tears down the Kafka infrastructure created by setup.sh.
# Removes resources in reverse order: topics, cluster, operator subscription.
#
# Usage:
#   ./teardown.sh [NAMESPACE]
#
# Arguments:
#   NAMESPACE   Target namespace (default: positive-thoughts)
#
# Environment variables:
#   REMOVE_OPERATOR   Set to "true" to also remove the AMQ Streams Operator (default: false)
#   REMOVE_NAMESPACE  Set to "true" to also delete the namespace (default: false)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="${1:-positive-thoughts}"
REMOVE_OPERATOR="${REMOVE_OPERATOR:-false}"
REMOVE_NAMESPACE="${REMOVE_NAMESPACE:-false}"
KAFKA_CLUSTER_NAME="positive-thoughts"

log() { echo "==> $*"; }
warn() { echo "WARNING: $*" >&2; }

command -v oc >/dev/null 2>&1 || { echo "ERROR: oc CLI not found" >&2; exit 1; }
oc whoami >/dev/null 2>&1 || { echo "ERROR: Not logged in to OpenShift" >&2; exit 1; }

# -------------------------------------------------------------------
# Step 1: Delete topics
# -------------------------------------------------------------------
log "Deleting Kafka topics in namespace '${NAMESPACE}'..."
oc delete kafkatopic thoughts.events -n "${NAMESPACE}" --ignore-not-found=true

# -------------------------------------------------------------------
# Step 2: Delete Kafka cluster
# -------------------------------------------------------------------
log "Deleting Kafka cluster '${KAFKA_CLUSTER_NAME}'..."
oc delete kafka "${KAFKA_CLUSTER_NAME}" -n "${NAMESPACE}" --ignore-not-found=true

log "Waiting for Kafka cluster pods to terminate..."
oc wait pod -l strimzi.io/cluster="${KAFKA_CLUSTER_NAME}" -n "${NAMESPACE}" \
    --for=delete --timeout=120s 2>/dev/null || true

# -------------------------------------------------------------------
# Step 3: Remove operator (optional)
# -------------------------------------------------------------------
if [ "${REMOVE_OPERATOR}" = "true" ]; then
    log "Removing AMQ Streams Operator subscription..."
    oc delete subscription amq-streams -n openshift-operators --ignore-not-found=true

    CSV_NAME=$(oc get csv -n openshift-operators -o jsonpath='{.items[?(@.spec.displayName=="AMQ Streams")].metadata.name}' 2>/dev/null || true)
    if [ -n "${CSV_NAME}" ]; then
        log "Removing CSV '${CSV_NAME}'..."
        oc delete csv "${CSV_NAME}" -n openshift-operators --ignore-not-found=true
    fi
else
    log "Skipping operator removal (set REMOVE_OPERATOR=true to remove)"
fi

# -------------------------------------------------------------------
# Step 4: Remove namespace (optional)
# -------------------------------------------------------------------
if [ "${REMOVE_NAMESPACE}" = "true" ]; then
    log "Deleting namespace '${NAMESPACE}'..."
    oc delete namespace "${NAMESPACE}" --ignore-not-found=true
else
    log "Skipping namespace removal (set REMOVE_NAMESPACE=true to remove)"
fi

echo ""
log "Kafka teardown complete."
