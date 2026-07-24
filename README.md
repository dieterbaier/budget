# Budget

Private, single-household budgeting application: offline-first mobile expense
capture with a backend that owns budget calculation.

This repository was bootstrapped with the
[architecture-knowledge-toolkit](https://github.com/docs-as-code-toolkit/architecture-knowledge-toolkit)
`bootstrap-project` skill. It currently contains **architecture documentation
only** (no application code yet).

> **All architecture content is an AI-generated draft** (`draft`/`proposed`,
> `reviewed: false`). It was seeded from the toolkit's private-budget example and
> must be reviewed against the real product intent before acceptance. Open
> assumptions and required human decisions are tracked in
> `src/docs/doc-005-questions-and-answers.adoc`.

## Layout

- `src/docs/doc-001-arc42.adoc` — assembled arc42 architecture entry point.
- `src/docs/arc42/` — arc42 chapters as addressable source artifacts, with ADRs
  (chapter 9), quality scenarios (chapter 10), and risks (chapter 11).
- `src/docs/canvas/`, `doc-002-vision-mission.adoc`, `doc-004-roadmap.adoc`,
  `doc-005-questions-and-answers.adoc` — product clarification.
- `metamodel/`, `templates/`, `scripts/`, `build.sh` — executable tooling
  vendored from the toolkit (kept in sync; see the recorded reference in
  `AGENTS.md`).
- `adapters/` — generated thin agent routing wrappers (do not edit by hand).
- `AGENTS.md`, `.github/copilot-instructions.md`, `general-semantic-contracts.md`
  — thin AI contracts that delegate method guidance to the toolkit.

## Validate and generate

```sh
ruby scripts/validate-metamodel.rb \
  --docs src/docs \
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
