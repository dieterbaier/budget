// The feature's public API — CON-005. Named exports only; extended on demand.
export { CategoriesPage } from './components/CategoriesPage'
// A component rather than the list plus the query: the caller asks for a
// choice, not for data it would then have to know how to render.
export { CategoryPicker } from './components/CategoryPicker'
