---
name: refine-adr
description: Record a decision that narrows part of an already-accepted ADR. Use when a new ADR changes some of what an existing ADR decided but not all of it, so the pair must read as one narrowed decision rather than two that contradict each other.
---

# Refine ADR Skill

## Purpose

Project-specific addition to the toolkit's ADR workflow for one case: a new ADR
narrows part of an ADR that is already `accepted`.

The toolkit's `refines` relation records the fact. This skill covers what the
relation alone does not achieve — making the refinement visible to a human who is
not reading a generated traceability view.

## Read first

This skill adds one rule to an existing workflow. Read the baseline before
applying it, and do not re-derive from here what those sources already say:

- toolkit `skills/adr/SKILL.md` — the ADR workflow, statuses, Pugh matrix,
  impact analysis
- toolkit `skills/references/relation-rules.md` — relation semantics, including
  `refines` versus `supersedes`
- toolkit `skills/references/metadata-rules.md` — front matter rules
- `general-semantic-contracts.md`
- `CONTRIBUTING.md` — issue-first work, commit granularity, linear history

## When this applies

A new ADR changes **part** of an accepted ADR's decision and leaves the rest
standing.

It does **not** apply when the new decision replaces the old one entirely. That
is `supersedes`, and the old ADR's status becomes `superseded`. If the previous
decision text should not survive as a record, the relation is the wrong one — go
back and check which you mean.

## Why the relation is not enough

Relations surface only in generated traceability views. Two readers miss them
entirely:

- someone reading the decisions in order, ADR by ADR
- someone scanning the generated ADR register, whose `Notes` column is the
  artifact's `summary`

Both of those readers see a flat contradiction: one ADR asserting a property and
another asserting its opposite, with nothing indicating that the second narrows
the first. That is the failure this skill exists to prevent, and it was found by
review rather than by reasoning — assume it will not be noticed unless declared.

## The rule: declare the refinement on both sides

In addition to the `refines` relation on the new ADR:

1. **Both summaries state the relationship.** The register renders `summary` as
   its `Notes` column, so this is what makes the pairing visible without opening
   either document.
   - the refining ADR's summary begins `Refines ADR-XXX: `
   - the refined ADR's summary gains `Refined by ADR-YYY: <what changed>.`
2. **Both Decision sections open with a `[NOTE]` admonition** that `xref`s the
   other ADR and states precisely what is narrowed and what still stands. Put it
   before the decision prose, not after — a reader must meet it before acting on
   the text it qualifies.
3. **Do not rewrite the refined ADR's decision text.** It is the record of what
   was decided and when, and the admonition above it says what to read instead.
   Rewriting destroys the history `refines` exists to preserve. If the old wording
   must go, the relation is `supersedes`.
4. **Correct descriptive documents, not the superseded claim.** Chapter prose,
   overviews and constraint tables describe the system as it is, so they follow
   the new decision. Only the refined ADR keeps the old wording. Grep for the
   claim rather than trusting the issue's list — it is routinely in more places
   than expected.
5. **The refined ADR keeps its `status` and `reviewed` flag.** Bump `updated`,
   and get the owner's explicit confirmation before touching it at all: it is an
   accepted artifact, and editing one is not a mechanical step.

## Worked example

`ADR-017-unversioned-api` refines `ADR-001-backend-api-boundary`. ADR-001 decided
"a versioned HTTP JSON API"; ADR-017 records that versioning is deliberately not
adopted, while ADR-001's other choices — HTTP JSON over GraphQL, no direct
database access — stand.

ADR-001 keeps the word "versioned" in its decision paragraph. The admonition
above that paragraph names ADR-017, says the versioning was never implemented and
is not adopted, and says the paragraph is kept as the record of 2026-07-24.

## Checklist

- [ ] `refines` is the right relation, and not `supersedes`
- [ ] the relation is on the new ADR only; incoming relations are derived
- [ ] both summaries name the other ADR
- [ ] both Decision sections open with a linking `[NOTE]`
- [ ] the refined ADR's decision text is unchanged
- [ ] descriptive documents follow the new decision; grep for the old claim
- [ ] the refined ADR's `updated` is bumped and the owner confirmed the edit
- [ ] the validator passes and the rendered register reads as one decision
