# Kafka Infrastructure (Red Hat AMQ Streams)

Automated provisioning of Apache Kafka on OpenShift via the Red Hat AMQ Streams Operator.

## Files

| File | Description |
|---|---|
| `01-operator-subscription.yml` | OLM Subscription CR that installs the AMQ Streams Operator from the Red Hat catalog |
| `02-kafka-cluster.yml` | Kafka CR defining a single-broker cluster with persistent storage, plain and TLS listeners, ZooKeeper, and the Entity Operator for topic/user management |
| `03-kafka-topics.yml` | KafkaTopic CR for `thoughts.events` (3 partitions, 1 replica, 7-day retention) |
| `setup.sh` | Orchestrator script that applies the CRs in order with readiness gates between each step |
| `teardown.sh` | Removes topics, cluster, and optionally the operator and namespace |

## Prerequisites

- `oc` CLI installed and authenticated (`oc login`)
- Cluster-admin privileges (required to install operators via OLM)
- Available persistent storage (default: 10Gi for Kafka, 5Gi for ZooKeeper)

## Setup

```bash
./setup.sh [NAMESPACE]
```

The namespace defaults to `positive-thoughts` if not provided. The script is idempotent -- safe to re-run if any step fails partway through.

### What the script does

1. Creates the target namespace (if it doesn't exist)
2. Installs the AMQ Streams Operator via an OLM Subscription (skips if already installed)
3. Waits for the operator CSV to reach `Succeeded` phase
4. Deploys the Kafka cluster with persistent storage
5. Waits for the Kafka cluster to report `Ready`
6. Creates the `thoughts.events` topic
7. Verifies the bootstrap service is available

On completion it prints the bootstrap connection string for use in Quarkus services:

```
KAFKA_BOOTSTRAP_SERVERS=positive-thoughts-kafka-bootstrap.positive-thoughts.svc.cluster.local:9092
```

### Configuration

Override defaults with environment variables:

| Variable | Default | Description |
|---|---|---|
| `KAFKA_STORAGE_SIZE` | `10Gi` | PVC size for each Kafka broker |
| `ZOOKEEPER_STORAGE_SIZE` | `5Gi` | PVC size for each ZooKeeper node |
| `KAFKA_REPLICAS` | `1` | Number of Kafka broker replicas |
| `ZOOKEEPER_REPLICAS` | `1` | Number of ZooKeeper replicas |

Example with 3 brokers and larger storage:

```bash
KAFKA_REPLICAS=3 KAFKA_STORAGE_SIZE=50Gi ./setup.sh my-namespace
```

## Teardown

```bash
./teardown.sh [NAMESPACE]
```

Removes the Kafka topics and cluster from the target namespace. By default the operator and namespace are preserved. Control this with environment variables:

| Variable | Default | Description |
|---|---|---|
| `REMOVE_OPERATOR` | `false` | Also remove the AMQ Streams Operator subscription and CSV |
| `REMOVE_NAMESPACE` | `false` | Also delete the target namespace |

Full cleanup example:

```bash
REMOVE_OPERATOR=true REMOVE_NAMESPACE=true ./teardown.sh positive-thoughts
```
