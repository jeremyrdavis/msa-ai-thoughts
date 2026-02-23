# PostgreSQL Database Setup

Automated provisioning of PostgreSQL on OpenShift for the Positive Thoughts application.

## Files

| File | Description |
|---|---|
| `01-secret.yml` | Secret containing database username, password, and database name |
| `02-pvc.yml` | PersistentVolumeClaim for PostgreSQL data (default 5Gi) |
| `03-deployment.yml` | Deployment using Red Hat PostgreSQL 16 image with liveness/readiness probes |
| `04-service.yml` | ClusterIP Service exposing port 5432 for internal access |
| `setup.sh` | Orchestrator script that applies manifests in order and waits for readiness |
| `teardown.sh` | Removes all PostgreSQL resources in reverse order |

## Prerequisites

- `oc` CLI installed and authenticated (`oc login`)
- Available persistent storage (default: 5Gi)
- Access to pull `registry.redhat.io/rhel9/postgresql-16` (or override with a different image)

## Setup

```bash
./setup.sh [NAMESPACE]
```

The namespace defaults to `positive-thoughts` if not provided. The script is idempotent -- safe to re-run.

### What the script does

1. Creates the target namespace (if it doesn't exist)
2. Creates a Secret with database credentials
3. Creates a PVC for persistent data
4. Deploys the PostgreSQL pod
5. Waits for the deployment rollout to complete
6. Prints the JDBC connection string for Quarkus services

On completion it prints the connection details:

```
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://postgresql.positive-thoughts.svc.cluster.local:5432/thoughts_db
QUARKUS_DATASOURCE_USERNAME=thoughts
QUARKUS_DATASOURCE_PASSWORD=thoughts
```

### Configuration

Override defaults with environment variables:

| Variable | Default | Description |
|---|---|---|
| `POSTGRESQL_USER` | `thoughts` | Database username |
| `POSTGRESQL_PASSWORD` | `thoughts` | Database password |
| `POSTGRESQL_DATABASE` | `thoughts_db` | Database name |
| `POSTGRESQL_STORAGE_SIZE` | `5Gi` | PVC size |
| `POSTGRESQL_IMAGE` | `registry.redhat.io/rhel9/postgresql-16:latest` | Container image |

Example with custom credentials and storage:

```bash
POSTGRESQL_USER=admin POSTGRESQL_PASSWORD=s3cret POSTGRESQL_STORAGE_SIZE=20Gi ./setup.sh my-namespace
```

### Schema management

This setup only provisions the PostgreSQL instance. Database schema (tables, indexes, constraints) is created automatically by **Flyway migrations** embedded in each Quarkus service on startup. No manual schema setup is needed.

## Teardown

```bash
./teardown.sh [NAMESPACE]
```

Removes the service, deployment, PVC, and secret. The namespace is preserved by default.

| Variable | Default | Description |
|---|---|---|
| `REMOVE_NAMESPACE` | `false` | Also delete the target namespace |

Full cleanup:

```bash
REMOVE_NAMESPACE=true ./teardown.sh positive-thoughts
```
