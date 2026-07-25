// The feature's public API — CON-005. Everything else under this directory is
// internal and must not be imported from outside the feature.
//
// Exports are named deliberately rather than re-exported with `export *`, which
// would re-expose the interior the rule exists to hide. Add to this list when
// another feature genuinely needs something, not in advance.
export { MonthlyExpenditureView } from './components/MonthlyExpenditureView'
