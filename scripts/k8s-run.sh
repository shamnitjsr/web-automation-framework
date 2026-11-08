#!/usr/bin/env bash
# Runs the suite as an Indexed Job and fails the pipeline if any shard had failing tests.
# Required env: IMAGE, RUN_ID.  Optional: SHARDS (default 4), TEST_GROUPS (default empty = all).
set -euo pipefail
: "${IMAGE:?IMAGE is required}"
: "${RUN_ID:?RUN_ID is required}"
export IMAGE RUN_ID SHARDS="${SHARDS:-4}" TEST_GROUPS="${TEST_GROUPS:-}"
NS=qa-automation
JOB="web-automation-${RUN_ID}"

envsubst < k8s/job.yaml | kubectl apply -f -
kubectl -n "$NS" wait --for=condition=complete "job/${JOB}" --timeout=3600s

kubectl -n "$NS" logs -l "job-name=${JOB}" --tail=-1 --max-log-requests=100 \
  | grep SHARD_RESULT | sort | tee shard-results.txt || true

REPORTED=$(wc -l < shard-results.txt)
if [[ "$REPORTED" -ne "$SHARDS" ]]; then
  echo "Expected $SHARDS shard results, got $REPORTED"; exit 1
fi
if grep -qv "exit=0" shard-results.txt; then
  echo "One or more shards had failing tests"; exit 1
fi
echo "All $SHARDS shards passed"
