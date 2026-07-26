// Guards the `overrides` entry in package.json, which is explained in
// CONTRIBUTING.md.
//
// `vite-plugin-pwa` reaches `brace-expansion` through
// workbox-build -> ejs -> jake -> filelist -> minimatch. Versions of
// brace-expansion up to 5.0.7 carry a denial-of-service advisory, and the fix
// only exists in a major that is ESM-only. Overriding brace-expansion directly
// satisfies `npm audit` and breaks minimatch, which then throws
// "expand is not a function" on the first glob containing a brace.
//
// The build does not catch that, because this project's own globs never take
// the path. Neither does `npm audit`, which would report zero. Only running the
// expansion catches it — so it runs here, on every `npm test`, rather than once
// by hand at review time.

import { describe, it, expect } from 'vitest'
import { createRequire } from 'node:module'
import { mkdtempSync, rmSync, writeFileSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { basename, join } from 'node:path'

const require = createRequire(import.meta.url)

describe('the installed dependency tree', () => {
  it('expands a brace glob through filelist, the way workbox does', () => {
    const dir = mkdtempSync(join(tmpdir(), 'budget-brace-glob-'))

    try {
      writeFileSync(join(dir, 'kept.ts'), '')
      writeFileSync(join(dir, 'kept.tsx'), '')
      writeFileSync(join(dir, 'ignored.md'), '')

      // Loaded from the real tree rather than imported, so the assertion is
      // about what is installed and not about what a bundler resolved.
      const { FileList } = require('filelist')
      const list = new FileList()
      list.include(join(dir, '*.{ts,tsx}'))

      // Not `.map(basename)`: map passes the index as basename's `suffix`.
      const found = list.toArray().map((path: string) => basename(path)).sort()

      // The brace is the point. A single-extension glob would pass even with a
      // broken brace-expansion.
      expect(found).toEqual(['kept.ts', 'kept.tsx'])
    } finally {
      rmSync(dir, { recursive: true, force: true })
    }
  })

  it('renders through ejs, which is what pulls that chain in', () => {
    const ejs = require('ejs')

    expect(ejs.render('<%= value %>', { value: 'ok' })).toBe('ok')
  })
})
