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
| `application/backend/` | `./gradlew test` |
| `application/web/` | `npm run lint`, `npm test`, and the PWA build (`tsc --noEmit && vite build`) |

Run the relevant one locally before pushing; they are the same commands CI uses.

Once a documentation change lands on `main`, a separate workflow republishes the
rendered architecture documentation to
<https://dieterbaier.github.io/budget/>.

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
