-- Initial schema for the current monthly expenditure slice.
-- Accounts, category groups, balances, and pension/asset tables arrive in later
-- slices; this migration covers only what the monthly-expenditure use case reads.

create table categories (
    id               bigint generated always as identity primary key,
    name             varchar(100) not null unique,
    pension_relevant boolean      not null default true
);

create table transactions (
    id               bigint         generated always as identity primary key,
    booking_date     date           not null,
    amount           numeric(12, 2) not null,
    category_id      bigint         not null references categories (id),
    transaction_type varchar(20)    not null
);

create index idx_transactions_booking_date on transactions (booking_date);

create table fixed_costs (
    id               bigint         generated always as identity primary key,
    name             varchar(100)   not null,
    amount           numeric(12, 2) not null,
    payment_interval varchar(20)    not null,
    category_id      bigint         not null references categories (id),
    anchor_date      date           not null
);

create table income_entries (
    id           bigint         generated always as identity primary key,
    income_month date           not null,
    amount       numeric(12, 2) not null
);
