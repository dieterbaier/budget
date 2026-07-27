// CON-008: the web client's JavaScript budgets (QS-003, QG-005).
//
// Runs after `vite build`, so exceeding a budget fails `npm run build` and
// therefore the `web` job of PR validation. A budget nothing enforces is a
// number in a document.
//
// Two ceilings rather than one, because the two grow for unrelated reasons:
//
//   application  dist/assets/*.js -- what this project's own decisions move.
//                ADR-016 chose Zod Mini over the full build and took 13.7 kB
//                out of this number.
//   pwa runtime  the service worker and Workbox runtime that vite-plugin-pwa
//                generates. Not something feature work changes; it moves when
//                the plugin is upgraded.
//
// Separating them means feature work cannot consume the tooling allowance and
// plugin churn cannot consume the application allowance. The first version of
// this check reported the runtime without bounding it, which left it free to
// grow to any size: a printed line is not a gate.

import { gzipSync } from 'node:zlib'
import { readFileSync, readdirSync } from 'node:fs'
import { join } from 'node:path'
import process from 'node:process'

// Bytes. Displayed as kB (1000 bytes) to match what `vite build` prints, so the
// two numbers reconcile.
//
// The application ceiling is set so that the regression it claims to prevent
// actually fails it: reverting to the full Zod build measures 74.0 kB, which
// passes at 75 kB and fails at 70 kB. The first version used 75 kB and would
// have let that regression through — see QS-003.
const BUDGETS = {
  application: 70_000,
  'pwa runtime': 10_000,
}

const dist = join(import.meta.dirname, '..', 'dist')

function gzippedSize(files) {
  return files.reduce((total, file) => total + gzipSync(readFileSync(file)).length, 0)
}

function jsIn(dir) {
  return readdirSync(dir, { withFileTypes: true })
    .filter((entry) => entry.isFile() && entry.name.endsWith('.js'))
    .map((entry) => join(dir, entry.name))
}

const measured = {
  application: gzippedSize(jsIn(join(dist, 'assets'))),
  'pwa runtime': gzippedSize(jsIn(dist)),
}

const kb = (bytes) => `${(bytes / 1000).toFixed(1)} kB`

console.log('\nBundle budget (CON-008)')
for (const [name, budget] of Object.entries(BUDGETS)) {
  const used = ((measured[name] / budget) * 100).toFixed(0)
  console.log(`  ${name.padEnd(12)} ${kb(measured[name])} gzipped of ${kb(budget)}  (${used}%)`)
}

const over = Object.entries(BUDGETS).filter(([name, budget]) => measured[name] > budget)

if (over.length > 0) {
  for (const [name, budget] of over) {
    console.error(`\nThe ${name} is ${kb(measured[name])} gzipped, over the ${kb(budget)} budget.`)
  }
  console.error(
    '\nEither make it smaller or change the budget deliberately in QS-003 -- but change it\n' +
      'as a decision, with the number that justifies it, rather than to make this pass.\n',
  )
  process.exit(1)
}
