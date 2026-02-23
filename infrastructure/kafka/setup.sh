#!/usr/bin/env bash
#
# Automated setup for Red Hat AMQ Streams (Kafka) on OpenShift.
#
# Usage:
#   ./setup.sh [NAMESPACE]
#
# Arguments:
#   NAMESPACE   Target namespace for Kafka cluster (default: positive-thoughts)
#
# Environment variables:
#   KAFKA_STORAGE_SIZE        Broker PVC size (default: 10Gi)
#   ZOOKEEPER_STORAGE_SIZE    ZooKeeper PVC size (default: 5Gi)
#   KAFKA_REPLICAS            Broker replica count (default: 1)
#   ZOOKEEPER_REPLICAS        ZooKeeper replica count (default: 1)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="${1:-positive-thoughts}"
KAFKA_STORAGE_SIZE="${KAFKA_STORAGE_SIZE:-10Gi}"
ZOOKEEPER_STORAGE_SIZE="${ZOOKEEPER_STORAGE_SIZE:-5Gi}"
KAFKA_REPLICAS="${KAFKA_REPLICAS:-1}"
ZOOKEEPER_REPLICAS="${ZOOKEEPER_REPLICAS:-1}"
KAFKA_CLUSTER_NAME="positive-thoughts"
OPERATOR_WAIT_TIMEOUT="300s"
CLUSTER_WAIT_TIMEOUT="600s"
TOPIC_WAIT_TIMEOUT="120s"

log() { echo "==> $*"; }
warn() { echo "WARNING: $*" >&2; }
fail() { echo "ERROR: $*" >&2; exit 1; }

# -------------------------------------------------------------------
# Preflight checks
# -------------------------------------------------------------------
command -v oc >/dev/null 2>&1 || fail "oc CLI not found. Install it from https://mirror.openshift.com/pub/openshift-v4/clients/ocp/stable/"
oc whoami >/dev/null 2>&1 || fail "Not logged in to OpenShift. Run 'oc login' first."

log "Target namespace: ${NAMESPACE}"
log "Kafka brokers: ${KAFKA_REPLICAS} x ${KAFKA_STORAGE_SIZE} persistent storage"
log "ZooKeeper nodes: ${ZOOKEEPER_REPLICAS} x ${ZOOKEEPER_STORAGE_SIZE} persistent storage"

# -------------------------------------------------------------------
# Step 1: Create namespace if it doesn't exist
# -------------------------------------------------------------------
if oc get namespace "${NAMESPACE}" >/dev/null 2>&1; then
    log "Namespace '${NAMESPACE}' already exists"
else
    log "Creating namespace '${NAMESPACE}'..."
    oc create namespace "${NAMESPACE}"
fi

oc project "${NAMESPACE}"

# -------------------------------------------------------------------
# Step 2: Install AMQ Streams Operator (cluster-scoped)
# -------------------------------------------------------------------
log "Checking for AMQ Streams Operator..."

if oc get subscription amq-streams -n openshift-operators >/dev/null 2>&1; then
    log "AMQ Streams Operator subscription already exists"
else
    log "Installing AMQ Streams Operator..."
    oc apply -f "${SCRIPT_DIR}/01-operator-subscription.yml"
fi

log "Waiting for AMQ Streams Operator to be ready (timeout: ${OPERATOR_WAIT_TIMEOUT})..."

# Wait for the CSV to appear and reach Succeeded phase
ATTEMPTS=0
MAX_ATTEMPTS=60
CSV_NAME=""
while [ -z "${CSV_NAME}" ] && [ ${ATTEMPTS} -lt ${MAX_ATTEMPTS} ]; do
    CSV_NAME=$(oc get csv -n openshift-operators -o jsonpath='{.items[?(@.spec.displayName=="AMQ Streams")].metadata.name}' 2>/dev/null || true)
    if [ -z "${CSV_NAME}" ]; then
        ATTEMPTS=$((ATTEMPTS + 1))
        sleep 5
    fi
done

if [ -z "${CSV_NAME}" ]; then
    fail "Timed out waiting for AMQ Streams CSV to appear"
fi

log "Found operator CSV: ${CSV_NAME}"
oc wait csv/"${CSV_NAME}" -n openshift-operators \
    --for=jsonpath='{.status.phase}'=Succeeded \
    --timeout="${OPERATOR_WAIT_TIMEOUT}"

log "AMQ Streams Operator is ready"

# -------------------------------------------------------------------
# Step 3: Deploy Kafka cluster
# -------------------------------------------------------------------
log "Deploying Kafka cluster '${KAFKA_CLUSTER_NAME}'..."

# Apply the Kafka CR, substituting configurable values via envsubst or sed
sed \
    -e "s/replicas: 1\b/replicas: ${KAFKA_REPLICAS}/" \
    -e "s/size: 10Gi/size: ${KAFKA_STORAGE_SIZE}/" \
    -e "s/size: 5Gi/size: ${ZOOKEEPER_STORAGE_SIZE}/" \
    "${SCRIPT_DIR}/02-kafka-cluster.yml" | \
    oc apply -n "${NAMESPACE}" -f -

log "Waiting for Kafka cluster to be ready (timeout: ${CLUSTER_WAIT_TIMEOUT})..."
oc wait kafka/"${KAFKA_CLUSTER_NAME}" -n "${NAMESPACE}" \
    --for=condition=Ready \
    --timeout="${CLUSTER_WAIT_TIMEOUT}"

log "Kafka cluster '${KAFKA_CLUSTER_NAME}' is ready"

# -------------------------------------------------------------------
# Step 4: Create topics
# -------------------------------------------------------------------
log "Creating Kafka topics..."
oc apply -n "${NAMESPACE}" -f "${SCRIPT_DIR}/03-kafka-topics.yml"

log "Waiting for topics to be ready (timeout: ${TOPIC_WAIT_TIMEOUT})..."
oc wait kafkatopic/thoughts.events -n "${NAMESPACE}" \
    --for=condition=Ready \
    --timeout="${TOPIC_WAIT_TIMEOUT}"

log "All topics are ready"

# -------------------------------------------------------------------
# Step 5: Verify
# -------------------------------------------------------------------
BOOTSTRAP_SVC="${KAFKA_CLUSTER_NAME}-kafka-bootstrap"
if oc get service "${BOOTSTRAP_SVC}" -n "${NAMESPACE}" >/dev/null 2>&1; then
    log "Bootstrap service verified: ${BOOTSTRAP_SVC}.${NAMESPACE}.svc.cluster.local:9092"
else
    warn "Bootstrap service '${BOOTSTRAP_SVC}' not found -- cluster may still be initializing"
fi

echo ""
log "Kafka infrastructure setup complete!"
echo ""
echo "  Cluster:   ${KAFKA_CLUSTER_NAME}"
echo "  Namespace: ${NAMESPACE}"
echo "  Bootstrap: ${BOOTSTRAP_SVC}.${NAMESPACE}.svc.cluster.local:9092"
echo "  Topics:    thoughts.events"
echo ""
echo "  Connect your Quarkus services with:"
echo "    KAFKA_BOOTSTRAP_SERVERS=${BOOTSTRAP_SVC}.${NAMESPACE}.svc.cluster.local:9092"
