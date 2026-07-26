import { defineConfig, presetWind3 } from 'unocss'

// The palette and the control look are the shared surface; everything else is
// styled on the component that owns it (ADR-014). A feature adding a screen
// should not need to touch this file, and never needs to touch a stylesheet
// that another feature also edits.
export default defineConfig({
  presets: [presetWind3()],

  theme: {
    colors: {
      ink: '#1f2933',
      muted: '#616e7c',
      line: '#cbd2d9',
      hairline: '#e4e7eb',
      canvas: '#f7f9fb',
      danger: { DEFAULT: '#a61b1b', soft: '#fdecea' },
      good: { DEFAULT: '#1b7a3d', soft: '#e6f4ea' },
    },
  },

  shortcuts: {
    // Form controls look the same across every feature, so their look is a
    // token rather than something each screen restates.
    'field-label': 'flex flex-col gap-1 mb-3 text-sm',
    'field-control': 'p-2 bg-white border border-line rounded-[0.4rem]',
    'field-submit':
      'p-2 bg-ink text-white border-none rounded-[0.4rem] cursor-pointer disabled:opacity-60',
  },
})
