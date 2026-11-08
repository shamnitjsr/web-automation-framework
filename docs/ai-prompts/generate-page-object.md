# Prompt: draft a Page Object + test from a manual test case

Context for the model (paste once per session):
- Stack: Java 17, Playwright for Java, TestNG, Page Object Model. Follow the existing classes in
  `src/main/java/.../pages` and `BasePage` (use its click/fill/getText helpers, no Thread.sleep).
- Locators: prefer getByRole / getByLabel / getByTitle; fall back to stable ids. Use the Playwright MCP
  server to open the page and read the accessibility snapshot BEFORE proposing locators.
- Pages hold locators and actions only. Assertions live in tests. Reusable UI parts go in `components/`.
- Annotate each test with `@TestCaseId("TC_XX_NNN")` and a group: smoke or regression.
- Never use real credentials; read them through `ConfigReader`.

Task:
Here is a manual test case (ID, steps, test data, expected result):
<paste the Excel row>

1. List which steps are automatable and which are not (and why).
2. Propose or extend the page object(s).
3. Write the TestNG test. Output code only, then a short list of assumptions.

A human reviews and runs every generated change before it is merged.
