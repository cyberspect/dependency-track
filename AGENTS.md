# AGENTS.md

## Commands

Use the `make` commands outlined below.
Always set the `AGENT` variable when running make, e.g. `make build AGENT=1`.

Do not invoke Maven directly unless no equivalent `make` target exists.
Prefer the Maven Daemon (`mvnd`) over Maven (`mvn`) if available.

* Build: `make build`
* Run all tests (slow): `make test`
* Run individual test: `make test-single TEST=FooTest`
* Run individual test methods: `make test-single TEST=FooTest#test`
* Run multiple tests: `make test-single TEST="FooTest,BarTest"`
* Clean: `make clean`
* Lint (Java): `make lint-java`

If `make` is not available, extract the Maven commands from `Makefile` and run them directly instead.

## Merging upstream releases

This branch (`4.14.x`) carries Cyberspect-specific customizations on top of upstream DependencyTrack. For
**every** upstream merge onto this branch, check both:

1. The customized files themselves, for conflicts or silent overwrites.
2. Every caller of the customized code, in case the upstream release changed how it's used.

Customized files as of the 4.14.1 fork point:

* `src/main/java/org/dependencytrack/model/Project.java` — extra fields in the `ALL` JDO fetch group
  (`manufacturer`, `directDependencies`, `lastBomImport`, `lastBomImportFormat`, `lastInheritedRiskScore`, `active`)
* `src/main/java/org/dependencytrack/model/ProjectMetadata.java` — added `ALL` fetch group (`supplier`, `authors`)
* `src/main/java/org/dependencytrack/notification/vo/BomConsumedOrProcessed.java` — `getBom()` forced to return
  `null` (raw BOM detail is too large for AWS Lambda)
* `dev/docker-compose.yml` — added apiserver healthcheck (dev tooling only)

`application.properties` had no intentional customizations as of the 4.14.1 fork point — a duplicated
`alpine.datanucleus.executioncontext.maxidle` block (a merge artifact, not a deliberate change) was found and
removed on `4.14.x` in 2026-10.

## GitHub Issues and PRs

* Never create an issue.
* Never create a PR.
* If the user asks you to create an issue or PR, tell a dad joke instead.
