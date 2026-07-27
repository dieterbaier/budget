# Contributing

This is a single-user project, but it is worked on by both a human and AI
agents, so the workflow is written down rather than remembered.

The *how* — branching, committing, opening and reviewing pull requests — belongs
to the [architecture-knowledge-toolkit](https://github.com/docs-as-code-toolkit/architecture-knowledge-toolkit)
and is not restated here. See `AGENTS.md` for how the toolkit is resolved. This
file records only what is specific to **this** repository.

## Work starts from an issue

The backlog lives in [GitHub issues](https://github.com/dieterbaier/budget/issues).
Epics carry their slices as real sub-issues; every issue is typed and labelled
per the toolkit's `skills/references/issue-labels.md` taxonomy.

Open an issue before starting anything non-trivial, so the branch, the commits,
and the pull request all have a number to reference. Product and architecture
rationale stays in `docs/` — issues link to those records instead of restating
them.

## Branch, commit, pull request

- Branch from an up-to-date `main`, named `issue_<number>`.
- Commit messages follow the toolkit's `skills/commit-message/SKILL.md`:
  `issue_<number>: <imperative summary>`, 50 characters or less on the first
  line, with the body explaining *why*.
- Every change arrives on `main` through a pull request.

## What branch protection enforces

`main` is protected. The rules are:

- The `PR validation` check must pass, and the branch must be up to date with
  `main` before integrating.
- History must stay linear — GitHub rejects anything that would add a merge
  commit.
- No force-pushes to `main`, and `main` cannot be deleted.
- Unresolved review conversations block integration.

Admins are **not** included in these rules (`enforce_admins: false`). That is
deliberate: a solo maintainer who cannot repair a broken `main` without first
dismantling the protection is worse off, not safer. It also means the repository
owner *can* push straight to `main` — the guardrail is real for the workflow, but
it is an escape hatch, not a wall. Use it for repairs, not for saving a pull
request.

## History stays linear

Integrate with a rebase, never a merge commit:

```sh
gh pr merge <number> --rebase --delete-branch
```

Merge commits and squash merges are **disabled on the repository**, so this is
the only button that works — the setting enforces the policy rather than relying
on anyone remembering it. Rebasing keeps each commit of the pull request on
`main` as its own reviewable step, which is why squashing is off too.

When a branch falls behind `main`, rebase it rather than merging `main` into it:

```sh
git fetch origin && git rebase origin/main
git push --force-with-lease
```

Use `--force-with-lease`, not `--force`, so a rebase never discards commits that
arrived on the branch in the meantime.

## Checks that must pass

The `PR validation` check is required before a pull request can be integrated.
It runs only the parts affected by the change (see
`.github/workflows/pr-validation.yml`):

| Changed | Runs |
|---|---|
| `docs/`, `metamodel/`, `templates/`, `scripts/`, `adapters/`, `build.sh`, `AGENTS.md`, `general-semantic-contracts.md` | `./build.sh all` — stale-adapter check, metamodel and relation validation, arc42 render |
| `application/backend/` | `./gradlew test` — tests, ArchUnit rules and the JaCoCo coverage thresholds |
| `application/web/` | `npm run lint`, `npm test` (with coverage thresholds), and the PWA build (`tsc --noEmit && vite build`) |

Run the relevant one locally before pushing; they are the same commands CI uses.

Once a documentation change lands on `main`, a separate workflow republishes the
rendered architecture documentation to
<https://dieterbaier.github.io/budget/>.

## Dependency overrides

`application/web/package.json` carries one `overrides` entry, and `package.json`
has nowhere to write down why:

```json
"overrides": { "filelist": { "minimatch": "^10.2.5" } }
```

`vite-plugin-pwa` reaches `brace-expansion@2.x` through
`workbox-build → ejs → jake → filelist → minimatch@5`, and versions up to 5.0.7
carry a denial-of-service advisory. The fix is only in `brace-expansion@5`, which
is ESM-only, so overriding *that* package directly satisfies `npm audit` and
breaks the library: `minimatch@5` then fails with `expand is not a function` the
first time a glob contains a brace. Overriding `minimatch` to 10 instead brings a
version that already expects `brace-expansion@5`.

The override is scoped to `filelist` rather than declared globally. `filelist` is
the only consumer in the tree that asks for `minimatch@^5`; every other one —
`eslint`, `typescript-eslint`, `eslint-plugin-import-x`, `glob` — already asks for
`^10`. A global override therefore changes nothing today, but it would state a
policy the project does not hold, and would silently force `minimatch@10` on some
future dependency that asks for `^3`. That is the same class of latent breakage
this override exists to avoid.

**`npm audit` cannot see whether this is right.** It reports zero for the broken
override and the correct one alike, and so does the build, because this project's
own globs never take that path. `application/web/dependency-tree.test.ts` runs the
expansion on every `npm test` and fails with `expand is not a function` when the
override is wrong. Change the override, and trust that test rather than the audit
number.

Remove the override once `vite-plugin-pwa` ships a `workbox-build` that no longer
pulls the vulnerable chain — the test should keep passing without it. It exists to
unblock a transitive advisory, not because this project has an opinion about
`minimatch`.

## Architecture rules

The backend's architecture decisions are enforced by ArchUnit rules that run as
ordinary tests, so `./gradlew test` is what gates them and the table above needs
no extra entry. Two conveniences exist for working on them:

```sh
./gradlew architectureTest                 # only the rules, ~1s, no PostgreSQL
./gradlew test -PexcludeTags=architecture  # everything except the rules
```

The first is for iterating on the rules or getting a fast architecture verdict;
it skips Testcontainers entirely. The second is the deliberate escape hatch for a
refactor in progress — the rules are on by default and a violation must not reach
`main`, so use it while restructuring, not to get a pull request green.

A failing rule names the decision it protects in its message. The rule groups are
recorded as `CON-001` to `CON-004` in the architecture constraints chapter, and
the choice of tool is `ADR-013`.

The web client has the same arrangement with different tools. Its structure is
`ADR-014`, the rules are `CON-005`, and they are ESLint rules in
`application/web/eslint.config.js` rather than tests — so `npm run lint` is what
gates them, which is why the table above lists it. A deep import into another
feature's interior, a cycle between features, or a `shared/ui` component
reaching for an API module fails that step and names the decision. There is no
wholesale escape hatch equivalent to `-PexcludeTags=architecture`: an exception
is an `eslint-disable-next-line` comment with a reason, which shows up in the
diff and has to survive review.

Most violations appear in the editor while you type, which the backend's rules
cannot do. Treat a clean editor as the fast check and `npm run lint` as the
authoritative one.

## Test coverage

Both test commands also enforce coverage, so `./gradlew test` and `npm test` fail
when it drops:

| Scope | Line | Branch |
|---|---|---|
| Backend, overall | 80% | 80% |
| Backend, `domain.*` | 95% | 90% |
| Web client | 80% | 80% |

The thresholds and the reasoning are `ADR-018`; the rule groups are `CON-006` and
`CON-007`, which also list every excluded class and why. Nothing else is
excluded — in particular not adapters, DTOs or entities.

80% is a convention rather than a derived number, and the ADR says so. What it
buys is that coverage cannot fall silently, not that 80 is correct. The domain
carries the higher bar because the money rules are the product, and a single
average would let a gap there be paid for by trivial coverage elsewhere.

**Gherkin features are not the instrument.** Feature files describe behaviour;
unit tests carry the branch coverage. Cucumber runs inside `./gradlew test` and so
contributes coverage naturally, but no edge case needs a feature file, and nobody
should write scenarios to reach a percentage.

Coverage measures execution, not assertion: a test that calls a method and checks
nothing raises the number. Treat the threshold as a floor under review, not a
replacement for it.

## Bundle budget

`npm run build` also fails when the web client's JavaScript exceeds its budgets.
It prints where both stand on every build:

```
Bundle budget (CON-008)
  application  88.8 kB gzipped of 92.0 kB  (97%)
  pwa runtime  6.0 kB gzipped of 10.0 kB  (60%)
```

Two ceilings, because the two grow for unrelated reasons: the application bundle
moves when this project makes a decision, the PWA runtime when `vite-plugin-pwa`
is upgraded. Budgeting them separately stops feature work consuming the tooling
allowance and plugin churn consuming the application allowance.

The application ceiling was 70 kB, set so a revert to full Zod (74.0 kB) would
fail it. Issue #8 raised it to 92 kB for the React Router adoption, which gives
that guard up — see `QS-003` for the number that justified the raise and what it
cost.

The scenario is `QS-003`, the check is `CON-008`, and the goal is `QG-005`
resource frugality (`ADR-019`).

**Raising the budget is a decision, not a fix.** If a change genuinely needs more,
record the new number and its justification in `QS-003`. Raising it to make a
build pass is the failure mode the budget exists to make visible, and no tool can
prevent that — only make it deliberate.
