// Lint configuration for the web client.
//
// The architecture block at the bottom is the fitness function for ADR-014:
// it is what CON-005 points at as evidence, and a rule broken there fails the
// `PR validation` check. See ADR-015 for why ESLint carries these rules rather
// than a dedicated architecture checker.
//
// ESLint is pinned to 9 rather than 10 on purpose: eslint-plugin-import, which
// provides the cycle check, still caps at 9. Revisit when it moves.

import js from '@eslint/js'
import globals from 'globals'
import tseslint from 'typescript-eslint'
import boundaries from 'eslint-plugin-boundaries'
import importPlugin from 'eslint-plugin-import'
import reactHooks from 'eslint-plugin-react-hooks'

// The app shell and a feature see the same things: other features through their
// public API only, plus everything shared.
const fromInsideTheApp = [
  { element: { type: 'feature', fileInternalPath: 'index.ts' } },
  { element: { type: 'shared' } },
  { element: { type: 'shared-ui' } },
]

export default tseslint.config(
  { ignores: ['dist/**', 'dev-dist/**', 'coverage/**'] },

  js.configs.recommended,
  ...tseslint.configs.recommended,

  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2022,
      globals: { ...globals.browser },
    },
    plugins: { 'react-hooks': reactHooks },
    rules: {
      ...reactHooks.configs.recommended.rules,
    },
  },

  // Config files run in Node, not in the browser.
  {
    files: ['*.config.{js,ts}'],
    languageOptions: { globals: { ...globals.node } },
  },

  // ---------------------------------------------------------------------
  // CON-005: Feature Boundaries
  // ---------------------------------------------------------------------
  {
    files: ['src/**/*.{ts,tsx}'],
    plugins: { boundaries, import: importPlugin },
    settings: {
      // `import/parsers` is load-bearing, not boilerplate. Without it
      // `import/no-cycle` cannot parse the .ts files it follows, finds no
      // exports, and so reports no cycles -- it passes because it is blind
      // rather than because the code is acyclic. Verified by probe: a
      // deliberate two-file cycle goes undetected without this line and is
      // reported with it.
      'import/parsers': { '@typescript-eslint/parser': ['.ts', '.tsx'] },
      'import/resolver': { typescript: { project: './tsconfig.json' } },

      // Order matters: the first matching descriptor wins, so shared/ui is
      // declared before the general shared segment it sits inside.
      'boundaries/elements': [
        { type: 'app', pattern: 'src/app' },
        { type: 'feature', pattern: 'src/features/*', capture: ['feature'] },
        { type: 'shared-ui', pattern: 'src/shared/ui' },
        { type: 'shared', pattern: 'src/shared/*', capture: ['segment'] },
      ],

      // The entry point and the ambient type declaration belong to no element,
      // exactly as BudgetApplication belongs to no backend layer (CON-002).
      'boundaries/ignore': ['src/main.tsx', 'src/vite-env.d.ts', 'src/test/**'],
    },
    rules: {
      // Dependencies *within* one element are not checked (`checkInternals`
      // defaults to false), which is what lets a feature's own component import
      // its own api module by relative path while an outsider importing that
      // same file is rejected.
      //
      // The two `disallow` policies come first and exist only to produce a
      // better message than the catch-all would: without them a deep import
      // reports the baffling "feature may not depend on feature".
      'boundaries/dependencies': [
        'error',
        {
          default: 'disallow',
          message: '{{from.type}} may not depend on {{to.type}} (CON-005, ADR-014).',
          policies: [
            {
              from: [{ element: { type: 'app' } }, { element: { type: 'feature' } }],
              disallow: [{ element: { type: 'feature', fileInternalPath: '!index.ts' } }],
              message:
                'Import feature "{{to.captured.feature}}" through its index.ts, not its interior (CON-005, ADR-014).',
            },
            {
              from: [{ element: { type: 'shared-ui' } }],
              disallow: [
                { element: { type: 'feature' } },
                { element: { type: 'shared', captured: { segment: '!format' } } },
              ],
              message:
                'shared/ui is presentational: it may not reach {{to.type}} (CON-005, ADR-014).',
            },

            { from: [{ element: { type: 'app' } }], allow: fromInsideTheApp },
            { from: [{ element: { type: 'feature' } }], allow: fromInsideTheApp },
            {
              from: [{ element: { type: 'shared-ui' } }],
              allow: [
                { element: { type: 'shared-ui' } },
                { element: { type: 'shared', captured: { segment: 'format' } } },
              ],
            },
            {
              from: [{ element: { type: 'shared' } }],
              allow: [{ element: { type: 'shared' } }],
            },
          ],
        },
      ],

      // The feature graph is acyclic. This rule takes no custom message, so
      // unlike the others it does not name CON-005 in its output.
      'import/no-cycle': ['error', { maxDepth: Infinity, ignoreExternal: true }],
    },
  },

  // Tests sit beside the code they exercise and run under Node globals.
  {
    files: ['**/*.test.{ts,tsx}'],
    languageOptions: { globals: { ...globals.node } },
  },
)
