# Official Playwright Java image: JDK + Maven + browsers + OS dependencies already installed.
# The tag MUST match <playwright.version> in pom.xml, otherwise Playwright can't find its browsers.
ARG PLAYWRIGHT_VERSION=1.49.0
FROM mcr.microsoft.com/playwright/java:v${PLAYWRIGHT_VERSION}-jammy

ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
WORKDIR /app

# 1) dependencies first -> cached unless pom.xml changes
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 2) sources -> compile once at build time so pods start fast
COPY testng.xml ./
COPY src ./src
RUN mvn -B -q test-compile

COPY scripts/run-tests.sh ./run-tests.sh
RUN chmod +x run-tests.sh

ENTRYPOINT ["./run-tests.sh"]
