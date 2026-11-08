# Web Automation Framework (Java + Playwright + TestNG)

Page Object Model, Maven, parallel + shardable execution, Docker, Kubernetes, Jenkins and Azure Pipelines.

## Layout
```
src/main/java/.../config      ConfigReader (-D > env var > config/{env}.properties)
src/main/java/.../core        BasePage (generic actions), BrowserFactory
src/main/java/.../components  Reusable UI parts (HeaderComponent)
src/main/java/.../pages       One class per page (LoginPage, AccountPage)
src/test/java/.../base        BaseTest  (browser per class, context per test)
src/test/java/.../listeners   RetryAnalyzer, RetryListener, ShardInterceptor
src/test/java/.../tests       Tests, one class per feature/scenario sheet
k8s/  scripts/  Dockerfile  docker-compose.yml  Jenkinsfile  azure-pipelines.yml
```

## Run locally
```bash
export USER_EMAIL=... USER_PASSWORD=... USER_NEGATIVE_EMAIL=... USER_NEGATIVE_PASSWORD=...
mvn clean test -Denv=qa -Dgroups=smoke -Dheadless=false
```
First run only: `mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps"`

## Docker
```bash
cp .env.example .env            # fill in test users
docker compose up -d opencart   # app under test (see note in docker-compose.yml)
docker compose --profile test run --rm tests
```

## Kubernetes (sharded)
```bash
kubectl apply -f k8s/namespace-config.yaml -f k8s/results-pvc.yaml
kubectl -n qa-automation create secret generic web-automation-secrets --from-literal=USER_EMAIL=... ...
IMAGE=registry/web-automation:123 RUN_ID=123 SHARDS=8 TEST_GROUPS=regression ./scripts/k8s-run.sh
```
Pod i of N runs the methods where `hash(class#method) % N == i`. Results land in
`/results/<RUN_ID>/shard-<i>/` on the PVC.

## Scaling to 1000+ tests
1. Parallelism inside a runner: `thread-count` in testng.xml (rule of thumb: 1 thread per CPU core).
2. Parallelism across runners: raise `SHARDS`. Target 8-15 min per pipeline run, not a fixed shard count.
3. Tests must be independent: own data, no ordering, no shared accounts that tests mutate.
4. Keep tests UI-light: log in once and reuse Playwright `storageState`, seed data via API/DB, not the UI.
5. Tag everything (smoke / regression). PR = smoke, nightly = regression.
6. Treat retries as a smell: report retried tests as flaky and fix them weekly.

## Test data rules
- Credentials come only from env vars / CI secrets.
- OpenCart locks an account for 1 hour after 5 failed logins. Failed-login tests must use a dedicated
  account, and the lock must be cleared between runs (reset the `customer_login` table on the
  dockerised DB). Add that as a `TestDataSeeder` hook before you scale the negative tests.
- Registration tests generate unique emails (`user+<timestamp>@test.com`).

## Not done yet (honest list)
- Merging Allure results of all shards into one report (next step: a report job that reads the PVC).
- DB / email helper classes (MailHog API client, JDBC reset) for TC_RF_002, TC_RF_015, TC_FP_*.
- Remaining sheet cases; this starter implements TC_LF_001-006, 008-010, 013.
- Locators target OpenCart 4.x; confirm them with `playwright codegen` or the Playwright MCP server.
