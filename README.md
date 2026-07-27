# Budget

Private, **single-user** budgeting application: an actuals tracker that replaces
a Notion budget. The owner records transactions from bank statements, organized
by category and category group across three accounts; a server-owned domain
computes monthly expenditure, account reserves, a cash runway, and a pension
projection, rendered by an installable React PWA.

## Two goals

The project has a second purpose beside the product, and it is worth stating up
front because it explains the shape of this repository.

1. **Build the budget application** described above — the functional goal.
2. **Prove the [architecture-knowledge-toolkit](https://github.com/docs-as-code-toolkit/architecture-knowledge-toolkit)
   on real work** — whether the method holds up outside its own examples, and how
   it actually helps day-to-day architectural work.

The toolkit was not merely used to bootstrap this repository; exercising it is an
ongoing objective. That is why a single-user budget tracker carries more recorded
decisions, constraints and fitness functions than its size alone would justify:
the density is how the method's strengths and weaknesses surface.

It is not a licence for ceremony. Artifact work earns its place when it produces
a *finding* — something learned about the method, or a decision a later reader
would otherwise get wrong. Findings flow back upstream as issues against the
toolkit, and the same question is asked of every new artifact: would someone
without it probably decide wrongly or miss an assumption?

This repository holds architecture documentation under `docs/` and the
application under `application/`.

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
- `application/web/` — the React + TypeScript PWA (Vite), the single client for
  desktop and mobile (`cd application/web && npm test`).
- `metamodel/`, `templates/`, `scripts/`, `build.sh` — executable documentation
  tooling vendored from the toolkit (kept in sync; see the recorded reference in
  `AGENTS.md`).
- `adapters/` — generated thin agent routing wrappers (do not edit by hand).
- `AGENTS.md`, `.github/copilot-instructions.md`, `general-semantic-contracts.md`
  — thin AI contracts that delegate method guidance to the toolkit.

## Backlog

Planned work is tracked as GitHub issues:
**<https://github.com/dieterbaier/budget/issues>**. Epics carry their slices as
real sub-issues, and every issue is typed and labelled per the toolkit's
`skills/references/issue-labels.md` taxonomy (`type:` and `area:` prefixes), so
the backlog can be filtered without reading issue bodies.

Product and architecture rationale stays in `docs/` — the roadmap in
`docs/doc-004-roadmap.adoc` and the open questions in
`docs/doc-005-questions-and-answers.adoc`. Issues reference those records rather
than restating them.

Work starts from an issue and lands on `main` through a rebased pull request —
see [CONTRIBUTING.md](CONTRIBUTING.md).

## Run the application

The application lives under `application/`. The backend is runnable today; the
web PWA and any further parts are documented here as the project evolves.

### Run the backend locally

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
# read the current monthly expenditure
curl 'http://localhost:8080/api/monthly-expenditure?month=2026-07'
# {"month":"2026-07","variableCosts":900.00,"fixedCostsMonthly":100.00,
#  "total":1000.00,"averageIncome":950.00,"difference":50.00,"overspending":true}

# record a transaction (the category must already exist)
curl -X POST 'http://localhost:8080/api/transactions' \
  -H 'Content-Type: application/json' \
  -d '{"date":"2026-07-15","amount":42.00,"category":"Groceries","type":"EXPENSE"}'
```

Stop and clean up with `docker compose down` (or `podman compose down`). Tests
run without any of this — they use Testcontainers (`./gradlew test`).

### Run the web PWA locally

With the backend running, start the Vite dev server. It proxies `/api` to
`http://localhost:8080`, so the PWA is same-origin in dev (no CORS) and in prod:

```sh
cd application/web
npm install
npm run dev          # http://localhost:5173
```

`npm test` runs the component tests (Vitest + Testing Library); `npm run build`
produces the installable PWA (service worker + manifest) in `dist/`.

## Validate and generate the documentation

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

Generated AsciiDoc fragments are written next to their sources under each
chapter's `generated/` directory (these are derived output, not edited by hand).
`./build.sh build` additionally renders the assembled arc42 documentation to
`build/architecture/index.html` — open that file in a browser to read it as a
rendered artifact.

The same `./build.sh build` runs in CI on every push to `main` that touches the
documentation sources, and the result is published to GitHub Pages:
**<https://dieterbaier.github.io/budget/>** (see
`.github/workflows/publish-architecture-docs.yml`). Because the build validates
before it renders, a broken model fails the workflow instead of publishing.

## AI agent adapters

```sh
node scripts/build-agent-adapters.js   # regenerate adapters/
node scripts/check-agent-adapters.js   # fail if adapters are stale
```

Generated files under `**/generated/` and the `adapters/` wrappers are derived
output, not primary editing surfaces. Edit source artifacts or the generators and
regenerate.

## License

Source code and architecture documentation in this repository are licensed under
the [MIT License](LICENSE). The application is *about* private data, but no
budget data lives here — the repository contains only throwaway local
development credentials and sample data.
