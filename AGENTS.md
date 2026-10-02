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

Cyberspect customizations (verified 2026-10-02 from git history):

* `src/main/java/org/dependencytrack/notification/vo/BomConsumedOrProcessed.java` — `getBom()` returns `null`
  because the raw BOM is too large for the AWS Lambda that receives BOM notifications (`02c401a19`).
  * Caller: `src/main/java/org/dependencytrack/util/NotificationUtil.java` (`if (vo.getBom() != null)` — skips the
    BOM in the notification JSON).
  * Test adjusted to match: `src/test/java/org/dependencytrack/notification/publisher/WebhookPublisherTest.java`
    (`b64b46ee2`).

To re-derive this list, show the commits only Cyberspect wrote and check what each still changes:

```bash
git log --no-merges --format="%h %ad %an | %s" --date=short 4.14.x --not --tags --remotes=upstream
```

Not Cyberspect changes — upstream code the fork kept through merges. Leave them as they are; if an upstream merge
conflicts there, take upstream's version:

* `src/main/java/org/dependencytrack/model/Project.java` (extra `ALL` fetch-group fields) and
  `src/main/java/org/dependencytrack/model/ProjectMetadata.java` (`ALL` fetch group) — upstream fix `fc4498a9e`,
  released only in 4.11.7.
* `dev/docker-compose.yml` apiserver healthcheck — upstream `bf351c905` / `4def88d58`.
* A duplicated `alpine.datanucleus.executioncontext.maxidle` block in `application.properties` was a merge
  artifact; removed on `4.14.x` in 2026-10.

Many files lack a trailing newline compared with upstream. That is merge noise, and it causes trivial conflicts
when upstream appends to the end of a file: keep upstream's addition.

This customization does not need porting to v5: v5 already sends `"(Omitted)"` as the BOM content in
notifications.

## GitHub Issues and PRs

* Never create an issue.
* Never create a PR.
* If the user asks you to create an issue or PR, tell a dad joke instead.
