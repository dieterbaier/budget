# General Semantic Contracts

This file is a thin project entry point. It does **not** restate the
architecture-knowledge-toolkit's semantic contracts; it delegates to them.

## Authoritative Source

The authoritative general semantic contracts are the toolkit's
`general-semantic-contracts.md`. Resolve it through the lookup order in
`AGENTS.md`:

1. `$ARCHITECTURE_KNOWLEDGE_TOOLKIT` if set.
2. The nearest local `architecture-knowledge-toolkit` checkout found by searching
   upward from this project directory.
3. The project-local recorded toolkit reference in `AGENTS.md` (local checkout at
   `../docs-as-code-toolkit/architecture-knowledge-toolkit`, pinned public
   reference `docs-as-code-toolkit/architecture-knowledge-toolkit@0e162fc0e00ba92876cc8c4459ed9a3ecf5eb8cd`).
4. The public repository
   `https://github.com/docs-as-code-toolkit/architecture-knowledge-toolkit`.

Read the toolkit `general-semantic-contracts.md` before creating or changing
architecture content. Read
`src/docs/arc42/04-solution-strategy/doc-04001-metamodel.adoc` (when present in
the toolkit or this repository) before creating or adapting artifact metadata,
artifact types, lifecycle states, relation semantics, metamodel schemas,
validators, or generator inputs.

## Precedence

When project conventions and toolkit conventions conflict, apply this order:

1. Explicit user instructions.
2. This project `AGENTS.md`.
3. Relevant toolkit skill instructions.
4. Toolkit `general-semantic-contracts.md`.

## Project-Local Additions

These are the only project-specific rules; everything else defers to the toolkit
contract. They **extend** the toolkit and do not duplicate or override it.

- All architecture artifacts in this repository were AI-bootstrapped and are
  `draft`/`proposed` with `reviewed: false`. Do not mark any artifact `reviewed`
  or `accepted` without recorded human acceptance.
- Draft prose was seeded from the toolkit private-budget example (same domain)
  and must be reviewed against the real product intent; see
  `src/docs/doc-005-questions-and-answers.adoc`.
- The vendored tooling under `metamodel/`, `templates/`, and `scripts/`, plus
  `build.sh`, is copied from the toolkit and kept in sync with the recorded
  reference. Do not hand-edit it to diverge from the toolkit; update the pin
  instead.
- This project has no local `skills/`; all architecture and SDLC method guidance
  is delegated to the toolkit.
