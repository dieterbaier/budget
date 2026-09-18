-- The fixed cost takes its registered terms (issue #79, ADR-022).
--
-- V1 created this table under names chosen before the term register existed.
-- anchor_date was the worst of them: it named a fixed reference point, and the
-- domain has letzteZahlung, a date that moves forward with every payment.
--
-- V1 is not edited. It has run, Flyway validates it against a stored checksum,
-- and it stays the record of what ran (ADR-010). Renames are metadata-only in
-- PostgreSQL, so the rows are untouched.

alter table fixed_costs rename to fixkostenpositionen;

alter table fixkostenpositionen rename column payment_interval to zahlungsintervall;

alter table fixkostenpositionen rename column anchor_date to letzte_zahlung;

-- PostgreSQL derived these three names from the table when V1 created it, and a
-- table rename leaves them behind.
alter table fixkostenpositionen rename constraint fixed_costs_pkey to fixkostenpositionen_pkey;

alter table fixkostenpositionen
    rename constraint fixed_costs_category_id_fkey to fixkostenpositionen_category_id_fkey;

alter sequence fixed_costs_id_seq rename to fixkostenpositionen_id_seq;
