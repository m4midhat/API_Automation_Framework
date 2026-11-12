# REST API Automation Framework

**Author:** Midhat Rosull Chughtai · **Email:** m4midhat@gmail.com · **GitHub:** github.com/m4midhat
**Purpose:** API test cases reporting

A RestAssured + TestNG API test automation framework built against open-source, public sandbox
APIs for portfolio demonstration. Every tool in the stack - RestAssured, TestNG, Allure, Maven,
Jackson, Jenkins/Azure DevOps integration - matches the "API & Backend" and "Test Automation"
skills on the accompanying resume; no tool outside that stack was introduced.

## APIs under test

| API | Base URL | What it exercises |
|---|---|---|
| [ReqRes](https://reqres.in) | `https://reqres.in/api` | User CRUD, pagination, register/login |
| [Restful-Booker](https://restful-booker.herokuapp.com) | `https://restful-booker.herokuapp.com` | Booking CRUD, token auth, health check |
| [JSONPlaceholder](https://jsonplaceholder.typicode.com) | `https://jsonplaceholder.typicode.com` | Posts/comments CRUD, nested resources |

All three are free, open sandboxes meant for exactly this kind of practice. Known quirks of each
are called out in the relevant test's `description` and, where relevant, below.

**ReqRes daily quota**: anonymous (keyless) requests are capped at 40/day per IP. This suite's
ReqRes tests alone make ~35 requests, so a second run on the same day from the same IP may see
`429 rate_limit_exceeded`. Sign up for a free key at https://app.reqres.in (250 requests/day) and
set it via `-Dreqres.api.key=...` or the `REQRES_API_KEY` env var - no code change needed.

**Restful-Booker**: a single free-tier Heroku instance, deliberately seeded with a few bugs for
practice (see its own docs). It also rate-limits aggressive bursts with `418 I'm a teapot`, and
requires a single-value `Accept: application/json` header (not a weighted multi-value list) or it
returns 418 regardless of load - both are handled by this framework already.

## Test count

96 executed test cases total (TestNG `@Test` invocations, data-provider rows included):

| Suite | Classes | Test cases |
|---|---|---|
| ReqRes | 7 | 36 |
| Restful-Booker | 6 | 37 |
| JSONPlaceholder | 5 | 23 |
| **Total** | **18** | **96** |

## Stack

- **Java 17**, **Maven** - build and dependency management
- **RestAssured** (+ `json-schema-validator`) - HTTP client and response/schema assertions
- **TestNG** - test runner, data providers, retry analyzer, parallel execution
- **Allure** (`allure-testng`, `allure-rest-assured`) - HTML test report with request/response
  capture per step
- **ExtentReports** (`extentreports`, Spark reporter) - second, standalone HTML test report
- **Jackson** - request/response POJO (de)serialization
- AES-256-GCM via the JDK's own `javax.crypto` - credential encryption at rest (no third-party
  crypto library)

## Project layout

```
src/main/java/com/entertainer/qa/
  config/       ConfigManager          - CLI / env var / properties-file resolution
  security/     CredentialCipher       - AES-256-GCM encrypt/decrypt + CLI entry point
                CredentialManager      - decrypts ENC(...) config values on demand
  clients/      BaseApiClient, ReqResClient, BookerClient, JsonPlaceholderClient
  models/       Request/response POJOs, one package per API
  utils/        TestDataFactory        - unique request payload generation
  reporting/    ExtentManager          - ExtentReports instance + per-thread test node
  listeners/    RetryAnalyzer, RetryAnnotationTransformer, SuiteLifecycleListener,
                TestExecutionListener, ExtentReportListener

src/test/java/com/entertainer/qa/
  base/         BaseTest               - shared client wiring + Booker auth helper
  tests/reqres, tests/booker, tests/jsonplaceholder

src/test/resources/
  config/       qa.properties, staging.properties
  schemas/      JSON Schema files used by schema-validation tests

testng.xml       Suite definition (parallel per API host)
Jenkinsfile, azure-pipelines.yml   CI/CD pipeline examples
```

## Running locally

```bash
mvn test
```

Runs against the `qa` environment by default. Switch environment or any config value from the
CLI without touching a file:

```bash
mvn test -Denv=staging
mvn test -Dbooker.base.url=https://restful-booker.herokuapp.com
```

Run a single class or a subset:

```bash
mvn test -Dtest=AuthTests,CreateBookingTests -DfailIfNoTests=false
```

### Listeners

All suite-wide listeners are registered in `testng.xml`'s own `<listeners>` block (not in
`pom.xml`), so the suite behaves identically whether it's run via `mvn test`, an IDE's TestNG
runner, or `java org.testng.TestNG testng.xml` directly:

| Listener | Interface | Responsibility |
|---|---|---|
| `AllureTestNg` | - | Allure result generation (library-provided) |
| `RetryAnnotationTransformer` | `IAnnotationTransformer` | wires `RetryAnalyzer` onto every `@Test` automatically |
| `SuiteLifecycleListener` | `ISuiteListener` | suite start/finish logging, writes Allure's `environment.properties`, flushes the ExtentReports HTML |
| `TestExecutionListener` | `ITestListener` | per-test start/pass/fail/skip console logging, useful in CI logs before either report is opened |
| `ExtentReportListener` | `ITestListener` | drives the ExtentReports HTML report - one node per test, categorised by API, tagged with the report author |

Note: Maven's `-Dtest=ClassName` shortcut makes Surefire build an ad-hoc suite that bypasses
`testng.xml` entirely (listeners included) - fine for a quick local spot-check, but always run
plain `mvn test` (or point `-Dsuite` at a real suite XML file) to get listener output, retries, and
parallel execution as configured.

### Configuration resolution order

Every value read through `ConfigManager` is resolved, highest priority first:

1. JVM system property - `-Dkey=value`
2. Environment variable - upper-snake-cased (`booker.auth.username` → `BOOKER_AUTH_USERNAME`)
3. `src/test/resources/config/<env>.properties`

This is what lets a CI/CD agent (Jenkins, Azure DevOps, GitHub Actions, ...) inject real secrets
and target hosts via pipeline variables with zero code changes - see `Jenkinsfile` and
`azure-pipelines.yml` for working examples.

## Encrypted credentials

Restful-Booker's demo credentials are stored encrypted in `config/*.properties` as `ENC(...)` and
decrypted only at the point of use via `CredentialManager`:

```properties
booker.auth.username=ENC(rTZtycka27+5MSLPnXJvTPj4o/+alTRZMoeoQ5gX0fj5W6Pso49cAyMbmag2A0Ffyw==)
```

The decryption passphrase is never committed - it is supplied at run time:

```bash
mvn test -DencryptionKey=your-passphrase-here
# or
export ENCRYPTION_KEY=your-passphrase-here && mvn test
```

Without a passphrase, the suite falls back to a documented insecure local-dev default so it still
runs out of the box against these public sandboxes - never rely on that fallback outside this use
case (a startup warning is printed every time it's used).

To encrypt a new value (e.g. after rotating the demo password, or wiring the framework up against
a real environment with real credentials):

```bash
mvn -q compile
java -cp target/classes com.entertainer.qa.security.CredentialCipher encrypt myPassword myPassphrase
# prints ENC(...) - paste it into the properties file or a CI/CD secret variable
```

A CI/CD pipeline may instead inject a credential as **plain text** from its own secret store
(`-Dbooker.auth.password=$SECRET`); `CredentialManager` uses it as-is since the secret manager has
already handled encryption at rest.

## Reporting

Every run produces two independent HTML reports:

### Allure

```bash
mvn test                 # writes raw results to target/allure-results
mvn allure:report         # builds the static HTML report in target/site/allure-maven-plugin
mvn allure:serve          # builds and opens the report in a browser
```

Records which environment it ran against (via `SuiteLifecycleListener`) and includes the full
request/response for every API call (via the `AllureRestAssured` filter).

### ExtentReports

```bash
mvn test                 # also writes target/extent-report/ExtentReport.html directly - open it in a browser, no extra command needed
```

A single self-contained HTML file (Spark reporter), grouped by API (ReqRes / Restful-Booker /
JSONPlaceholder via `assignCategory`) with a system-info panel showing:

| Field | Source |
|---|---|
| Author, Email, GitHub, Purpose | `report.author`, `report.author.email`, `report.author.github`, `report.purpose` in `config/*.properties` |
| Environment, base URLs | the same `env`/`*.base.url` values the test run itself used |
| Test Framework, Java Version, Operating System, Execution Date | gathered automatically at report creation |

Override the output path/title/name/theme the same way as any other config value:

```bash
mvn test -Dextent.report.path=target/extent-report/CustomName.html -Dextent.report.theme=dark
```

## CI/CD

`Jenkinsfile` and `azure-pipelines.yml` both: install nothing but Maven/JDK, run `mvn test` with
the environment and encrypted-credential passphrase supplied as pipeline parameters/variables, and
publish both the Allure and ExtentReports HTML reports as build artifacts (the latter needs no
separate build step - `mvn test` writes it directly). Copy whichever matches your pipeline tool and
wire in your own credential variables. Both simply invoke `mvn test`, so they pick up
`testng.xml`'s listeners (and anything else configured there) with no pipeline-side changes
required beyond what's already there for Extent's output path.

## Project metadata

`pom.xml` declares a few informational properties alongside the usual build config - `author`,
`author.email`, `author.github`, `project.purpose` - plus the conventional convenience flags
`skipTests` / `maven.test.skip` (toggle from the CLI without editing the file) and
`project.reporting.outputEncoding` (paired with `project.build.sourceEncoding`).

The same four values are set independently in `config/*.properties` as `report.author`,
`report.author.email`, `report.author.github`, `report.purpose` - that copy is the one
`ExtentManager` actually reads at run time (through `ConfigManager`, so it's overridable from the
CLI/env like everything else), since build-time POM properties aren't available inside the test
JVM without extra Maven resource filtering this project doesn't otherwise need.
