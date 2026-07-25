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
- `main` is protected: it takes no direct pushes, and every change arrives
  through a pull request.

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
| `application/web/` | `npm test` and the PWA build (`tsc --noEmit && vite build`) |

Run the relevant one locally before pushing; they are the same commands CI uses.

Once a documentation change lands on `main`, a separate workflow republishes the
rendered architecture documentation to
<https://dieterbaier.github.io/budget/>.
