// CON-008: the web client's JavaScript budget (QS-003, QG-005).
//
// Runs after `vite build`, so exceeding the budget fails `npm run build` and
// therefore the `web` job of PR validation. A budget nothing enforces is a
// number in a document.
//
// What is measured: the application bundle in dist/assets. That is what the
// project's own decisions move -- ADR-016 chose Zod Mini over full Zod and saved
// 13.7 KB here.
//
// What is not: the service worker and the Workbox runtime that vite-plugin-pwa
// generates. They are the tooling's output rather than a consequence of how this
// application is written, and budgeting them would make the number move when the
// plugin is upgraded, for reasons no feature change caused. They are reported
// anyway, so a jump in them is visible rather than silent.

import { gzipSync } from 'node:zlib'
import { readFileSync, readdirSync } from 'node:fs'
import { join } from 'node:path'
import process from 'node:process'

// Bytes. Displayed as kB (1000 bytes) to match what `vite build` prints, so the
// two numbers reconcile.
const BUDGET_BYTES = 75_000

const dist = join(import.meta.dirname, '..', 'dist')

function gzippedSize(files) {
  return files.reduce((total, file) => total + gzipSync(readFileSync(file)).length, 0)
}

function jsIn(dir, predicate = () => true) {
  return readdirSync(dir, { withFileTypes: true })
    .filter((entry) => entry.isFile() && entry.name.endsWith('.js') && predicate(entry.name))
    .map((entry) => join(dir, entry.name))
}

const appBundle = gzippedSize(jsIn(join(dist, 'assets')))
const tooling = gzippedSize(jsIn(dist))

const kb = (bytes) => `${(bytes / 1000).toFixed(1)} kB`
const used = ((appBundle / BUDGET_BYTES) * 100).toFixed(0)

console.log(`\nBundle budget (CON-008)`)
console.log(`  application  ${kb(appBundle)} gzipped of ${kb(BUDGET_BYTES)}  (${used}% of budget)`)
console.log(`  pwa runtime  ${kb(tooling)} gzipped  (reported, not budgeted)`)

if (appBundle > BUDGET_BYTES) {
  console.error(
    `\nThe application bundle is ${kb(appBundle)} gzipped, over the ${kb(BUDGET_BYTES)} budget.\n` +
      `Either make it smaller or change the budget deliberately in QS-003 -- but change it\n` +
      `as a decision, with the number that justifies it, rather than to make this pass.\n`,
  )
  process.exit(1)
}
