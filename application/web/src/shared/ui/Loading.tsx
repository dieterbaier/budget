/**
 * The default placeholder while something is being read. Muted, like every
 * other placeholder in the app -- a screen that is waiting should not shout as
 * loudly as one that has something to say.
 *
 * Where the shape of the waiting matters more than the word -- a form control
 * that must not change size when its data arrives -- the call site passes its
 * own placeholder to `QueryBoundary` instead of using this.
 */
export function Loading() {
  return <p className="text-muted">Loading…</p>
}
