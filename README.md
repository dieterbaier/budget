# Budget

Private, **single-user** budgeting application: an actuals tracker that replaces
a Notion budget. The owner records transactions from bank statements, organized
by category and category group across three accounts; a server-owned domain
computes monthly expenditure, account reserves, a cash runway, and a pension
projection, rendered by an installable React PWA.

This repository was bootstrapped with the
[architecture-knowledge-toolkit](https://github.com/docs-as-code-toolkit/architecture-knowledge-toolkit)
`bootstrap-project` skill. It holds architecture documentation under `docs/` and
the application under `application/` (currently a Spring Boot backend walking
skeleton).

> The initial content was AI-generated from the toolkit's private-budget
> example. On 2026-07-24 the owner reviewed the scope and made the first product
> and technology decisions (see `docs/arc42/09-architecture-decisions/` and
> `docs/doc-005-questions-and-answers.adoc`); content grounded in those
> decisions is marked `reviewed`/`accepted`. Newly derived or still-uncertain
> content remains `draft`/`proposed` until confirmed. Remaining open items are
> tracked in `docs/doc-005-questions-and-answers.adoc`.

## Layout

- `docs/doc-001-arc42.adoc` — assembled arc42 architecture entry point.
- `docs/arc42/` — arc42 chapters as addressable source artifacts, with ADRs
  (chapter 9), quality scenarios (chapter 10), and risks (chapter 11).
- `docs/canvas/`, `doc-002-vision-mission.adoc`, `doc-004-roadmap.adoc`,
  `doc-005-questions-and-answers.adoc` — product clarification.
- `application/backend/` — the Spring Boot (Java 21, Gradle) backend, built with
  its own toolchain (`cd application/backend && ./gradlew test`).
- `metamodel/`, `templates/`, `scripts/`, `build.sh` — executable documentation
  tooling vendored from the toolkit (kept in sync; see the recorded reference in
  `AGENTS.md`).
- `adapters/` — generated thin agent routing wrappers (do not edit by hand).
- `AGENTS.md`, `.github/copilot-instructions.md`, `general-semantic-contracts.md`
  — thin AI contracts that delegate method guidance to the toolkit.

## Run the backend locally

The backend needs a PostgreSQL. Start it from the compose file, then run the app
with the `local` profile (which points at `localhost:5432` and seeds sample
data). Works with Docker or Podman — use whichever `compose` you have:

```sh
cd application/backend
docker compose up -d        # or: podman compose up -d
./gradlew bootRun           # activates the `local` profile automatically
```

Then query the current monthly expenditure:

```sh
curl 'http://localhost:8080/api/monthly-expenditure?month=2026-07'
# {"month":"2026-07","variableCosts":900.00,"fixedCostsMonthly":100.00,
#  "total":1000.00,"averageIncome":950.00,"difference":50.00,"overspending":true}
```

Stop and clean up with `docker compose down` (or `podman compose down`). Tests
run without any of this — they use Testcontainers (`./gradlew test`).

## Validate and generate

```sh
ruby scripts/validate-metamodel.rb \
  --docs docs \
  --relations-schema metamodel/relations.schema.yaml \
  --generate
```

Or via the docs-toolbox task runner (pinned container image):

```sh
./build.sh validate     # validate metadata + relations
./build.sh generate     # validate, then regenerate derived fragments
./build.sh build        # generate + render architecture HTML
DOCS_TOOLBOX_LOCAL=1 ./build.sh generate   # use host toolchain instead
```

## Agent adapters

```sh
node scripts/build-agent-adapters.js   # regenerate adapters/
node scripts/check-agent-adapters.js   # fail if adapters are stale
```

Generated files under `**/generated/` and the `adapters/` wrappers are derived
output, not primary editing surfaces. Edit source artifacts or the generators and
regenerate.
