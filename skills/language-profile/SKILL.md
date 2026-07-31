---
name: language-profile
description: Choose the register a document is written in before writing it. Use when writing or revising an ADR, architecture constraint, quality scenario, risk, glossary entry, requirements text, README, CONTRIBUTING, the questions-and-answers document, a diary recap, a GitHub issue, a pull request body, a commit message, a code comment, Javadoc, or a Gherkin specification.
---

# Language Profile Skill

## Purpose

Project-specific addition covering one thing: the *register* each artifact here is
written in.

The toolkit's skills decide what an artifact must **contain**. This skill decides
how it **reads**. The two never overlap: if a rule is about structure, required
sections, metadata or relations, it belongs to the toolkit and not here.

It exists because the differences are load-bearing rather than stylistic. An ADR
that hedges has decided nothing. A diary entry that mocks a past belief destroys
the record of why the belief was plausible, which is the stated reason the diary
exists.

## Read first

This skill adds voice rules to existing workflows. Read the baseline before
applying it, and do not re-derive from here what those sources already say —
resolve them through the lookup order in `AGENTS.md`:

- `skills/adr/SKILL.md`, `skills/risk/SKILL.md`, `skills/quality-scenario/SKILL.md`
- `skills/commit-message/SKILL.md` — **owns the commit format entirely.** This
  skill adds nothing to it.
- `skills/pr-review/SKILL.md`
- `general-semantic-contracts.md`

## Picking a profile

Not by formal against informal. That is a social axis and it predicts none of the
rules below. Pick by **what happens to the text when it turns out to be wrong**:

| Fate | Artifacts | Profile |
|---|---|---|
| Superseded, never rewritten | ADR, constraint, quality scenario, risk | [1](#1-superseded) |
| Corrected in place, silently | Glossary, requirements overview, arc42 chapters, README, CONTRIBUTING | [2](#2-corrected-silently) |
| Corrected in place, visibly | `docs/doc-005-questions-and-answers.adoc` | [3](#3-corrected-visibly) |
| Never revised | `diary/**` | [4](#4-never-revised) |
| Corrected when a claim fails | Issue, pull request body, commit message | [5](#5-working-traffic) |
| Changes with the code | Code comment, Javadoc, Gherkin | [6](#6-code-and-specifications) |

An author who knows a text will never be edited again writes differently from one
who knows it always describes *now*. That difference is what the profiles encode.

## 1. Superseded

ADR, architecture constraint, quality scenario, risk.

Read by someone deciding years later whether this still applies, who was not
present when it was decided.

- **Name the deciding role and the date.** The role, not the person: owner,
  architect. A decision whose author is unrecoverable cannot be re-judged.
- **Active voice.** "It was decided", "es wurde entschieden" and their relatives
  are forbidden. They are the construction that makes a decision unattributable.
- **No hedging.** The Decision section says what holds, not what might be worth
  considering. "We could perhaps" is not a decision.
- **No blame.** Attribute the decision, never the error. Not "X got the caching
  wrong" but "the chosen caching strategy produced unexpected behaviour".
- **Self-contained.** Reference sibling artifacts by ID; do not restate them. A
  reader must not need the conversation that produced it.
- **Record the rejected alternative and why**, or it gets proposed again with its
  refutation invisible.
- Present tense for the rule, past tense for the history.

## 2. Corrected silently

Glossary, requirements overview, arc42 chapter documents, README, CONTRIBUTING.

Read by someone who needs to act now.

- **Present tense, describing what is.** No "we then decided to" — that is what
  an ADR is for, and duplicating it means two artifacts own one truth.
- **No dates in the body.** They go stale invisibly. The date belongs in the
  frontmatter.
- **Define once.** A term is defined in the glossary and linked from everywhere
  else. Rationale lives in the ADR.
- README and CONTRIBUTING may carry voice; the glossary may not. That is a
  difference in density, not in register.

## 3. Corrected visibly

`docs/doc-005-questions-and-answers.adoc`.

Read by someone tracing why a decision reads the way it does.

- **An answer that turns out wrong is not overwritten.** A dated correction is
  added beneath it and the original stays legible.
- Identifiers are permanent. A question keeps its `Q-` id forever.
- Say what the earlier answer claimed, not only what is now true. The point of
  this document is the delta.

## 4. Never revised

`diary/**`.

Read by us, later, looking for why a mistake was plausible.

- **First person plural by default.** Use "I" only where the distinction carries
  the lesson — "I proposed 75 kB; then we did the arithmetic I had not". A blanket
  plural would hide who skipped the check.
- Past tense.
- **Unsparing about the error, generous about its plausibility.** Both halves are
  required. Naming the mistake without explaining why it convinced anyone leaves a
  record nobody can learn from.
- **No cynicism.** Mocking a past belief deletes exactly what the entry is for.
- Every claim checkable: dates, issue numbers, commit ids, quoted output.
- **A recap is open until the day it covers is closed. After that, append only.**
  What we believed and when is never revised; how it reads may be — voice,
  typography, a cross-reference. Changing a past claim, softening a past error or
  deleting an entry is forbidden; that becomes a new entry.

## 5. Working traffic

Issue, pull request body, commit message.

Read by a reviewer now, and by a stranger doing archaeology later.

- Commit format is `skills/commit-message/SKILL.md`. Not restated here.
- Say **why the change was worth making**, not what the diff already shows.
- Acceptance criteria are checkable by someone who did not write them.
- **Correct the claim, not only the code.** When a justification in an issue or a
  pull request turns out to be wrong, correct it there too — the issue text is
  what a later reader finds first.
- Do not sell. A body that overstates what a change achieves is a claim nobody
  will ever re-run.

## 6. Code and specifications

Code comments, Javadoc, Gherkin.

Read by whoever changes this next.

- **Explain the non-obvious reason, never the what.** `Category`'s Javadoc is the
  model here: it explains why identity-by-name and renaming can coexist, which no
  reader could derive from the code.
- Match the density and idiom of the surrounding code.
- Gherkin uses domain vocabulary only. No implementation terms.
- **A claim in a comment is a check nobody can run.** It gets the same scrutiny as
  an assertion, or it does not get written. A comment in
  `.github/workflows/pr-validation.yml` promised that a green check meant the
  deployment would render; it did not, and nothing could have caught it.

## Not covered here

- **Terminology.** Which word names a concept is ADR-022, and the glossary is the
  register. This skill only says: use the agreed term.
- **Interface text.** The interface language is ADR-023 and the wording is product
  work.
- **Commit message format.** The toolkit owns it.

## When this skill should be deleted

The *mechanism* — profiles chosen by revision fate — is general and belongs
upstream if a second project confirms it. The *profiles* are this project's and do
not.

So the upstream shape is the mechanism plus a requirement that each project
declare its own profiles. If the toolkit ever ships that, this file keeps only the
profile table and loses everything above it. If the toolkit ships profiles too,
delete this file.

One project is not evidence. This should survive longer before it is offered.

## Baseline

Written against toolkit `de7c2ad`. If the toolkit skills listed under
[Read first](#read-first) have moved since, check that the delta above still only
adds and never contradicts.
