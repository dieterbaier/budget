# Next Steps

_Last updated: 2026-07-24 — end of the first slice._

Working notes on where to continue. The authoritative product/architecture
records live under `docs/` (see the pointers at the bottom); this file is just a
quick, practical hand-off.

## Where we are

First vertical slice complete and verified end-to-end (PWA → backend → PostgreSQL).

- **Architecture docs** — arc42 reviewed and the first decisions recorded as ADRs
  (ADR-001, ADR-003–ADR-012). Product scope: private, single-user actuals tracker
  replacing Notion.
- **Backend** (`application/backend/`) — Spring Boot (Java 21, Gradle),
  Clean/Hexagonal, TDD + Cucumber, no Lombok, explicit bean wiring. Implements the
  *current monthly expenditure* view (read) and *record transaction* (write),
  PostgreSQL via Flyway, verified with Testcontainers. **23 tests.**
- **Web** (`application/web/`) — React + TypeScript Vite **PWA**: monthly
  expenditure view + record-transaction form, same-origin via the `/api` dev
  proxy. **4 tests.**
- **Local run** — compose Postgres + `bootRun` + Vite; the full stack was run and
  confirmed working (read, write reflected in read, and error handling).

## Quick start

```sh
# backend (terminal 1)
cd application/backend && podman compose up -d && ./gradlew bootRun   # or: docker compose up -d
# web (terminal 2)
cd application/web && npm install && npm run dev                      # http://localhost:5173

# tests
cd application/backend && ./gradlew test
cd application/web && npm test
```

## Open decisions to close

- **Q-ARCH-003** — Transaction identity source: a stable bank reference vs a
  generated `TransactionId` (UUID). Needs a look at the real bank-export format.
  Blocks edit/delete.
- **Q-OPS-003** — Which always-on host (small VPS) for multi-device use.
- **Q-PO-004 is answered** (nominal pension projection) but the exact compounding
  is finalized when the pension view is built.

## Candidate next slices

1. **Master-data / bootstrap screens** — categories, category groups, accounts
   (+ manual balance snapshots), fixed costs, income table. The other views are
   thin without this.
2. **Transaction identity + edit/delete** — introduce `TransactionId` (ADR-009),
   return an id + `Location` from `POST`, and add edit/delete. Closes Q-ARCH-003.
3. **Remaining views** (build on master data) — group view; reserve requirement
   for the non-monthly fixed-cost account; cash runway to month end; net worth +
   nominal pension projection with the shortfall.
4. **Single-user auth** (ADR-007) — one credential, Argon2, TLS. Do before any
   hosted deployment.
5. **Deployment** — pick the host (Q-OPS-003), a hosted profile, and backups
   (nightly `pg_dump` + the in-app export endpoint).

**Suggested order:** 1 → 2 → 3 → 4 → 5. The killer views need master data first;
identity/edit makes entries correctable; auth and deployment come when going
multi-device.

## Where things are documented

- Vision & scope — `docs/doc-002-vision-mission.adoc`, `docs/arc42/01-introduction-and-goals/`
- Roadmap — `docs/doc-004-roadmap.adoc`
- Open questions — `docs/doc-005-questions-and-answers.adoc`
- Decisions (ADRs) — `docs/arc42/09-architecture-decisions/`
- How to run / build — `README.md`
