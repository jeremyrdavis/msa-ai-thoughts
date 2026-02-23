#!/usr/bin/env bash
#
# Automated setup for PostgreSQL on OpenShift.
#
# Usage:
#   ./setup.sh [NAMESPACE]
#
# Arguments:
#   NAMESPACE   Target namespace (default: positive-thoughts)
#
# Environment variables:
#   POSTGRESQL_USER          Database username (default: thoughts)
#   POSTGRESQL_PASSWORD      Database password (default: thoughts)
#   POSTGRESQL_DATABASE      Database name (default: thoughts_db)
#   POSTGRESQL_STORAGE_SIZE  PVC size (default: 5Gi)
#   POSTGRESQL_IMAGE         Container image (default: registry.redhat.io/rhel9/postgresql-16:latest)

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
NAMESPACE="${1:-positive-thoughts}"
POSTGRESQL_USER="${POSTGRESQL_USER:-thoughts}"
POSTGRESQL_PASSWORD="${POSTGRESQL_PASSWORD:-thoughts}"
POSTGRESQL_DATABASE="${POSTGRESQL_DATABASE:-thoughts_db}"
POSTGRESQL_STORAGE_SIZE="${POSTGRESQL_STORAGE_SIZE:-5Gi}"
POSTGRESQL_IMAGE="${POSTGRESQL_IMAGE:-registry.redhat.io/rhel9/postgresql-16:latest}"
DEPLOY_WAIT_TIMEOUT="120s"

log() { echo "==> $*"; }
warn() { echo "WARNING: $*" >&2; }
fail() { echo "ERROR: $*" >&2; exit 1; }

# -------------------------------------------------------------------
# Preflight checks
# -------------------------------------------------------------------
command -v oc >/dev/null 2>&1 || fail "oc CLI not found. Install it from https://mirror.openshift.com/pub/openshift-v4/clients/ocp/stable/"
oc whoami >/dev/null 2>&1 || fail "Not logged in to OpenShift. Run 'oc login' first."

log "Target namespace: ${NAMESPACE}"
log "Database: ${POSTGRESQL_DATABASE}"
log "User: ${POSTGRESQL_USER}"
log "Storage: ${POSTGRESQL_STORAGE_SIZE}"

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
# Step 2: Create Secret
# -------------------------------------------------------------------
log "Applying database credentials secret..."

cat <<EOF | oc apply -n "${NAMESPACE}" -f -
apiVersion: v1
kind: Secret
metadata:
  name: postgresql-credentials
  labels:
    app: postgresql
    app.kubernetes.io/part-of: positive-thoughts
type: Opaque
stringData:
  POSTGRESQL_USER: "${POSTGRESQL_USER}"
  POSTGRESQL_PASSWORD: "${POSTGRESQL_PASSWORD}"
  POSTGRESQL_DATABASE: "${POSTGRESQL_DATABASE}"
EOF

# -------------------------------------------------------------------
# Step 3: Create PVC
# -------------------------------------------------------------------
log "Applying persistent volume claim (${POSTGRESQL_STORAGE_SIZE})..."

sed \
    -e "s/storage: 5Gi/storage: ${POSTGRESQL_STORAGE_SIZE}/" \
    "${SCRIPT_DIR}/02-pvc.yml" | \
    oc apply -n "${NAMESPACE}" -f -

# -------------------------------------------------------------------
# Step 4: Deploy PostgreSQL
# -------------------------------------------------------------------
log "Deploying PostgreSQL..."

sed \
    -e "s|image: registry.redhat.io/rhel9/postgresql-16:latest|image: ${POSTGRESQL_IMAGE}|" \
    "${SCRIPT_DIR}/03-deployment.yml" | \
    oc apply -n "${NAMESPACE}" -f -

# -------------------------------------------------------------------
# Step 5: Create Service
# -------------------------------------------------------------------
log "Applying service..."
oc apply -n "${NAMESPACE}" -f "${SCRIPT_DIR}/04-service.yml"

# -------------------------------------------------------------------
# Step 6: Wait for readiness
# -------------------------------------------------------------------
log "Waiting for PostgreSQL deployment to be ready (timeout: ${DEPLOY_WAIT_TIMEOUT})..."
oc rollout status deployment/postgresql -n "${NAMESPACE}" --timeout="${DEPLOY_WAIT_TIMEOUT}"

log "PostgreSQL is ready"

# -------------------------------------------------------------------
# Step 7: Verify
# -------------------------------------------------------------------
JDBC_URL="jdbc:postgresql://postgresql.${NAMESPACE}.svc.cluster.local:5432/${POSTGRESQL_DATABASE}"

echo ""
log "PostgreSQL setup complete!"
echo ""
echo "  Namespace: ${NAMESPACE}"
echo "  Service:   postgresql.${NAMESPACE}.svc.cluster.local:5432"
echo "  Database:  ${POSTGRESQL_DATABASE}"
echo "  User:      ${POSTGRESQL_USER}"
echo ""
echo "  Connect your Quarkus services with:"
echo "    QUARKUS_DATASOURCE_JDBC_URL=${JDBC_URL}"
echo "    QUARKUS_DATASOURCE_USERNAME=${POSTGRESQL_USER}"
echo "    QUARKUS_DATASOURCE_PASSWORD=${POSTGRESQL_PASSWORD}"
