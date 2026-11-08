#!/usr/bin/env bash
# Container entrypoint. Runs one shard and copies results to RESULTS_DIR (if set).
#   TEST_GROUPS  : TestNG groups, e.g. "smoke" or "regression" (empty = all)
#   SHARD_TOTAL / SHARD_INDEX : this runner's slice of the suite (see ShardInterceptor)
#   RESULTS_DIR  : e.g. /results (volume). Output goes to $RESULTS_DIR/$RUN_ID/shard-$SHARD_INDEX
#   K8S_MODE=true: always exit 0 so one failing shard does not make Kubernetes kill the others;
#                  the pipeline reads the SHARD_RESULT log line instead (scripts/k8s-run.sh).
INDEX="${SHARD_INDEX:-0}"
RUN_ID="${RUN_ID:-local}"

ARGS=()
if [[ -n "${TEST_GROUPS:-}" ]]; then ARGS+=("-Dgroups=${TEST_GROUPS}"); fi

mvn -B test "${ARGS[@]}"
STATUS=$?

if [[ -n "${RESULTS_DIR:-}" ]]; then
  DEST="${RESULTS_DIR}/${RUN_ID}/shard-${INDEX}"
  mkdir -p "$DEST"
  cp -r target/allure-results "$DEST/" 2>/dev/null || true
  cp -r target/surefire-reports "$DEST/" 2>/dev/null || true
  cp -r target/traces "$DEST/" 2>/dev/null || true
fi

echo "SHARD_RESULT index=${INDEX} exit=${STATUS}"
if [[ "${K8S_MODE:-false}" == "true" ]]; then exit 0; fi
exit $STATUS
